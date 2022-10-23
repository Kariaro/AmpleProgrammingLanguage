package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;

import java.util.List;

public class CompilerStat extends Stat {
	private List<Part> parts;
	private String targetType;
	
	public CompilerStat(ISyntaxPos syntaxPos, String targetType, List<Part> parts) {
		super(syntaxPos);
		this.targetType = targetType;
		this.parts = parts;
	}
	
	public List<Part> getParts() {
		return parts;
	}
	
	public String getTargetType() {
		return targetType;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return false;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.COMPILER;
	}
	
	/**
	 * Compiler expression part
	 *
	 * <code>"mov RAX, {}" : reference</code>
	 */
	public static record Part(ISyntaxPos syntaxPosition, String command, List<Reference> references) {
	
	}
}
