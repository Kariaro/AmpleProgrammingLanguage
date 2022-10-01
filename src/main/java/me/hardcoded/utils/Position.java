package me.hardcoded.utils;

import java.io.File;

public class Position {
	public final File file;
	public final int column;
	public final int line;
	
	public Position(File file, int column, int line) {
		this.file = file == null ? null : file.getAbsoluteFile();
		this.column = column;
		this.line = line;
	}
	
	public Position(int column, int line) {
		this(null, column, line);
	}
}
