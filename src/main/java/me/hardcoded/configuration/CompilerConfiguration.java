package me.hardcoded.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import me.hardcoded.main.Main;
import me.hardcoded.utils.FileUtils;

// TODO: Make this into an XML format
public class CompilerConfiguration {
//	public enum Type {
//		/**
//		 * The output format of the compiler.
//		 */
//		OUTPUT_FORMAT,
//
//		/**
//		 * The output target format. <i>Default bytecode</i>
//		 */
//		TARGET_FORMAT,
//
//		/**
//		 * The file that is going to be compiled.
//		 */
//		SOURCE_FILE,
//
//		/**
//		 * The output destination of the compiled file.
//		 */
//		OUTPUT_FILE,
//
//		/**
//		 * The directory used for non-absolute paths.
//		 */
//		WORKING_DIRECTORY,
//
//		/**
//		 * The operation that should be performed by the compiler.
//		 */
//		OPERATION,
//	}

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
		;
		
		public final String key;
		
		Type(String key) {
			this.key = key;
		}
	}
	
	private final Map<Type, Object> map;
	
	public CompilerConfiguration() {
		this.map = new HashMap<>();
		
		setSourceFile("");
		setOutputFolder("");
//		setOutputFile("");
		setWorkingDirectory("");
		setOutputFormat(OutputFormat.IR);
		setTargetFormat(TargetFormat.BYTECODE);
//		setOperation(Operation.COMPILE);
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
	
	public void setOutputFormat(OutputFormat format) {
		set(Type.OUTPUT_FORMAT, format);
	}
	
	public void setTargetFormat(TargetFormat format) {
		set(Type.TARGET_FORMAT, format);
	}
	
	public void setSourceFile(String pathname) {
		set(Type.SOURCE_FILE, resolveFile(new File(pathname)));
	}
	
	public void setOutputFolder(String pathname) {
		set(Type.OUTPUT_FOLDER, resolveFile(new File(pathname)));
	}
	
	public void setWorkingDirectory(String pathname) {
		set(Type.WORKING_DIRECTORY, FileUtils.makeAbsolute(new File(pathname)));
	}
	
	@SuppressWarnings("unchecked")
	private <T> T get(Type type) {
		return (T)map.get(type);
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
	
	public List<File> lookupFile(String pathname) {
		return List.of(getWorkingDirectory().toPath().resolve(pathname).toAbsolutePath().toFile());
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
	
	private static void printHelpMessage() {
		try (InputStream stream = Main.class.getResourceAsStream("/command/help.txt")) {
			System.out.println(new String(stream.readAllBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void printFormatListMessage() {
		System.out.println("Available formats:");
		System.out.println(Arrays.stream(OutputFormat.values())
			.map("[%s]\n"::formatted).reduce((a, b) -> a + b).orElse("").trim()
		);
	}
	
	public static CompilerConfiguration parseArgs(String[] args) {
		CompilerConfiguration config = new CompilerConfiguration();
		
		/*
		./ample
		
			--format-list				displays a list of available output formats
			
			--project, -p <xml>			compile the project from an xml file
			
			--target, -t <value>		set the target output of the compiler
			
			--format, -f <value>		set the format of the compiler
			
			-i <source>					specify the input file to compile
			
			-o <outputFolder>			specify the output folder
		*/
		
		try {
			for(int i = 0; i < args.length; i++) {
				String str = args[i];
				
				switch(str) {
					case "--format-list" -> {
						printFormatListMessage();
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
					
//					case "-c", "--compile" -> {
//						config.setOperation(Operation.COMPILE);
//					}
//
//					case "-r", "--run" -> {
//						config.setOperation(Operation.RUN);
//					}
					
					default -> {
						System.out.printf("Invalid argument '%s'\n\n", str);
						printHelpMessage();
					}
					
					case "-?", "-h", "--help" -> {
						printHelpMessage();
						return config;
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Invalid argument");
			printHelpMessage();
		}
		
		return config;
	}
}
