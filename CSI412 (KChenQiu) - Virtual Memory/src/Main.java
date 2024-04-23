
public class Main {
	public static void main(String[] args) throws InterruptedException {
		OS.Startup(new HelloWorld());
		OS.CreateProcess(new GoodbyeWorld());	
		
		//OS.Open("random");		
		//OS.Open("file TestFile");
		//use this to test file unrecognized exception
		//OS.Open("file");
		
		//OS.CreateProcess(new LongProcess(), OS.Priority.High);
		//OS.CreateProcess(new SleepProcess(), OS.Priority.High);
		//OS.CreateProcess(new LongProcess(), OS.Priority.Low);
		
		//creating a new ping also creates a new pong and vice versa
		//OS.CreateProcess(new Ping());

		//test for paging
		//OS.CreateProcess(new PagingProcess());
		
		//test for virtual memory
		for(int i = 0; i < 20; i++) {
			OS.CreateProcess(new PiggyProcess());
		}
	}
}
