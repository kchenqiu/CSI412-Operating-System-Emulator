import java.lang.Thread;
import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable{
	Thread userThread;
	private Semaphore semaphore;
	private boolean quantum;
	
    public UserlandProcess() {
        userThread = new Thread(this,this.getClass().getName());        
        semaphore= new Semaphore(0, true);
        quantum = false;
        userThread.start();
    }
    
	//sets the boolean indicating that this process’ quantum has expired
	public void requestStop() {
		quantum = true;
	}
	
	//will represent the main of our “program”
	public abstract void main();
	
	//indicates if the semaphore is 0
	public boolean isStopped() {
		if(semaphore.availablePermits() == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	//true when the Java thread is not alive
	public boolean isDone() {
		if(userThread.isAlive()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	//releases (increments) the semaphore, allowing this thread to run
	public void start(){
		semaphore.release();
	}
	
	//acquires (decrements) the semaphore, stopping this thread from running
	public void stop() throws InterruptedException {
		semaphore.acquire();
	}
	
	//acquire the semaphore, then call main
	@Override
	public void run() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		main();
	}
	
	//if the boolean is true, set the boolean to false and call OS.switchProcess()
	public void cooperate() throws InterruptedException {
		if(quantum == true) {
			quantum = false;
			OS.SwitchProcess();
		}
	}
}
