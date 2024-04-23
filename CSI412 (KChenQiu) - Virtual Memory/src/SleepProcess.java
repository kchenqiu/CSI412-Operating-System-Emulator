
public class SleepProcess extends UserlandProcess{

	public void main(){
		while(true) {
			System.out.println("Sleeping");
			try {
				OS.Sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Waking up");
			try {
				cooperate();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
