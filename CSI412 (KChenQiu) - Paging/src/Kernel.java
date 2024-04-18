import java.lang.Thread;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable, Device {
	private Thread kernelThread ;
	private Semaphore semaphore;
	private Scheduler scheduler;
	private PCB newPCB;
	private VirtualFileSystem vfs;
	private boolean[] memoryMap = new boolean[1024];
	
	//initializes the variables then calls thread.start()
	public Kernel() throws InterruptedException {
		kernelThread = new Thread(this, "KERNEL");
		semaphore = new Semaphore(0, true);
		scheduler = new Scheduler();	
		vfs = new VirtualFileSystem();
		kernelThread.start();
	}
	
	//releases (increments) the semaphore, allowing this thread to run
	public void start() throws InterruptedException {
		semaphore.release();
	}
	
	//accessor method for scheduler
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	//run method
	public void run() {	
		int size;
		//loops forever, waiting for a semaphore 	
		while(true) {		
			//when semaphore is acquired, goes through a switch with the current call type
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
			switch(OS.call) {				
			//calls scheduler CreateProcess using the UserlandProcess passed through OS
			case CreateProcess:	
				newPCB = new PCB((UserlandProcess)(OS.parameters.get(0)));
				OS.returnValue = scheduler.CreateProcess(newPCB);	
				scheduler.getCurrentlyRunning().run();
				break;	
				
			case PriorityCreateProcess:
				newPCB = new PCB((UserlandProcess)(OS.parameters.get(0)));
				OS.returnValue = scheduler.CreateProcess(newPCB, OS.priority);	
				scheduler.getCurrentlyRunning().run();
				break;
				
			//calls scheduler switch process
			case SwitchProcess:
				try {
					scheduler.SwitchProcess();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				scheduler.getCurrentlyRunning().run();	
				break;	
				
			//calls scheduler sleep
			case Sleep:
				scheduler.Sleep((int)(OS.parameters.get(0)));
				scheduler.getCurrentlyRunning().run();
				break;
				
			//uses the device open 
			case Open:
				OS.returnValue = Open((String)(OS.parameters.get(0)));
				scheduler.getCurrentlyRunning().run();
				break;
				
			//uses the device close
			case Close:
				Close((int)(OS.parameters.get(0)));
				scheduler.getCurrentlyRunning().run();
				break;
				
			//uses the device read
			case Read:
				OS.returnValue = Read((int)(OS.parameters.get(0)),(int)(OS.parameters.get(1)));
				scheduler.getCurrentlyRunning().run();
				break;
				
			//uses the device seek
			case Seek:
				Seek((int)(OS.parameters.get(0)), (int)(OS.parameters.get(1)));
				scheduler.getCurrentlyRunning().run();
				break;
				
			//uses the device write
			case Write:
				OS.returnValue = Write((int)(OS.parameters.get(0)), (byte[])(OS.parameters.get(1)));
				scheduler.getCurrentlyRunning().run();
				break;	
				
			case GetPID:
				OS.returnValue = scheduler.getPID();
				scheduler.getCurrentlyRunning().run();
				break;
				
			case GetPIDByName:
				OS.returnValue = scheduler.getPIDByName((String)(OS.parameters.get(0)));
				scheduler.getCurrentlyRunning().run();
				break;
				
			case SendMessage:
				scheduler.SendMessage((KernelMessage)(OS.parameters.get(0)));
				scheduler.getCurrentlyRunning().run();
				break;
				
			case WaitForMessage:
				OS.returnValue = scheduler.WaitForMessage();
				scheduler.getCurrentlyRunning().run();
				break;
				
			case AllocateMemory:
				size = (int)(OS.parameters.get(0))/1024;
				int memoryLocation = 0;
				//checks if there is enough room for memory allocation
				for(int i = 0; i < size; i++) {
					//checks if memory is taken
					if(!memoryMap[i]) {
						memoryLocation = i;
					}
					//increases size by i if there is not enough space
					else {
						size = (int)(OS.parameters.get(0)) + i;
					}
				}
				
				memoryLocation = memoryLocation - ((int)(OS.parameters.get(0))/1024)+1;
				
				for(int i = memoryLocation; i < size; i++) {
					memoryMap[i] = true;
					scheduler.getCurrentlyRunning().setPage(i);
				}
				
				OS.returnValue = memoryLocation;
				
				scheduler.getCurrentlyRunning().run();
				
				break;
				
			case FreeMemory:
				int pointer = (int)(OS.parameters.get(0));
				size = (int)(OS.parameters.get(1))/1024;
				
				//starts at pointer and sets a part of memory map to false and resets the page array in PCB
				for(int i = pointer; i < (size + pointer); i++) {
					scheduler.getCurrentlyRunning().setTargetPage(i, -1);
					memoryMap[i] = false;
				}
				
				OS.returnValue = true;
				
				scheduler.getCurrentlyRunning().run();
				
				break;
				
			default:
				break;
			
			}				
		}
	}
	//opens a device and saves the id into PCB
	@Override
	public int Open(String s) {
		for(int i = 0; i < 10; i++) {
			if(scheduler.getCurrentlyRunning().getInt(i) == -1) {
				scheduler.getCurrentlyRunning().setInt(i, vfs.Open(s));
				return scheduler.getCurrentlyRunning().getInt(i);
			}
		}
		return -1;
	}

	//uses Virtual File System method to close
	@Override
	public void Close(int id) {
		vfs.Close(scheduler.getCurrentlyRunning().getInt(id));
	}

	//uses Virtual File System method to read
	@Override
	public byte[] Read(int id, int size) {
		return vfs.Read(scheduler.getCurrentlyRunning().getInt(id), size);
	}

	//uses Virtual File System method to seek
	@Override
	public void Seek(int id, int to) {
		vfs.Seek(scheduler.getCurrentlyRunning().getInt(id), to);
		
	}

	//uses Virtual File System method to write
	@Override
	public int Write(int id, byte[] data) {
		return vfs.Write(scheduler.getCurrentlyRunning().getInt(id), data);
	}
}
