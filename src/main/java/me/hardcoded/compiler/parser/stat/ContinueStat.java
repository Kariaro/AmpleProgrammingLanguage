package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.io.IOException;

public class ContinueStat extends Stat {
	public ContinueStat(ISyntaxPos syntaxPos) {
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
		return TreeType.CONTINUE;
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
	}
	
	public static ContinueStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		return new ContinueStat(head.syntaxPos());
	}
}
