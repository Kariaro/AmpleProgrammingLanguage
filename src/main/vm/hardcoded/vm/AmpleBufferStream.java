package hardcoded.vm;

import java.io.OutputStream;

public class AmpleBufferStream extends OutputStream {
	protected final char[] buffer;
	private volatile int lowestZero = 0;
	private volatile boolean hasChanges;
	
	public AmpleBufferStream() {
		this(0x8000);
	}
	
	public AmpleBufferStream(int capacity) {
		this.buffer = new char[capacity];
	}
	
	public void write(int b) {
		throw new UnsupportedOperationException();
	}
	
	public synchronized void write(int index, char c) {
		buffer[index] = c;
		if(c != 0) {
			if(lowestZero == index) {
				lowestZero = -1;
				for(int i = index + 1; i < buffer.length; i++) {
					if(buffer[i] == 0) {
						lowestZero = i;
						break;
					}
				}
				
				if(lowestZero < 0) {
					lowestZero = buffer.length;
				}
			}
			
			
		} else {
			if(index < lowestZero) {
				lowestZero = index;
			}
		}
		
		hasChanges = true;
	}
	
	public synchronized int getLength() {
		return lowestZero;
	}
	
	public synchronized boolean hasChanges() {
		boolean changes = hasChanges;
		hasChanges = false;
		return changes;
	}
	
	public synchronized String getBuffer() {
		return new String(buffer, 0, getLength());
	}
}
