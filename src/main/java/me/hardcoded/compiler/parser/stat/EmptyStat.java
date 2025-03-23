package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.io.IOException;

public class EmptyStat extends Stat {
	public EmptyStat(ISyntaxPos syntaxPos) {
		super(syntaxPos);
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.EMPTY;
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
	}
	
	public static EmptyStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		return new EmptyStat(head.syntaxPos());
	}
}
