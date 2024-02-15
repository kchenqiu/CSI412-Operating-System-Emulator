import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
	private LinkedList<UserlandProcess> userProcess = new LinkedList<UserlandProcess>();
	private static Timer timer;
	public UserlandProcess current;
	
	//constructor that schedules the interrupts 
	public Scheduler() {
		timer = new Timer("TIMER");
		TimerTask interrupt = new TimerTask() {
			public void run() {
				if(current != null) 
				{
				current.requestStop();
				}
			}
		};
		timer.scheduleAtFixedRate(interrupt, 250, 250);
	}
	
	//adds UserlandProcess to LL of processes
	//calls SwitchProcess if nothing is running
	public int CreateProcess(UserlandProcess up) {
		userProcess.add(up);		
		if(current == null) {
			SwitchProcess();
		}
		int pid = (int)(current.userThread.getId());
		return pid;
	}
	
	//takes the current process and moves it to the back of the list
	//sets the process at the beginning of the list to current
	public void SwitchProcess() {

		if(current == null) {
			current = userProcess.getFirst();
		}
		else {
			userProcess.removeFirst();
			if(current.isDone());
			else {
				userProcess.add(current);
			}
			current = userProcess.getFirst();		
		}
	}
}
