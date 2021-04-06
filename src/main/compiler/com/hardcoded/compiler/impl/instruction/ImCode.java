package com.hardcoded.compiler.impl.instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * An intermediate code bundle
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ImCode {
	public enum Type {
		CLASS,
		FUNCTION
	}
	
	protected List<InstList> list;
	
	public ImCode() {
		list = new ArrayList<>();
	}
	
	public List<InstList> list() {
		return list;
	}
	
	public void push(InstList list) {
		this.list.add(list);
	}
}
