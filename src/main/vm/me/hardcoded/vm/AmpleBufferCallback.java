package me.hardcoded.vm;

@FunctionalInterface
public interface AmpleBufferCallback {
	int BUFFER_CLOSED = 1;
	int BUFFER_CHANGED = 2;
	
	void bufferChanged(AmpleBufferStream stream, int type);
}
