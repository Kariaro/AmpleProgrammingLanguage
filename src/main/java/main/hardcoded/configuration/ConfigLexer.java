package hardcoded.configuration;

import hardcoded.lexer.GenericLexerContext;

public class ConfigLexer {
	public static final GenericLexerContext<ConfigType> LEXER;
	
	static {
		LEXER = new GenericLexerContext<ConfigType>()
			.addRule(null, i -> i.addRegex("[ \t]+"))

			// Data
			.addRule(ConfigType.STRING, i -> i.addMultiline("\"", "\""))
			.addRule(ConfigType.NUMBER, i -> i.addRegex("[0-9]+"))
			
			
			.addRule(ConfigType.FORMAT, i -> i.addStrings("-f", "-format"))
			.addRule(ConfigType.WORKING_DIRECTORY, i -> i.addStrings("-p", "--working-directory"))
			.addRule(ConfigType.RUN, i -> i.addString("-run"))
			.addRule(ConfigType.SOURCE_FOLDERS, i -> i.addString("-sf"))
			.addRule(ConfigType.COMPILE, i -> i.addString("-compile"))
			.addRule(ConfigType.HELP, i -> i.addStrings("-?", "-h", "--help"))
			
		.toImmutable();	
	}
	
	public enum ConfigType {
		FORMAT,
		WORKING_DIRECTORY,
		RUN,
		SOURCE_FOLDERS,
		COMPILE,
		HELP,
		
		STRING,
		NUMBER
	}
}
