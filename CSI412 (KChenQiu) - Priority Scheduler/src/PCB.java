
public class PCB {
	private UserlandProcess userlandProcess;
	private static int nextPID;
	private int pid;
	public int demotionCounter;
	
	public PCB(UserlandProcess up) {
		userlandProcess = up;
		pid = nextPID;
		nextPID++;
	}
	
	public UserlandProcess getUserlandProcess() {
		return userlandProcess;
	}
	
	public int getPID() {
		return pid;
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
