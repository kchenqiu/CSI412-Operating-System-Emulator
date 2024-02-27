import java.lang.Thread;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable {
	private Thread kernelThread ;
	private Semaphore semaphore;
	private Scheduler scheduler;
	private PCB newPCB;
	
	//initializes the variables then calls thread.start()
	public Kernel() throws InterruptedException {
		kernelThread = new Thread(this, "KERNEL");
		semaphore = new Semaphore(0, true);
		scheduler = new Scheduler();		
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
				scheduler.current.run();
				break;	
				
			case PriorityCreateProcess:
				newPCB = new PCB((UserlandProcess)(OS.parameters.get(0)));
				OS.returnValue = scheduler.CreateProcess(newPCB, OS.priority);	
				scheduler.current.run();
				break;
				
			//calls scheduler switch process
			case SwitchProcess:
				scheduler.SwitchProcess();	
				scheduler.current.run();
				break;	
				
			case Sleep:
				scheduler.Sleep((int)(OS.parameters.get(0)));
				scheduler.current.run();
				break;
			}				
		}
	}
}
