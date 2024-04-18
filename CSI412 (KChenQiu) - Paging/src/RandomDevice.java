import java.util.Random;

public class RandomDevice implements Device{
	Random[] randomArray = new Random[10];
	byte[][] byteArray = new byte[10][];
	
	
	@Override
	public int Open(String s) {
		//goes through the array of random objects
		for(int i = 0; i < 10; i++) {
			//first empty array will be filled
			if(randomArray[i] == null) {
				//checks for the seed
				if(s.equals(null) || s.equals("")) {
					randomArray[i] = new Random();
					return i;
				}
				else {
					randomArray[i] = new Random(Long.parseLong(s));
					return i;
				}
			}
		}
		//corner case where array of object is full
		throw new RuntimeException("Array of random object is full");
	}

	@Override
	public void Close(int id) {
		//sets the device entry to null
		randomArray[id] = null;
	}

	@Override
	public byte[] Read(int id, int size) {
		//creates/fills the array of bits with random values
		byte readArray[] = new byte[size];
		randomArray[id].nextBytes(readArray);		
		byteArray[id] = readArray; 		
		return byteArray[id];
	}

	@Override
	public void Seek(int id, int to) {
		//does read without return bytes
		Read(id, to);
	}

	@Override
	public int Write(int id, byte[] data) {
		//returns 0
		return 0;
	}

}
