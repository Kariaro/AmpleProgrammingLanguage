package com.hardcoded.compiler.impl.instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hardcoded.compiler.api.Instruction;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.utils.NumberUtils;
import com.hardcoded.utils.StringUtils;

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
	
	private String toPrettyString(Reference ref) {
		StringBuilder sb = new StringBuilder();
		
		switch(ref.getType()) {
			case LABEL: {
				if(ref.isTemporary()) {
					if(ref.getTempIndex() < 0) {
						sb.append("#").append(-ref.getTempIndex());
					} else {
						sb.append("#temp_").append(ref.getTempIndex());
					}
				} else {
					sb.append(ref.getName());
				}
				
				break;
			}
			
			case CLASS:
			case FUN:
			case MEMBER: {
				sb.append(ref.getName());
				break;
			}
			
			case VAR: {
				if(ref.isTemporary()) {
					if(ref.getTempIndex() < 0) {
						sb.append("$").append(-ref.getTempIndex());
					} else {
						sb.append("$temp_").append(ref.getTempIndex());
					}
				} else {
					sb.append('@').append(ref.getName());
				}
				
				break;
			}
			
			case EMPTY: {
				sb.append("<invalid>");
				break;
			}
		}
		
		return sb.toString();
	}
	
	public String toPrettyString() {
		if(type == Instruction.Type.LABEL) {
			return toPrettyString(args.get(0).getReference()) + ":";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(type.name().toLowerCase());
		
		if(!args.isEmpty()) {
			sb.append(" ");
			for(int i = 0; i < args.size(); i++) {
				InstParam param = args.get(i);
				
				if(param.isEmpty()) {
					sb.append("<empty>");
				} else if(param.isNumber()) {
					sb.append(NumberUtils.toString(param.getNumber()));
				} else if(param.isString()) {
					sb.append(StringUtils.escapeString(param.getString()));
				} else if(param.isReference()) {
					sb.append(toPrettyString(param.getReference()));
				}
				
				if(i != args.size() - 1) {
					sb.append(", ");
				}
			}
		}
		
		return sb.toString();
	}
	
	public static Inst get(Type type) {
		return new Inst(type);
	}
}
