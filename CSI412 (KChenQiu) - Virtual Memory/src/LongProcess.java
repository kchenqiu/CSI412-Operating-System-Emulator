
public class LongProcess extends UserlandProcess{

	public void main(){
		while(true) {
			for(int i = 0; i < 10; i++) {
				System.out.println("Doing something " + i);
				try {
					Thread.sleep(i*50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				cooperate();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
