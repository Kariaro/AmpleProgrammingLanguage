package me.hardcoded.vm;

/**
 * This buffer stream outputs only to the default output in sequential order.
 * 
 * @author HardCoded
 */
public class StdAmpleBufferStream extends AmpleBufferStream {
	private volatile int currentIndex = 0;
	
	@Override
	public synchronized void write(int b) {
		System.out.print((char)b);
		if(currentIndex < buffer.length) {
			buffer[currentIndex++] = (char)b;
		}
	}
	
	@Override
	public synchronized void write(int index, char c) {
		System.out.print(c);
		if(currentIndex < buffer.length) {
			buffer[currentIndex++] = c;
		}
	}
	
	@Override
	public synchronized int getLength() {
		return currentIndex;
	}
	
	@Override
	public synchronized String getBuffer() {
		return new String(buffer, 0, currentIndex);
	}
}
