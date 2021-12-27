package hardcoded.compiler.statement;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import hardcoded.compiler.impl.IBlock;
import hardcoded.compiler.impl.ISyntaxPosition;
import hardcoded.compiler.types.HighType;

public class Construct extends Statement implements IBlock {
	private final Map<String, HighType> members;
	private final String name;
	
	public Construct(String name, ISyntaxPosition syntaxPosition) {
		super(false);
		this.name = name;
		this.members = new LinkedHashMap<>();
		this.syntaxPosition = syntaxPosition;
	}
	
	public String getName() {
		return name;
	}
	
	public void addMember(HighType type, String name) {
		// TODO: Make sure this member does not already exist
		members.put(name, type);
	}
	
	public boolean hasMember(String name) {
		return members.containsKey(name);
	}
	
	@Override
	public String asString() {
		return "CONSTRUCT";
	}
	
	@Override
	public String toString() {
		return "construct %s {}".formatted("");
	}

	@Override
	public File getDeclaringFile() {
		return this.syntaxPosition.getStartPosition().file;
	}
}