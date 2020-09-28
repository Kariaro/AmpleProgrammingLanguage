package hardcoded.utils;

/**
 * Because the standard java library does not include
 * a fast integer array wrapper this class was created.
 * 
 * <p>The {@code IntBuffer} class is a array wrapper
 * that you can write integer values to. After that
 * you can return those integers as a array with {@linkplain #toArray()}.
 * 
 * @author HardCoded
 */
public final class IntBuffer {
	private final int[] array;
	private int index;
	
	public IntBuffer(int capacity) {
		this.array = new int[capacity];
	}
	
	public int length() {
		return index;
	}
	
	public void write(long value) {
		array[index++] = (int)(value & 0xffffffff);
	}
	
	public void writeOffset(long value, int offset) {
		array[index + offset] = (int)(value & 0xffffffff);
	}
	
	/**
	 * Returns the value of the array at the current {@code index} pointer.
	 * @param	offset	the offset from the {@code index} pointer
	 * @return the value of the array at the current {@code index} pointer
	 */
	public int read(int offset) {
		return array[index + offset];
	}
	
	public void write(int[] array) {
		for(int i : array)
			write(i);
	}
	
	public void reset() {
		index = 0;
	}
	
	public int[] toArray() {
		int[] copy = new int[index];
		System.arraycopy(array, 0, copy, 0, index);
		return copy;
	}
	
	public String toString() {
		if(index == 0) return "IntBuffer {}";
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < index; i++) {
			sb.append(", ").append(String.format("0x%02X", array[i]));
		}
		
		return "IntBuffer { " + sb.toString().substring(2) + " }";
	}
}