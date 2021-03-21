package com.hardcoded.compiler.impl.expression;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.hardcoded.compiler.api.AtomType;
import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.impl.context.Reference;

/**
 * A atom expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AtomExpr implements Expression {
	protected AtomType type;
	protected double number = 0d;
	protected String string = "";
	protected Reference ref;
	
	private AtomExpr(double value) {
		this.number = value;
		this.type = AtomType.number;
	}
	
	private AtomExpr(String value) {
		this.string = value;
		this.type = AtomType.string;
	}
	
	private AtomExpr(Reference ref) {
		this.ref = ref;
		this.type = AtomType.ref;
	}
	
	@Override
	public Type getType() {
		return Type.ATOM;
	}
	
	@Override
	public List<Expression> getExpressions() {
		return Collections.emptyList();
	}
	
	public boolean isReference() {
		return type == AtomType.ref;
	}
	
	public boolean isNumber() {
		return type == AtomType.number;
	}
	
	public boolean isString() {
		return type == AtomType.string;
	}
	
	@Override
	public String toString() {
		switch(type) {
			case number: return Double.toString(number);
			case string: return '"' + string + '"';
			case ref: return Objects.toString(ref);
			default: throw new IllegalStateException("Bad AtomExpr missing type");
		}
	}
	
	public static AtomExpr get(double value) {
		return new AtomExpr(value);
	}
	
	public static AtomExpr get(String string) {
		return new AtomExpr(string);
	}
	
	public static AtomExpr get(Reference ref) {
		return new AtomExpr(ref);
	}
}
