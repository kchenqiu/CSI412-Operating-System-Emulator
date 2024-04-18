
public class PagingProcess extends UserlandProcess{

	public void main() {
		int i = 1;
		byte data = 1;
		while(true) {
			if(i == 100) {
				System.out.println("Killing Paging Process");
				break;
			}
			
			try {
				i+= (OS.AllocateMemory(1024)/1024);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			Write(i, data);
			
			System.out.println("Total: " + i + " Reading: " + Read((i*1024)));
			

			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
