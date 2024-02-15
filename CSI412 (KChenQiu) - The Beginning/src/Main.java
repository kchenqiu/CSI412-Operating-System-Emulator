
public class Main {
	public static void main(String[] args) throws InterruptedException {
		OS.Startup(new HelloWorld());
		OS.CreateProcess(new GoodbyeWorld());
	}
}
