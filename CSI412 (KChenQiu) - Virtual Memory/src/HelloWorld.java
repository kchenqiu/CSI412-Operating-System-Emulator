
public class HelloWorld extends UserlandProcess{
	
	public void main() {
		
		while(true) {		
			//infinite loop of printing "Hello World" and cooperate();
			System.out.println("Hello World");
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
