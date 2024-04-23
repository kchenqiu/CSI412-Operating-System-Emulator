
public class Ping extends UserlandProcess{
	
	public void main() {					
		int pid = 0;
		int pongPid = 0;
		int counter = 0;
		KernelMessage recievedMessage = null;
		
		//setup for creating ping
		try {
			pid = OS.GetPID();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.print("I am PING: " + pid);
		
		//set up for pong if it is not created
		try {
			if(OS.GetPIDByName("Pong") == -1) {
				pongPid = OS.CreateProcess(new Pong());
			}
			else {
				pongPid = OS.GetPIDByName("Pong");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(", pong = " + pongPid);
		
		while(true) {		
			//sends a message to pong
			try {
				OS.SendMessage(new KernelMessage(pid, OS.GetPIDByName("Pong"), counter, null));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			//recieves the message from pong after it runs
			try {
				recievedMessage = OS.WaitForMessage();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(recievedMessage == null) {
				try {
					recievedMessage = OS.WaitForMessage();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//prints out the message
			System.out.println("PING: from " + recievedMessage.getSenderPID() + " to " + 
								recievedMessage.getTargetPID() + "what: " + recievedMessage.getMessageType());

			//increments what
			counter++;
		}
	}
}
