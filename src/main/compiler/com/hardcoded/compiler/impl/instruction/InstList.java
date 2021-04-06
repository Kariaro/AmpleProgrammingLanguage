package com.hardcoded.compiler.impl.instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * An instruction list
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class InstList {
	private List<Inst> list;
	
	protected InstList() {
		this.list = new ArrayList<>();
	}
	
	public int size() {
		return list.size();
	}
	
	public Inst get(int index) {
		return list.get(index);
	}
	
	public void add(Inst inst) {
		System.out.printf("%08x: %s\n", hashCode(), inst);
		list.add(inst);
	}
	
	public void add(InstList list) {
		this.list.addAll(list.list);
	}
	
	public void remove(int index) {
		list.remove(index);
	}
	
	public List<Inst> list() {
		return list;
	}
	
	public static InstList get() {
		return new InstList();
	}
}
