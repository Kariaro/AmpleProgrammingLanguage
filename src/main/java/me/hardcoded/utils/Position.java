package me.hardcoded.utils;

import java.io.File;

public class Position {
	public final File file;
	public final int column;
	public final int line;
	
	@Deprecated(forRemoval = true)
	public final int offset;
	
	public Position(File file, int column, int line, int offset) {
		this.file = file == null ? null : file.getAbsoluteFile();
		this.offset = offset;
		this.column = column;
		this.line = line;
	}
	
	public Position(int column, int line, int offset) {
		this(null, column, line, offset);
	}
}
