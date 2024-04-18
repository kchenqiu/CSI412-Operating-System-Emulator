
public class Pong extends UserlandProcess{

	public void main() {
		int pid = 0;
		int pingPid = 0;
		int counter = 0;
		KernelMessage recievedMessage = null;
		
		//set up for pong
		try {
			pid = OS.GetPID();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("I am Pong: " + pid);
		
		//set up for ping if it is not created
		try {
			if(OS.GetPIDByName("Ping") == -1) {
				pingPid = OS.CreateProcess(new Pong());
			}
			else {
				pingPid = OS.GetPIDByName("Ping");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(", ping = " + pingPid);
		
		while(true) {			
			//sends message to ping
			try {
				OS.SendMessage(new KernelMessage(pid, OS.GetPIDByName("Ping"), counter, null));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			

			//recieves message from ping after it runs
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
			System.out.println("PONG: from " + recievedMessage.getSenderPID() + " to " + 
								recievedMessage.getTargetPID() + "what: " + recievedMessage.getMessageType());	

			//increments what
			counter++;
		}
		
	}

}
