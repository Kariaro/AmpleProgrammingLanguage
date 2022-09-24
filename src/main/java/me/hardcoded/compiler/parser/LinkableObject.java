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
	// Used by the linker
	private final List<Reference> importedReferences;
	private final List<Reference> exportedReferences;
	private final List<String> imports;
	
	// Internal fields
	private final String checksum;
	private final File file;
	private final ProgStat program;
	
	public LinkableObject(File file, String checksum, ProgStat program, List<String> imports, List<Reference> exportedReferences, List<Reference> importedReferences) {
		this.file = file;
		this.checksum = checksum;
		this.program = program;
		this.importedReferences = importedReferences;
		this.exportedReferences = exportedReferences;
		this.imports = imports;
	}
	
	public List<Reference> getImportedReferences() {
		return importedReferences;
	}
	
	public List<Reference> getExportedReferences() {
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
