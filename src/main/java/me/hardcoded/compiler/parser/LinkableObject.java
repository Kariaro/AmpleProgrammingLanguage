package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.parser.stat.ProgStat;
import me.hardcoded.compiler.parser.type.Reference;

import java.io.File;
import java.util.List;

/**
 * This is a linkable object that is used in when creating the intermediate code for the arucas programming language.
 * When combining linkable objects all unresolved references will need to be resolved to compile.
 */
public class LinkableObject {
	private final List<Reference> importedReferences;
	private final List<Reference> exportedReferences;
	private final List<String> imports;
	private final File file;
	private final ProgStat program;
	
	public LinkableObject(File file, ProgStat program, List<String> imports, List<Reference> exportedReferences, List<Reference> importedReferences) {
		this.file = file;
		this.program = program;
		this.importedReferences = importedReferences;
		this.exportedReferences = exportedReferences;
		this.imports = imports;
	}
	
	public List<String> getImports() {
		return imports;
	}
	
	public File getFile() {
		return file;
	}
	
	public ProgStat getProgram() {
		return program;
	}
	
	public List<Reference> getImportedReferences() {
		return importedReferences;
	}
	
	public List<Reference> getExportedReferences() {
		return exportedReferences;
	}
}
