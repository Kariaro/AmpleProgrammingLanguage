package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.parser.stat.ProgStat;
import me.hardcoded.compiler.parser.type.Reference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a linkable object that is used in when creating the intermediate code for the arucas programming language.
 * When combining linkable objects all unresolved references will need to be resolved to compile.
 */
public class LinkableObject {
	private List<Reference> missingReferences;
	private ProgStat program;
	private File file;
	
	public LinkableObject(File file, ProgStat program) {
		this.file = file;
		this.program = program;
		this.missingReferences = new ArrayList<>();
	}
	
	public void addMissingReference(Reference reference) {
		missingReferences.add(reference);
	}
	
	public File getFile() {
		return file;
	}
	
	public ProgStat getProgram() {
		return program;
	}
	
	public List<Reference> getMissingReferences() {
		return missingReferences;
	}
	
	// TODO: Serialize these objects
}
