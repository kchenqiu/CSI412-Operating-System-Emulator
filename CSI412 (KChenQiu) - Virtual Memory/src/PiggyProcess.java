import java.util.Random;

public class PiggyProcess extends UserlandProcess{
	private Random random = new Random();
	private static int numb = 0;
	private int currentNumb;
	
	//accessing random memory
	public void main() {
		currentNumb = numb;
		numb++;
		try {
			OS.AllocateMemory(100*1024);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true) {
			try {
				Thread.sleep(50);			
				cooperate();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int rand = random.nextInt(1024*1024);
			System.out.println("Process :" + currentNumb + " Address: " + rand + " Byte Read: " + Read(rand));

		}
	}
}
