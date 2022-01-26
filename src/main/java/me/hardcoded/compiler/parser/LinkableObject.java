package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.parser.stat.ProgStat;

import java.io.File;

/**
 * This is a linkable object that is used in when creating the intermediate code for the arucas programming language.
 * When combining linkable objects all unresolved references will need to be resolved to compile.
 */
public class LinkableObject {
	private ProgStat program;
	private File file;
	
	public LinkableObject(File file, ProgStat program) {
		this.file = file;
		this.program = program;
	}
	
	public File getFile() {
		return file;
	}
	
	public ProgStat getProgram() {
		return program;
	}
	
	// TODO: Get unresolved imports
	// TODO: Serialize these objects
}
