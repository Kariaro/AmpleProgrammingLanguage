package me.hardcoded.compiler.parser.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructData {
	private final Map<String, ValueType> memberTypes;
	private final List<Map.Entry<String, ValueType>> memberOrdered;
	private final String name;
	
	public StructData(String name) {
		this.name = name;
		this.memberTypes = new HashMap<>();
		this.memberOrdered = new ArrayList<>();
	}
	
	public void addMember(ValueType type, String name) {
		memberTypes.put(name, type);
		memberOrdered.add(Map.entry(name, type));
	}
	
	public boolean hasMember(String name) {
		return memberTypes.containsKey(name);
	}
	
	public ValueType getMember(String name) {
		return memberTypes.get(name);
	}
	
	public List<Map.Entry<String, ValueType>> getMembers() {
		return memberOrdered;
	}
	
	public String getName() {
		return name;
	}
	
	public int getMemberIndex(String name) {
		final int size = memberOrdered.size();
		for (int i = 0; i < size; i++) {
			if (memberOrdered.get(i).getKey().equals(name)) {
				return i;
			}
		}
		return -1;
	}
}
