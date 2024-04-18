import java.util.LinkedList;

public class PCB {
	private UserlandProcess userlandProcess;
	private static int nextPID;
	private int pid;
	private int[] intArray = new int[10];
	private int[] pageArray = new int[100];
	private String name;
	private LinkedList<KernelMessage> messageQueue;
	public int demotionCounter;
	
	//constructor sets the ulp, pid and int array
	public PCB(UserlandProcess up) {
		userlandProcess = up;
		name = up.getClass().getSimpleName();
		pid = nextPID;
		nextPID++;
		for(int i = 0 ; i < 10; i++) {
			intArray[i] = -1;
		}
		for(int i = 0; i < 100; i++) {
			pageArray[i] = -1;
		}
		messageQueue = new LinkedList<KernelMessage>();
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
	
	public void setPage(int value) {
		for(int i = 0; i < 100; i++) {
			if(pageArray[i] == -1){
				pageArray[i] = value;
				break;
			}
		}
	}
	
	public void setTargetPage(int target, int value) {
		pageArray[target] = value;
	}
	
	public int getPage(int i) {
		return pageArray[i];
	}
	
	//accessor method for name
	public String getName() {
		return name;
	}
	
	//mutator method to add kernel message to queue
	public void addMessage(KernelMessage km) {
		messageQueue.add(km);
	}
	
	//method to remove message after it has been read
	public void removeMessage() {
		messageQueue.remove();
	}
	
	//accessor method for message
	public KernelMessage getMessage() {
		if(!messageQueue.isEmpty()) {
			return messageQueue.getFirst();
		}
		else {
			return null;
		}
	}
	
	//calls stop until their is no more semaphores to acquire
	public void stop() throws InterruptedException {
		do {
			userlandProcess.stop();
			Thread.sleep(20);
		}while(!userlandProcess.isStopped());
	}
	
	//returns ulp isDone()
	public boolean isDone() {
		return userlandProcess.isDone();
	}
	
	//calls ulp start()
	public void run() {
		userlandProcess.start();		
	}
}
