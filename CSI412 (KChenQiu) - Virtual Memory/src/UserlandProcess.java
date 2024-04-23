import java.lang.Thread;
import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable{
	private Thread userThread;
	private Semaphore semaphore;
	private boolean quantum;
	private static int[][] tlb = new int[2][2];
	private static byte[] memory = new byte[1048576];
	
    public UserlandProcess() {
        userThread = new Thread(this, this.getClass().getName());    
        semaphore  = new Semaphore(0, true);
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
	
	public boolean getQuantum() {
		return quantum;
	}
	
	//if the boolean is true, set the boolean to false and call OS.switchProcess()
	public void cooperate() throws InterruptedException {
		if(quantum == true) {
			quantum = false;
			OS.SwitchProcess();
		}
	}
	
	public byte Read(int address) {
		int virtualAddress = address;
		int virtualPage = virtualAddress/1024;
		int pageOffset = virtualAddress%1024;
		int physicalAddress;
		//checks if the tlb has the physical address
		if(tlb[0][0] == virtualPage) {
			physicalAddress = tlb[0][1] * 1024 + pageOffset;
			return memory[physicalAddress];
		}
		else if(tlb[1][0] == virtualPage) {
			physicalAddress = tlb[1][1] * 1024 + pageOffset;
			return memory[physicalAddress];
		}
		else {
			//calls get mapping if virtual page is not in the tlb
			OS.GetMapping(virtualPage);
			//checks which one of the two has the virtual page
			if(tlb[0][0] == virtualPage) {
				physicalAddress = tlb[0][1] * 1024 + pageOffset;
				return memory[physicalAddress];
			}
			else if(tlb[1][0] == virtualPage) {
				physicalAddress = tlb[1][1] * 1024 + pageOffset;
				return memory[physicalAddress];
			}
		return -1;
		}
	}
	
	public void Write(int address, byte value) {
		int virtualAddress = address;
		int virtualPage = virtualAddress/1024;
		int pageOffset = virtualAddress%1024;
		int physicalAddress;
		//does the same as read but uses the physical address to set a value
		if(tlb[0][0] == virtualPage) {
			physicalAddress = tlb[0][1] * 1024 + pageOffset;
			memory[physicalAddress] = value;
		}
		else if(tlb[1][0] == virtualPage) {
			physicalAddress = tlb[1][1] * 1024 + pageOffset;
			memory[physicalAddress] = value;
		}
		else {
			OS.GetMapping(virtualPage);
			if(tlb[0][0] == virtualPage) {
				physicalAddress = tlb[0][1] * 1024 + pageOffset;
				memory[physicalAddress] = value;
			}
			else if(tlb[1][0] == virtualPage) {
				physicalAddress = tlb[1][1] * 1024 + pageOffset;
				memory[physicalAddress] = value;
			}
		}
	}
	
	//clears tlb to default values
	public void TLBClear() {
		tlb[0][0] = 0;
		tlb[0][1] = 0;
		tlb[1][0] = 0;
		tlb[1][1] = 0;
	}
	
	//mutator method for the tlb
	public void setTLB(int integer, int virtualPage, int physicalPage) {
		tlb[integer][0] = virtualPage;
		tlb[integer][1] = physicalPage;
	}
}
