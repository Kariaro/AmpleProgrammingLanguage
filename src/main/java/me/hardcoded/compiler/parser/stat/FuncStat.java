package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;

import java.util.List;
import java.util.Objects;

public class FuncStat extends Stat {
	private List<Reference> parameters;
	private Reference reference;
	private Stat body;
	
	public FuncStat(ISyntaxPos syntaxPos, List<Reference> parameters, Reference reference) {
		super(syntaxPos);
		this.parameters = parameters;
		this.reference = Objects.requireNonNull(reference);
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public List<Reference> getParameters() {
		return parameters;
	}
	
	public void setBody(Stat body) {
		this.body = body;
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
