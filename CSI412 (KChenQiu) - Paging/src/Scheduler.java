import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
	private LinkedList<PCB> highPriorityProcess = new LinkedList<PCB>();
	private LinkedList<PCB> normalPriorityProcess = new LinkedList<PCB>();
	private LinkedList<PCB> lowPriorityProcess = new LinkedList<PCB>();
	private LinkedList<PCB> asleepProcess = new LinkedList<PCB>();
	private LinkedList<OS.Priority> asleepPriority = new LinkedList<OS.Priority>();
	private LinkedList<Instant>awakeTime = new LinkedList<Instant>();
	private HashMap<Integer, PCB> processHash = new HashMap<Integer, PCB>();
	private HashMap<Integer, PCB> waitForMessageProcess = new HashMap<Integer, PCB>();
	private HashMap<Integer, OS.Priority> waitForMessagePriority = new HashMap<Integer, OS.Priority>();
	private static Timer timer;
	private Random random = new Random();
	private Clock clock = Clock.systemDefaultZone();
	private Instant clockInstant = clock.instant();
	private PCB current;
	private PCB previous;
	private Kernel kernel;
	
	//constructor that schedules the interrupts 
	public Scheduler() {	

		timer = new Timer("TIMER", true);
		TimerTask interrupt = new TimerTask() {
			public void run() {
				current.getUserlandProcess().requestStop();
				current.demotionCounter++;
			}
		};
		timer.schedule(interrupt, 250, 250);
	}
	
	//adds UserlandProcess to LL of processes
	//calls SwitchProcess if nothing is running
	//old CreateProcess (defaults to normal priority)
	public int CreateProcess(PCB up) {
		normalPriorityProcess.add(up);		
		if(current == null) {
			try {
				SwitchProcess();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int pid = up.getPID();		
		processHash.put(pid, up);
		return pid;
	}
	
	//overloaded CreateProcess method with a priority 
	//doesnt call switch process because startup will always use old CreateProcess
	public int CreateProcess(PCB up, OS.Priority prio) {
		int pid = 0;
		switch(prio) {
			case High: 
				highPriorityProcess.add(up);		
				pid = up.getPID();
				break; 
				
			case Normal:
				normalPriorityProcess.add(up);		
				pid = up.getPID();
				break;
				
			case Low:
				lowPriorityProcess.add(up);		
				pid = up.getPID();
				break;
		}				
		processHash.put(pid, up);
		return pid;
	}
	

	public void SwitchProcess() throws InterruptedException {
		//startup condition
		if(current == null) {
			current = normalPriorityProcess.getFirst();
		}
		
		else {
			//check which process current is in 
			//checks if it should be returned to the end of the list 
			if(!normalPriorityProcess.isEmpty() && current == normalPriorityProcess.getFirst()) {
				normalPriorityProcess.removeFirst();
				if(current.isDone()) {
					processHash.remove(current.getPID());
					for(int i = 0; i < 10; i++) {
						if(current.getInt(i) != -1) {
							kernel.Close(current.getInt(i));
						}
					}			
					
					for(int i = 0; i < 100; i++) {
						if(current.getPage(i) != -1) {
							OS.FreeMemory(current.getPage(i), 1024);
						}
					}
				}
				else {
					normalPriorityProcess.add(current);
				}
			}
			else if(!highPriorityProcess.isEmpty() && current == highPriorityProcess.getFirst()) {
				highPriorityProcess.removeFirst();
				if(current.isDone()) {
					processHash.remove(current.getPID());
					for(int i = 0; i < 10; i++) {
						if(current.getInt(i) != -1) {
							kernel.Close(current.getInt(i));
						}
					}			
					for(int i = 0; i < 100; i++) {
						if(current.getPage(i) != -1) {
							OS.FreeMemory(current.getPage(i), 1024);
						}
					}
				}
				else {
					highPriorityProcess.add(current);
				}
			}
			else if(!lowPriorityProcess.isEmpty() && current == lowPriorityProcess.getFirst()) {
				lowPriorityProcess.removeFirst();
				if(current.isDone()) {
					processHash.remove(current.getPID());
					for(int i = 0; i < 10; i++) {
						if(current.getInt(i) != -1) {
							kernel.Close(current.getInt(i));
						}
					}			
					for(int i = 0; i < 100; i++) {
						if(current.getPage(i) != -1) {
							OS.FreeMemory(current.getPage(i), 1024);
						}
					}
				}
				else {
					lowPriorityProcess.add(current);
				}
			}
			
			//sets variable to check for demotion
			previous = current;
			
			//calls helper method to see if any process needs to be awake
			if(Awake()) {
				return;
			}
			
			//random int to choose which priority to choose from
			int randInt = random.nextInt(10);
			
			//adds the first on the list
			//moves to lower priority if the list is empty
			switch(randInt) {
			case 0, 1, 2, 3, 4:
				if(!highPriorityProcess.isEmpty()) {
					current = highPriorityProcess.getFirst();
					break;
				}
				else if(!normalPriorityProcess.isEmpty()) {
					current = normalPriorityProcess.getFirst();
					break;
				}
				else {
					current = lowPriorityProcess.getFirst();
					break;
				}
			case 5, 6, 7:
				if(!normalPriorityProcess.isEmpty()) {
					current = normalPriorityProcess.getFirst();
					break;
				}
				else if(!highPriorityProcess.isEmpty()) {
					current = highPriorityProcess.getFirst();
					break;
				}
				else {
					current = lowPriorityProcess.getFirst();
					break;
				}	
			case 8, 9:
				if(!lowPriorityProcess.isEmpty()) {
					current = lowPriorityProcess.getFirst();
					break;
				}
				else if(!highPriorityProcess.isEmpty()) {
					current = highPriorityProcess.getFirst();
					break;
				}
				else {
					current = normalPriorityProcess.getFirst();
					break;
				}
			}
			
			//calls demotion helper method
			Demote(previous);
		}
	}
	
	
	public void Sleep(int milliseconds) {
		//adds the process into a new LL
		asleepProcess.add(current);
		
		//saves the information needed to awake the process
		awakeTime.add(clockInstant.plusMillis(milliseconds));
		
		//checks which priority the list needs to be removed from
		if(!highPriorityProcess.isEmpty() && current == highPriorityProcess.getFirst()) {
			asleepPriority.add(OS.Priority.High);
			highPriorityProcess.removeFirst();
			
			//puts a process onto current from the same priority
			//if LL is empty move onto a lower priority
			if(!highPriorityProcess.isEmpty()) {
				current = highPriorityProcess.getFirst();
			}
			else if(!normalPriorityProcess.isEmpty()) {
				current = normalPriorityProcess.getFirst();
			}
			else {
				current = lowPriorityProcess.getFirst();
			}
		}
		
		else if(!normalPriorityProcess.isEmpty() && current == normalPriorityProcess.getFirst()) {
			asleepPriority.add(OS.Priority.Normal);
			normalPriorityProcess.removeFirst();		
			
			//puts a process onto current from the same priority
			//if LL is empty move onto a lower priority
			if(!normalPriorityProcess.isEmpty()) {
				current = normalPriorityProcess.getFirst();
			}
			else if(!highPriorityProcess.isEmpty()) {
				current = highPriorityProcess.getFirst();
			}
			else {
				current = lowPriorityProcess.getFirst();
			}
		}
		else if(!lowPriorityProcess.isEmpty() && current == lowPriorityProcess.getFirst()) {
			asleepPriority.add(OS.Priority.Low);
			lowPriorityProcess.removeFirst();
			
			//puts a process onto current from the same priority
			//if LL is empty move onto a lower priority
			if(!lowPriorityProcess.isEmpty()) {
				current = lowPriorityProcess.getFirst();
			}
			else if(!highPriorityProcess.isEmpty()) {
				current = highPriorityProcess.getFirst();
			}
			else {
				current = normalPriorityProcess.getFirst();
			}
		}

	}
	
	public PCB getCurrentlyRunning() {
		return current;
	}
	
	public int getPID() {
		return current.getPID();
	}
	
	public int getPIDByName(String name) {
		for(int i = 0; i < highPriorityProcess.size(); i++) {
			if(highPriorityProcess.get(i).getName().equals(name)) {
				return highPriorityProcess.get(i).getPID();
			}
		}
		
		for(int i = 0; i < normalPriorityProcess.size(); i++) {
			if(normalPriorityProcess.get(i).getName().equals(name)) {
				return normalPriorityProcess.get(i).getPID();
			}
		}
		
		for(int i = 0; i < lowPriorityProcess.size(); i++) {
			if(lowPriorityProcess.get(i).getName().equals(name)) {
				return lowPriorityProcess.get(i).getPID();
			}
		}
		for(int i = 0; i < asleepProcess.size(); i++) {
			if(asleepProcess.get(i).getName().equals(name)) {
				return asleepProcess.get(i).getPID();
			}
		}
		
		return -1;
	}
	
	//looks for km in the hash using the pid
	public void SendMessage(KernelMessage km) {
		processHash.get(km.getTargetPID()).addMessage(km);
		
		//adds the process that was waiting for this message back onto the list
		if(waitForMessageProcess.get(km.getTargetPID()) != null) {
			current = waitForMessageProcess.get(km.getTargetPID());
			waitForMessageProcess.remove(current.getPID());
			if(waitForMessagePriority.get(current.getPID()).equals(OS.Priority.High)) {
				highPriorityProcess.addFirst(current);
			}
			else if(waitForMessagePriority.get(current.getPID()).equals(OS.Priority.Normal)) {
				normalPriorityProcess.addFirst(current);
			}
			else {
				lowPriorityProcess.addFirst(current);
			}
		}
	}
	

	public KernelMessage WaitForMessage() {
		KernelMessage message = null;
		if(current.getMessage() != null) {
			message = new KernelMessage(current.getMessage());
			current.removeMessage();
			return message;
		}
		else {
			//adds current into a hash for waiting for message
			waitForMessageProcess.put(current.getPID(), current);
			
			//checks each priority queue and removes current
			if(!highPriorityProcess.isEmpty() && current == highPriorityProcess.getFirst()) {
				waitForMessagePriority.put(current.getPID(), OS.Priority.High);
				highPriorityProcess.removeFirst();
				
				//puts a process onto current from the same priority
				//if LL is empty move onto a lower priority
				if(!highPriorityProcess.isEmpty()) {
					current = highPriorityProcess.getFirst();
				}
				else if(!normalPriorityProcess.isEmpty()) {
					current = normalPriorityProcess.getFirst();
				}
				else {
					current = lowPriorityProcess.getFirst();
				}
			}
			
			else if(!normalPriorityProcess.isEmpty() && current == normalPriorityProcess.getFirst()) {
				waitForMessagePriority.put(current.getPID(), OS.Priority.Normal);
				normalPriorityProcess.removeFirst();		
				
				//puts a process onto current from the same priority
				//if LL is empty move onto a lower priority
				if(!normalPriorityProcess.isEmpty()) {
					current = normalPriorityProcess.getFirst();
				}
				else if(!highPriorityProcess.isEmpty()) {
					current = highPriorityProcess.getFirst();
				}
				else {
					current = lowPriorityProcess.getFirst();
				}
			}
			else if(!lowPriorityProcess.isEmpty() && current == lowPriorityProcess.getFirst()) {
				waitForMessagePriority.put(current.getPID(), OS.Priority.Low);
				lowPriorityProcess.removeFirst();
				
				//puts a process onto current from the same priority
				//if LL is empty move onto a lower priority
				if(!lowPriorityProcess.isEmpty()) {
					current = lowPriorityProcess.getFirst();
				}
				else if(!highPriorityProcess.isEmpty()) {
					current = highPriorityProcess.getFirst();
				}
				else {
					current = normalPriorityProcess.getFirst();
				}
			}
			
			return null;
		}
	}
	
	
	private void Demote(PCB up) {
		//checks counter in PCB 
		//if it has been running for atleast 5 times in a row then demote
		if(up.demotionCounter >= 5) {
			
			//moves high priority into normal priority and resets counter
			if(!highPriorityProcess.isEmpty() && up.equals(highPriorityProcess.getLast())) {
				highPriorityProcess.removeLast();
				normalPriorityProcess.add(up);
				up.demotionCounter = 0;
			}
			//moves normal priority to low priority and resets counter
			else if(!normalPriorityProcess.isEmpty() && up.equals(normalPriorityProcess.getLast())) {
				normalPriorityProcess.removeLast();
				lowPriorityProcess.add(up);
				up.demotionCounter = 0;
			}
			//cant demote low priority process so just resets counter
			else {
				up.demotionCounter = 0;
			}

		}
		//checks if process is a repeat
		else if(up.getPID() == current.getPID()) {
			up.demotionCounter++;
		}
		//resets counter if it is not repeated
		else {
			up.demotionCounter = 0;
		}
	}
	
	//returns true if there is a process that needs to be awaken
	private boolean Awake() {
		//loops through all the asleep processes
		for(int i = 0; i < asleepProcess.size(); i++) {
			//if the time to wake up has passed then set the asleep process to current and remove from list
			if(awakeTime.get(i).compareTo(clock.instant()) >= 0) {
				current = asleepProcess.get(i);				
				asleepProcess.remove(i);
				//checks which priority list it should go in
				switch(asleepPriority.get(i)) {
				case High:
					highPriorityProcess.add(current);
					break;
					
				case Normal:
					normalPriorityProcess.add(current);
					break;
					
				case Low:
					lowPriorityProcess.add(current);
					break;
				}
				asleepPriority.remove(i);
				awakeTime.remove(i);
				return true;
			}
		}
		return false;
	}
}
