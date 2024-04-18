
public class KernelMessage {
	private int senderPID, targetPID, messageType;
	private byte[] data;
	
	//normal constructor for KernelMessage
	public KernelMessage(int sender, int target, int type, byte[] data) {
		this.senderPID = sender;
		this.targetPID = target;
		this.messageType = type;
		this.data = data;
	}
	
	//copy constructor for KernelMessage
	public KernelMessage(KernelMessage message) {
		senderPID = message.getSenderPID();
		targetPID = message.getTargetPID();
		messageType = message.getMessageType();
		data = message.getData();
	}
	
	//accessor method for sender pid
	public int getSenderPID() {
		return senderPID;
	}
	
	//accessor method for target pid
	public int getTargetPID() {
		return targetPID;
	}

	//accessor method for message type (what)
	public int getMessageType() {
		return messageType;
	}
	
	//accessor method for data
	public byte[] getData() {
		return data;
	}
	
	//mutator method for sender pid (used for security reasons in OS)
	public void setSenderPID(int pid) {
		senderPID = pid;
	}
}
