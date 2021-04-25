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
	
	protected final Token token;
	
	protected AtomType type;
	protected double number = 0d;
	protected String string = "";
	protected Reference ref;
	
	private AtomExpr(Token token, double value) {
		this(token, AtomType.NUMBER, value, null, null);
	}
	
	private AtomExpr(Token token, String value) {
		this(token, AtomType.STRING, 0, value, null);
	}
	
	private AtomExpr(Token token, Reference ref) {
		this(token, AtomType.REF, 0, null, ref);
	}
	
	private AtomExpr(Token token, AtomType type, double a, String b, Reference c) {
		super(token, false);
		this.token = token;
		this.type = type;
		this.number = a;
		this.string = b;
		this.ref = c;
		
		setLocation(token.offset, token.offset + token.value.length());
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
		return type == AtomType.REF;
	}
	
	public boolean isNumber() {
		return type == AtomType.NUMBER;
	}
	
	public boolean isString() {
		return type == AtomType.STRING;
	}
	
	@Override
	public boolean isConstant() {
		return type != AtomType.REF;
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
			case NUMBER: return number_format.format(number);
			case STRING: return '"' + string + '"';
			case REF: return Objects.toString(ref);
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
		AtomExpr expr = new AtomExpr(atom.token, atom.type, atom.number, atom.string, atom.ref == null ? null:atom.ref.clone());
		expr.setLocation(atom.start_offset, atom.end_offset);
		return expr;
	}
}
