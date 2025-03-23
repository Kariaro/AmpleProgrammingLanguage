package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.io.IOException;

public class BreakStat extends Stat {
	public BreakStat(ISyntaxPos syntaxPos) {
		super(syntaxPos);
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
		return TreeType.BREAK;
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
	}
	
	public static BreakStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		return new BreakStat(head.syntaxPos());
	}
}
