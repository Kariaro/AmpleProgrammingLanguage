package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.FuncParam;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.List;

public class FuncStat extends Stat {
	private List<Reference> parameters;
	private Reference reference;
	private Stat body;
	
	public FuncStat(Reference reference, List<Reference> parameters, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.reference = reference;
		this.parameters = parameters;
	}
	
	public void complete(Stat stat) {
		this.body = stat;
	}
	
	public ValueType getReturnType() {
		return reference.getValueType();
	}
	
	public String getName() {
		return reference.getName();
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public List<Reference> getParameters() {
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
