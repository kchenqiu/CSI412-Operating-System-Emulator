import java.util.ArrayList;

public class OS {
	
	//enum of what function to call
	enum CallType{
		CreateProcess, SwitchProcess
	}
	
	//static instance of enum
	static CallType call;
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
		while(kernel.getScheduler().current == null) {
			Thread.sleep(10);
		}
		kernel.getScheduler().current.stop();
		
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
	
	//Creates the Kernel() and calls CreateProcess twice – once for “init” and once for the idle process.
	public static void Startup(UserlandProcess init) throws InterruptedException {
		kernel = new Kernel();		
		CreateProcess(init);
		CreateProcess(new IdleProcess());				
	}
	
	private static void KernelSwitch() throws InterruptedException {
		kernel.start();
		if(kernel.getScheduler().current != null) {
			kernel.getScheduler().current.stop();
		}
	}
}
