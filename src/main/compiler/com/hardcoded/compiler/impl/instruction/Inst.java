package com.hardcoded.compiler.impl.instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hardcoded.compiler.api.Instruction;

/**
 * An instruction implementation
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class Inst implements Instruction {
	private Type type;
//	private Token start;
//	private Token token;
//	private Token end;
	private List<InstParam> args;
	
	protected Inst(Type type) {
		this.type = type;
		this.args =  new ArrayList<>();
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
//	public Inst setTokens(Token start, Token end) {
//		return setTokens(start, start, end);
//	}
//	
//	public Inst setTokens(Token token, Token start, Token end) {
//		this.token = token;
//		this.start = start;
//		this.end = end;
//		return this;
//	}
	
	public InstParam getParam(int index) {
		return args.get(index);
	}
	
	public void setParam(int index, InstParam param) {
		args.set(index, Objects.requireNonNull(param, "Instruction parameters must not be null"));
	}
	
	public Inst addParam(InstParam param) {
		args.add(Objects.requireNonNull(param, "Instruction parameters must not be null"));
		return this;
	}
	
	public Inst addParams(InstParam param_0, InstParam param_1, InstParam... params) {
		addParam(param_0);
		addParam(param_1);
		for(InstParam param : params) addParam(param);
		return this;
	}
	
	public int getNumParam() {
		return args.size();
	}
	
//	public List<InstParam> getParams() {
//		return args;
//	}
	
	@Override
	public String toString() {
		return String.format("%s %s", type, args);
	}
	
	public static Inst get(Type type) {
		return new Inst(type);
	}
}
