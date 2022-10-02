package me.hardcoded.configuration;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// TODO: Make this into an XML format
public class CompilerConfiguration {
	public enum Type {
		/**
		 * The output format of the compiler. <i>Default ir</i>
		 */
		OUTPUT_FORMAT("format"),
		
		/**
		 * The output target format. <i>Default bytecode</i>
		 */
		TARGET_FORMAT("target"),
		
		/**
		 * The file that is going to be compiled.
		 */
		SOURCE_FILE("source"),
		
		/**
		 * The directory used for non-absolute paths.
		 */
		WORKING_DIRECTORY("working_directory"),
		
		/**
		 * The output folder. <i>Default same folder as source file</i>
		 */
		OUTPUT_FOLDER("output"),
		
		/**
		 * Cache option. <i>Default is True</i>
		 */
		USE_CACHE("use_cache");
		
		public final String key;
		
		Type(String key) {
			this.key = key;
		}
	}
	
	private final Map<Type, Object> map;
	
	public CompilerConfiguration() {
		this.map = new HashMap<>();
		
		set(Type.USE_CACHE, true);
		set(Type.OUTPUT_FORMAT, OutputFormat.IR);
		set(Type.TARGET_FORMAT, TargetFormat.BYTECODE);
		setSourceFile("");
		setOutputFolder("");
		setWorkingDirectory("");
	}
	
	public OutputFormat getOutputFormat() {
		return get(Type.OUTPUT_FORMAT);
	}
	
	public TargetFormat getTargetFormat() {
		return get(Type.TARGET_FORMAT);
	}
	
	public File getSourceFile() {
		return get(Type.SOURCE_FILE);
	}
	
	public File getOutputFolder() {
		return get(Type.OUTPUT_FOLDER);
	}
	
	public File getWorkingDirectory() {
		return get(Type.WORKING_DIRECTORY);
	}
	
	public boolean useCache() {
		return get(Type.USE_CACHE);
	}
	
	void setOutputFormat(OutputFormat format) {
		set(Type.OUTPUT_FORMAT, format);
	}
	
	void setTargetFormat(TargetFormat format) {
		set(Type.TARGET_FORMAT, format);
	}
	
	void setSourceFile(String pathname) {
		set(Type.SOURCE_FILE, resolveFile(new File(pathname)));
	}
	
	void setOutputFolder(String pathname) {
		set(Type.OUTPUT_FOLDER, resolveFile(new File(pathname)));
	}
	
	void setWorkingDirectory(String pathname) {
		set(Type.WORKING_DIRECTORY, Path.of(pathname).toAbsolutePath().toFile());
	}
	
	@SuppressWarnings("unchecked")
	private <T> T get(Type type) {
		return (T) map.get(type);
	}
	
	private void set(Type type, Object object) {
		map.put(type, object);
	}
	
	private File resolveFile(File file) {
		if (file.isAbsolute()) {
			return file.getAbsoluteFile();
		}
		
		File workingDirectory = getWorkingDirectory();
		return new File(workingDirectory, file.getPath()).getAbsoluteFile();
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
	
	public static String getHelpMessage() {
		return "Help message for AmpleProgrammingLanguage https://github.com/Kariaro/AmpleProgrammingLanguage\n\n" +
			"Usage: [options]\n\n" +
			"options:\n" +
			"    -? -h --help\n" +
			"                  display this help message\n\n" +
			"    -w --working-directory <path>\n" +
			"                  set the working directory\n\n" +
			"    -f --format <format>\n" +
			"                  the output format type\n\n" +
			"    --format-list\n" +
			"                  prints all available formats\n\n" +
			"    -i --input-file <pathname>\n" +
			"                  set the main entry point of the compiler\n\n" +
			"    -o --output-file <pathname>\n" +
			"                  set the output file of this compiler\n\n" +
			"    -b --bytecode\n" +
			"                  set the output to bytecode (default)\n\n" +
			"    -a --assembler\n" +
			"                  set the output to assembler\n\n" +
			"    -c --compile\n" +
			"                  set the compiler mode to compile (default)\n\n" +
			"    -r --run\n" +
			"                  set the compiler mode to run\n";
	}
	
	public static String getFormatListMessage() {
		return "Available formats:\n" +
			Arrays.stream(OutputFormat.values())
				.map("[%s]\n"::formatted)
				.reduce((a, b) -> a + b)
				.orElse("").trim();
	}
	
	public static CompilerConfiguration parseArgs(Logger logger, String[] args) {
		CompilerConfiguration config = new CompilerConfiguration();
		
		/*
		./ample
		
			--format-list				displays a list of available output formats
			
			--project, -p <xml>			compile the project from an xml file
			
			--target, -t <value>		set the target output of the compiler
			
			--format, -f <value>		set the format of the compiler
			
			--use-cache <boolean>       change how the compiler deals with cache files
			
			-i <source>					specify the input file to compile
			
			-o <outputFolder>			specify the output folder
		*/
		
		try {
			for (int i = 0; i < args.length; i++) {
				String str = args[i];
				
				switch (str) {
					case "--format-list" -> {
						logger.info("{}", getFormatListMessage());
						return config;
					}
					
					// Project
					
					case "-t", "--target" -> {
						config.setTargetFormat(TargetFormat.valueOf(args[++i].toUpperCase()));
					}
					
					case "-f", "--format" -> {
						config.setOutputFormat(OutputFormat.valueOf(args[++i].toUpperCase()));
					}
					
					case "-w", "--working-directory" -> {
						config.setWorkingDirectory(args[++i]);
					}
					
					case "-i", "--input-file" -> {
						config.setSourceFile(args[++i]);
					}
					
					case "-o", "--output-folder" -> {
						config.setOutputFolder(args[++i]);
					}
					
					case "-b", "--bytecode" -> {
						config.setTargetFormat(TargetFormat.BYTECODE);
					}
					
					case "-a", "--assembler" -> {
						config.setTargetFormat(TargetFormat.ASSEMBLER);
					}
					
					case "--use-cache" -> {
						config.set(Type.USE_CACHE, Boolean.parseBoolean(args[++i]));
					}
					
					default -> {
						logger.info("Invalid argument '{}'", str);
						logger.info("{}", getHelpMessage());
						return null;
					}
					
					case "-?", "-h", "--help" -> {
						logger.info("{}", getHelpMessage());
						return null;
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.info("Invalid argument");
			logger.info("{}", getHelpMessage());
			return null;
		}
		
		return config;
	}
}
