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
	
	protected final Reference ref;
	protected final String string;
	protected final double number;
	protected final Type type;
	
	private InstParam() {
		this(Type.EMPTY, null, 0D, null);
	}
	
	private InstParam(String string) {
		this(Type.STR, string, 0D, null);
	}
	
	private InstParam(double number) {
		this(Type.NUM, null, number, null);
	}
	
	private InstParam(Reference ref) {
		this(Type.REF, null, 0D, ref);
	}
	
	private InstParam(Type type, String str, double num, Reference ref) {
		this.type = type;
		this.string = str;
		this.number = num;
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
