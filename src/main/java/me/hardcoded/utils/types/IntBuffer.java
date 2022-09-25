package me.hardcoded.utils.types;

import java.util.Arrays;

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
public class IntBuffer {
	private final int[] array;
	private int index;
	
	public IntBuffer(int capacity) {
		this.array = new int[capacity];
	}
	
	public int length() {
		return index;
	}
	
	public void write(long value) {
		array[index++] = (int) (value & 0xffffffff);
	}
	
	public void write(IntBuffer buffer) {
		for (int i = 0; i < buffer.index; i++) {
			array[index++] = buffer.array[i];
		}
	}
	
	public void writeOffset(long value, int offset) {
		array[index + offset] = (int) (value & 0xffffffff);
	}
	
	/**
	 * Returns the value of the array at the current {@code index} pointer.
	 *
	 * @param    offset    the offset from the {@code index} pointer
	 * @return the value of the array at the current {@code index} pointer
	 */
	public int read(int offset) {
		return array[index + offset];
	}
	
	public void write(int[] array) {
		for (int i : array) {
			write(i);
		}
	}
	
	public void reset() {
		index = 0;
	}
	
	public int[] toArray() {
		return Arrays.copyOf(array, index);
	}
	
	@Override
	public String toString() {
		int[] array = this.array;
		int index = this.index;
		
		if (index == 0)
			return "IntBuffer {}";
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < index; i++) {
			sb.append(", 0x%02X".formatted(array[i]));
		}
		
		return "IntBuffer { %s }".formatted(sb.toString().substring(2));
	}
}
