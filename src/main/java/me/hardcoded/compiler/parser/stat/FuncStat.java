package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.FuncParam;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.List;

public class FuncStat extends Stat {
	private ValueType returnType;
	private List<FuncParam> parameters;
	private Reference reference;
	private Stat body;
	
	public FuncStat(ValueType returnType, Reference reference, List<FuncParam> parameters, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.reference = reference;
		this.returnType = returnType;
		this.parameters = parameters;
	}
	
	public void complete(Stat stat) {
		this.body = stat;
	}
	
	public ValueType getReturnType() {
		return returnType;
	}
	
	public String getName() {
		return reference.getName();
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public List<FuncParam> getParameters() {
		return parameters;
	}
	
	public Stat getBody() {
		return body;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return body.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.FUNC;
	}
}
