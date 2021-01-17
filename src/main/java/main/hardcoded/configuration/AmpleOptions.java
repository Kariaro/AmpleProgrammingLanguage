package hardcoded.configuration;

import java.util.Set;

public enum AmpleOptions {
	COMPILER_MODE("compiler.mode", String.class),
	COMPILER_INPUT_FILE("compiler.inputfile", String.class),
	COMPILER_OUTPUT_FILE("compiler.outputfile", String.class),
	COMPILER_FORMAT("compiler.format", String.class),
	COMPILER_DIRECTORY("compiler.directory", String.class),
	COMPILER_SOURCE_PATHS("compiler.paths", Set.class)
	
	;
	
	public final String field;
	public final Class<?> type;
	
	private AmpleOptions(String field, Class<?> type) {
		this.field = field;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return field;
	}
}
