import java.util.ArrayList;
import java.util.Random;

public class OS {
	
	//enum of what function to call
	enum CallType{
		CreateProcess, PriorityCreateProcess, SwitchProcess, Sleep, 
		Open, Close, Read, Seek, Write, GetPID, GetPIDByName, SendMessage, 
		WaitForMessage, AllocateMemory, FreeMemory,
	}
	
	enum Priority{
		High, Normal, Low
	}
	
	//static instance of enum
	static CallType call;
	static Priority priority;
	//static array list of parameters
	static ArrayList<Object> parameters = new ArrayList<Object>();
	//static object of return value
	static Object returnValue;
	private static Kernel kernel;
	

	public static int CreateProcess(UserlandProcess up) throws InterruptedException {
		//resets parameters
		parameters.clear();
		//add new parameters to the list
		parameters.add(up);
		//set the current call
		call = CallType.CreateProcess;
		//switches to kernel
		kernel.start();			
		while(kernel.getScheduler().getCurrentlyRunning() == null) {
			Thread.sleep(10);
		}
		kernel.getScheduler().getCurrentlyRunning().stop();
		
		//cast and return the return value
		if(returnValue != null) {
			return (int)(returnValue);
		}
		else {
			return 0;
		}
	}
	
	public static int CreateProcess(UserlandProcess up, Priority prio) throws InterruptedException {
		//resets parameters
		parameters.clear();
		//add new parameters to the list
		parameters.add(up);
		//set the current call
		call = CallType.PriorityCreateProcess;
		priority = prio;
		//switches to kernel
		kernel.start();			
		while(kernel.getScheduler().getCurrentlyRunning() == null) {
			Thread.sleep(10);
		}
		kernel.getScheduler().getCurrentlyRunning().stop();
		//cast and return the return value
		if(returnValue != null) {
			return (int)(returnValue);
		}
		else {
			return 0;
		}
	}
	
	public static void SwitchProcess() throws InterruptedException {
		//set the current call
		call = CallType.SwitchProcess;		
		
		kernel.getScheduler().getCurrentlyRunning().getUserlandProcess().TLBClear();
		
		//switches to kernel
		KernelSwitch();
	}
	
	public static void Sleep(int milliseconds) throws InterruptedException {
		//reset parameters
		parameters.clear();
		//add new parameters to the list
		parameters.add(milliseconds);
		//set the current call type
		call = CallType.Sleep;
		//switch to kernel
		KernelSwitch();
	}
	
	//Creates the Kernel() and calls CreateProcess twice – once for “init” and once for the idle process.
	public static void Startup(UserlandProcess init) throws InterruptedException {
		kernel = new Kernel();		
		CreateProcess(init);
		CreateProcess(new IdleProcess());				
	}
	
	//OS method to call open in kernel
	public static int Open(String s) throws InterruptedException {
		parameters.clear();
		parameters.add(s);
		call = CallType.Open;
		
		KernelSwitch();
		
		return (int)(returnValue);
	}
	
	//OS method to call close in kernel
	public static void Close(int id) throws InterruptedException {
		parameters.clear();
		parameters.add(id);
		call = CallType.Close;
		
		KernelSwitch();
	}
	
	//OS method to call read in kernel
	public static byte[] Read(int id, int size) throws InterruptedException {
		parameters.clear();
		parameters.add(id);
		parameters.add(size);
		call = CallType.Read;
		
		KernelSwitch();
		
		return (byte[])(returnValue);
	}
	
	//OS method to call seek in kernel
	public static void Seek(int id, int to) throws InterruptedException {
		parameters.clear();
		parameters.add(id);
		parameters.add(to);
		call = CallType.Seek;
		
		KernelSwitch();
	}
	
	//OS method to call write in kernel
	public static int Write(int id, byte[] data) throws InterruptedException {
		parameters.clear();
		parameters.add(id);
		parameters.add(data);
		
		call = CallType.Write;
		
		KernelSwitch();
		
		return (int)(returnValue);
	}
	
	//OS method for getting current pid
	public static int GetPID() throws InterruptedException {
		call = CallType.GetPID;
		
		KernelSwitch();
		
		return (int)(returnValue);
	}
	
	//OS method for getting pid based on name
	public static int GetPIDByName(String name) throws InterruptedException {
		parameters.clear();
		parameters.add(name);
		
		call = CallType.GetPIDByName;
		
		KernelSwitch();
		
		return (int)(returnValue);
	}
	
	//OS method to send a message using sender pid to target pid
	public static void SendMessage(KernelMessage km) throws InterruptedException {
		KernelMessage message = new KernelMessage(km);
		message.setSenderPID(OS.GetPID());
		
		parameters.clear();
		parameters.add(message);
		
		call = CallType.SendMessage;
		
		KernelSwitch();
	}
	
	//OS method to wait for a message to be sent
	public static KernelMessage WaitForMessage() throws InterruptedException {
		call = CallType.WaitForMessage;
		
		KernelSwitch();
		
		return (KernelMessage)(returnValue);
	}
	
	
	public static void GetMapping(int virtualPageNumber) {
		PCB pcb = kernel.getScheduler().getCurrentlyRunning();
		
		if(virtualPageNumber >= 100) {
			return;
		}
		
		if(pcb.getPage(virtualPageNumber) == null) {
			pcb.newPage(virtualPageNumber);
		}
		
		//loops until it the page returns a valid memory location
		while(pcb.getPage(virtualPageNumber).physicalPageNumber == -1) {
			
			//looks through a random processes page for a page with memory allocated to it
			int page = -1;
			PCB victim = kernel.getScheduler().getRandomProcess();
			for(int i = 0; i < 100; i++) {
				if(victim.getPage(i) != null && victim.getPage(i).physicalPageNumber != -1) {
					page = i;
					break;
				}
			}
			
			//true if the victim process has a page with memory
			//repeats search loop if the process doesn't have memory allocated
			if(page != -1) {
				//gets the data from memory location we are taking then clears it
				byte[] data = new byte[1024];
				byte newByte = 0;
				int memoryLocation = victim.getPage(page).physicalPageNumber;
				for(int i = 0; i < 1024; i++) {
					data[i] = victim.getUserlandProcess().Read(memoryLocation + i);
					victim.getUserlandProcess().Write(memoryLocation + i, newByte);
				}
				
				//writes the data to swap file and sets the physical page to -1 while setting the disk page to offset
				kernel.ffs.Write(kernel.swapFD, data);
				victim.getPage(page).physicalPageNumber = -1;
				victim.getPage(page).diskPageNumber = kernel.offSet;
				kernel.offSet++;
				
				//retrieves the data from swap file if it was swapped out before
				if(pcb.getPage(virtualPageNumber).diskPageNumber != -1) {
					int disk = pcb.getPage(virtualPageNumber).diskPageNumber;
					byte[] read = kernel.ffs.Read(kernel.swapFD, (disk + 1) * 1024);
					for(int i = disk * 1024; i < (disk + 1)* 1024; i++) {
						pcb.getUserlandProcess().Write(i * memoryLocation, read[i]);
					}
					pcb.getPage(virtualPageNumber).diskPageNumber = -1;
				}				
				
				pcb.getPage(virtualPageNumber).physicalPageNumber = memoryLocation;
			}
		}
		//randomly replaces tlb
		Random random = new Random();
		int randInt = random.nextInt(2);
		
		if(randInt == 0) {
			pcb.getUserlandProcess().setTLB(0, virtualPageNumber, pcb.getPage(virtualPageNumber).physicalPageNumber);
		}
		else if(randInt == 1) {
			pcb.getUserlandProcess().setTLB(1, virtualPageNumber, pcb.getPage(virtualPageNumber).physicalPageNumber);
		}
	}
	
	public static int AllocateMemory(int size) throws InterruptedException {
		//checks that it is divisible by 1024
		if(size%1024 == 0) {
			//adds parameters
			parameters.clear();
			parameters.add(size);
			
			//sets call
			call = CallType.AllocateMemory;
			
			//switches to kernel
			KernelSwitch();
			
			return (int)(returnValue);
		}
		else {
			return -1;
		}
	}
	
	public static boolean FreeMemory(int pointer, int size) throws InterruptedException {
		//checks that it is divisible by 1024
		if(size%1024 == 0) {
			//adds parameters
			parameters.clear();
			parameters.add(pointer);
			parameters.add(size);
			
			//sets call
			call = CallType.FreeMemory;
			
			//switches to kernel
			KernelSwitch();
			
			return (boolean)(returnValue);
		}
		else {
			return false;
		}
	}
	
	//helper method
	private static void KernelSwitch() throws InterruptedException {	
		PCB c = kernel.getScheduler().getCurrentlyRunning();
		kernel.start();
		if(c != null) {
			c.stop();
		}
	}
}
