package com.hardcoded.compiler.impl.expression;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import com.hardcoded.compiler.api.AtomType;
import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.impl.context.IRefContainer;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.lexer.Token;

/**
 * A atom expression
 * 
 * <pre>
 * Valid syntax:
 *   [number]
 *   [string]
 *   [name]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AtomExpr extends Expr implements IRefContainer {
	private static final DecimalFormat number_format = new DecimalFormat("#.##########", DecimalFormatSymbols.getInstance(Locale.US));
	
	protected AtomType type;
	protected double number = 0d;
	protected String string = "";
	protected Reference ref;
	
	private AtomExpr(Token token, double value) {
		super(token, true);
		this.type = AtomType.number;
		this.number = value;
	}
	
	private AtomExpr(Token token, String value) {
		super(token, true);
		this.type = AtomType.string;
		this.string = value;
	}
	
	private AtomExpr(Token token, Reference ref) {
		super(token, true);
		this.type = AtomType.ref;
		this.ref = ref;
	}
	
	private AtomExpr(Token token, AtomType type, double a, String b, Reference c) {
		super(token, true);
		this.type = type;
		this.number = a;
		this.string = b;
		this.ref = c;
	}
	
	@Override
	public Type getType() {
		return Type.ATOM;
	}
	
	@Override
	public List<Expression> getExpressions() {
		return Collections.emptyList();
	}
	
	public AtomType getAtomType() {
		return type;
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
	public Reference getReference() {
		return ref;
	}
	
	@Override
	public void setReference(Reference ref) {
		this.ref = ref;
	}
	
	public String getString() {
		return string;
	}
	
	public double getNumber() {
		return number;
	}
	
	@Override
	public Token getRefToken() {
		return token;
	}
	
	@Override
	public String toString() {
		switch(type) {
			case number: return number_format.format(number);
			case string: return '"' + string + '"';
			case ref: return Objects.toString(ref);
			default: throw new IllegalStateException("Bad AtomExpr missing type");
		}
	}
	
	public static AtomExpr get(Token token, double value) {
		return new AtomExpr(token, value);
	}
	
	public static AtomExpr get(Token token, String string) {
		return new AtomExpr(token, string);
	}
	
	public static AtomExpr get(Token token, Reference ref) {
		return new AtomExpr(token, ref);
	}
	
	/**
	 * Create a copy of a AtomExpr
	 */
	public static AtomExpr get(AtomExpr atom) {
		return new AtomExpr(atom.token, atom.type, atom.number, atom.string, atom.ref == null ? null:atom.ref.clone()).end(atom.end);
	}
}
