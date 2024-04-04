import java.util.ArrayList;

public class OS {
	
	//enum of what function to call
	enum CallType{
		CreateProcess, PriorityCreateProcess, SwitchProcess, Sleep, 
		Open, Close, Read, Seek, Write, GetPID, GetPIDByName, SendMessage, WaitForMessage
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
	
	//helper method
	private static void KernelSwitch() throws InterruptedException {	
		PCB c = kernel.getScheduler().getCurrentlyRunning();
		kernel.start();
		if(c != null) {
			c.stop();
		}
	}
}
