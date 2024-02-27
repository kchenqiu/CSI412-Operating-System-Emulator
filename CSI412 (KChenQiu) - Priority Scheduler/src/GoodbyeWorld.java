
public class GoodbyeWorld extends UserlandProcess{
	
	public void main() {
		//infinite loop of printing "Goodbye World" and cooperate();
		while(true) {		
			System.out.println("Goodbye World");
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
