
public class VirtualFileSystem implements Device{
	Device[] deviceArray = new Device[10];
	int[] intArray = new int[10];
	
	@Override
	public int Open(String s) {
		//splits the string based on spaces
		String[] split = s.split(" "); 
		//checks for the device type
		if(split[0] == "random") {
			//loops until they find an empty spot
			for(int i = 0; i < 10; i++) {
				if(deviceArray[i] == null) {
					deviceArray[i] = new RandomDevice();
					//check for random seed
					if(split.length > 1) {
						intArray[i] = deviceArray[i].Open(split[1]);
					}
					else {
						intArray[i] = deviceArray[i].Open("");
					}
					return i;
				}
			}
		}
		
		else if(split[0] == "file") {
			//loops until they find an empty spot
			for(int i = 0; i < 10; i++) {
				if(deviceArray[i] == null) {
					deviceArray[i] = new FakeFileSystem();
					if(split.length > 1) {
						intArray[i] = deviceArray[i].Open(split[1]);
					}
					else {
						intArray[i] = deviceArray[i].Open("");
					}
					return i;
				}
			}
		}
		else {
			//case for if the first word of the input string is not a recognized device
			return -1;
		}
		//corner case for full device array
		throw new RuntimeException("Array of device is full");	
	}

	@Override
	public void Close(int id) {
		//calls the device close then sets the entries to null/0
		deviceArray[id].Close(intArray[id]);
		deviceArray[id] = null;
		intArray[id] = 0;
	}

	@Override
	public byte[] Read(int id, int size) {
		//calls the device read
		return deviceArray[id].Read(intArray[id], size);
	}

	@Override
	public void Seek(int id, int to) {
		//calls the device seek
		deviceArray[id].Seek(intArray[id], to);
	}

	@Override
	public int Write(int id, byte[] data) {
		//calls the device write
		return deviceArray[id].Write(intArray[id], data);
	}

}
