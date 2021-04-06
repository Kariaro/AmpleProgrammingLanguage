package com.hardcoded.compiler.impl.instruction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.hardcoded.compiler.impl.context.Reference;

public class InstParam {
	private static final DecimalFormat number_format = new DecimalFormat("#.##########", DecimalFormatSymbols.getInstance(Locale.US));
	public static final InstParam EMPTY = new InstParam();
	
	enum Type {
		EMPTY,
		REF,
		STR,
		NUM
	}
	
	protected Reference ref;
	protected String string;
	protected double number;
	protected Type type;
	
	private InstParam() {
		this.type = Type.EMPTY;
	}
	
	private InstParam(String string) {
		this.type = Type.STR;
		this.string = string;
	}
	
	private InstParam(double number) {
		this.type = Type.NUM;
		this.number = number;
	}
	
	private InstParam(Reference ref) {
		this.type = Type.REF;
		this.ref = ref;
	}
	
	public boolean isEmpty() {
		return type == Type.EMPTY;
	}
	
	@Override
	public String toString() {
		switch(type) {
			case NUM: return number_format.format(number);
			case REF: return ref.toString();
			case STR: return '"' + string + '"';
			case EMPTY: return "<empty>";
		}
		
		return String.format("<undefined> (%s, %s, %s)", string, number, ref);
	}
	
	public static InstParam get(String string) {
		return new InstParam(string);
	}
	
	public static InstParam get(double number) {
		return new InstParam(number);
	}
	
	public static InstParam get(Reference ref) {
		return new InstParam(ref);
	}
}
