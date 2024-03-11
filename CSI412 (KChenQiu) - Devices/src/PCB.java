
public class PCB {
	private UserlandProcess userlandProcess;
	private static int nextPID;
	private int pid;
	private int[] intArray = new int[10];
	public int demotionCounter;
	
	//constructor sets the ulp, pid and int array
	public PCB(UserlandProcess up) {
		userlandProcess = up;
		pid = nextPID;
		nextPID++;
		for(int i = 0 ; i < 10; i++) {
			intArray[i] = -1;
		}
	}
	
	//accessor method for ulp
	public UserlandProcess getUserlandProcess() {
		return userlandProcess;
	}
	
	//accessor method for pid
	public int getPID() {
		return pid;
	}
	
	//mutator method for int array
	public void setInt(int target, int set) {
		intArray[target] = set;
	}
	
	//accessor method for int array
	public int getInt(int i) {
		return intArray[i];
	}
	
	public void stop() throws InterruptedException {
		do {
			userlandProcess.stop();
			Thread.sleep(20);
		}while(!userlandProcess.isStopped());
	}
	
	public boolean isDone() {
		return userlandProcess.isDone();
	}
	
	public void run() {
		userlandProcess.start();
	}
}
