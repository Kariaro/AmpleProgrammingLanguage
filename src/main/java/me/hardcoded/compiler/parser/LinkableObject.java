package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.parser.stat.ProgStat;
import me.hardcoded.compiler.parser.type.ReferenceSyntax;

import java.io.File;
import java.util.List;

/**
 * This is a linkable object that is used in when creating the intermediate code for the arucas programming language.
 * When combining linkable objects all unresolved references will need to be resolved to compile.
 */
public class LinkableObject {
	// Used by the linker
	private final List<ReferenceSyntax> importedReferences;
	private final List<ReferenceSyntax> exportedReferences;
	private final List<String> imports;
	
	// Internal fields
	private final String checksum;
	private final File file;
	private final ProgStat program;
	
	public LinkableObject(File file, String checksum, ProgStat program, List<String> imports, List<ReferenceSyntax> exportedReferences, List<ReferenceSyntax> importedReferences) {
		this.file = file;
		this.checksum = checksum;
		this.program = program;
		this.importedReferences = importedReferences;
		this.exportedReferences = exportedReferences;
		this.imports = imports;
	}
	
	public List<ReferenceSyntax> getImportedReferences() {
		return importedReferences;
	}
	
	public List<ReferenceSyntax> getExportedReferences() {
		return exportedReferences;
	}
	
	public List<String> getImports() {
		return imports;
	}
	
	public String getChecksum() {
		return checksum;
	}
	
	public File getFile() {
		return file;
	}
	
	public ProgStat getProgram() {
		return program;
	}
	
}
