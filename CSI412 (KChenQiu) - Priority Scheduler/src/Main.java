
public class Main {
	public static void main(String[] args) throws InterruptedException {
		OS.Startup(new HelloWorld());
		OS.CreateProcess(new GoodbyeWorld());		
		//OS.CreateProcess(new LongProcess(), OS.Priority.High);
		//OS.CreateProcess(new SleepProcess(), OS.Priority.High);
		//OS.CreateProcess(new LongProcess(), OS.Priority.Low);
	}
}
