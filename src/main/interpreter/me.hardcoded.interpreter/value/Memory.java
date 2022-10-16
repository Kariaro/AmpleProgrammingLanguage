package me.hardcoded.interpreter.value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Memory {
	private final Map<Long, Value.ArrayValue> allocatedMemory;
	private final LinkedList<Long> availableMemory;
	private long nextAllocated;
	
	public Memory() {
		this.allocatedMemory = new HashMap<>();
		this.availableMemory = new LinkedList<>();
		
		// Allocate nullptr
		allocate(0);
	}
	
	public Value.ArrayValue allocate(int size) {
		long idx;
		if (availableMemory.isEmpty()) {
			idx = nextAllocated++;
		} else {
			idx = availableMemory.removeFirst();
		}
		Value.ArrayValue value = new Value.ArrayValue(idx << 32, size);
		allocatedMemory.put(idx, value);
		return value;
	}
	
	public Value.ArrayValue allocateString(String string) {
		long idx;
		if (availableMemory.isEmpty()) {
			idx = nextAllocated++;
		} else {
			idx = availableMemory.removeFirst();
		}
		Value.StringValue value = new Value.StringValue(idx << 32, string);
		allocatedMemory.put(idx, value);
		return value;
	}
	
	public void deallocate(long address) {
		if ((address >> 32) == 0) {
			// nullptr
			return;
		}
		
		allocatedMemory.remove(address);
		availableMemory.push(address >> 32);
	}
	
	public Value.ArrayValue getAllocated(long address) {
		Value.ArrayValue arrayValue = allocatedMemory.get(address >> 32);
		if (arrayValue != null && (int) address != 0) {
			return new Value.OffsetArrayValue(arrayValue, (int) address);
		}
		return arrayValue;
	}
}
