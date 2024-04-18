import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device{
	RandomAccessFile[] fileArray = new RandomAccessFile[10];
	
	@Override
	public int Open(String s) {
		//checks for null or empty string
		if(s.equals(null) || s.equals("")) {
			throw new RuntimeException("Unrecognized file name");
		}
		else {
			//loops till there is an empty spot
			for(int i = 0; i < 10; i++) {
				if(fileArray[i] == null) {
					try {
						//uses random access file to open the file
						fileArray[i] = new RandomAccessFile(s, "rwd");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return i;
				}
			}
		}
		throw new RuntimeException("File not found or Array of file devices are full");
	}

	@Override
	public void Close(int id) {
		//closes the file then sets it to null
		try {
			fileArray[id].close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileArray[id] = null;
		
	}

	@Override
	public byte[] Read(int id, int size) {
		//reads each individual byte and saves it to an array
		byte[] readArray = new byte[size];
		for(int i = 0; i < size; i++) {
			try {
				readArray[i] = fileArray[id].readByte();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return readArray;
	}

	@Override
	public void Seek(int id, int to) {
		//uses built in seek method
		try {
			fileArray[id].seek((long)(to));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int Write(int id, byte[] data) {
		try {
			//uses built in write method
			fileArray[id].write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
}
