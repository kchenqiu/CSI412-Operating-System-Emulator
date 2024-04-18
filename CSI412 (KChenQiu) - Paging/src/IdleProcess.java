
public class IdleProcess extends UserlandProcess{
	
	public void main() {
		//infinite loop of sleep and cooperate()
		while(true) {
			try {
			    Thread.sleep(50); // sleep for 50 ms
			} catch (Exception e) { }
			try {
				super.cooperate();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
