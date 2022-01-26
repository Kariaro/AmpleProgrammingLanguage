package me.hardcoded.configuration;

import java.io.File;
import java.io.IOException;
import java.util.*;

import me.hardcoded.main.Main;
import me.hardcoded.utils.FileUtils;

public class CompilerConfiguration {
	public enum Type {
		/**
		 * The output format of the compiler.
		 */
		OUTPUT_FORMAT,
		
		/**
		 * The output target format. <i>Default bytecode</i>
		 */
		TARGET_FORMAT,
		
		/**
		 * The file that is going to be compiled.
		 */
		SOURCE_FILE,
		
		/**
		 * The output destination of the compiled file.
		 */
		OUTPUT_FILE,
		
		/**
		 * The directory used for non absolute paths.
		 */
		WORKING_DIRECTORY,
		
		/**
		 * The operation that should be performed by the compiler.
		 */
		OPERATION,
		
		/**
		 * The source folder directories.
		 */
		SOURCE_FOLDERS,
	}
	
	public enum Operation {
		COMPILE,
		RUN,
		NONE
	}
	
	private final Map<Type, Object> map;
	
	public CompilerConfiguration() {
		this.map = new HashMap<>();
		
		setSourceFile("");
		setOutputFile("");
		setWorkingDirectory("");
		setOutputFormat(OutputFormat.IR);
		setTargetFormat(TargetFormat.BYTECODE);
		setOperation(Operation.COMPILE);
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
	
	public File getOutputFile() {
		return get(Type.OUTPUT_FILE);
	}
	
	public File getWorkingDirectory() {
		return get(Type.WORKING_DIRECTORY);
	}
	
	public Operation getOperation() {
		return get(Type.OPERATION);
	}
	
	public List<File> getSourceFolders() {
		List<File> list = get(Type.SOURCE_FOLDERS);
		if (list == null) {
			set(Type.SOURCE_FOLDERS, list = new ArrayList<>());
		}
		return list;
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
	
	public void setOutputFile(String pathname) {
		set(Type.OUTPUT_FILE, resolveFile(new File(pathname)));
	}
	
	public void setWorkingDirectory(String pathname) {
		set(Type.WORKING_DIRECTORY, FileUtils.makeAbsolute(new File(pathname)));
	}
	
	public void setOperation(Operation operation) {
		set(Type.OPERATION, operation);
	}
	
	public void addSourceFolder(String pathname) {
		getSourceFolders().add(resolveFile(new File(pathname)));
	}
	
	public void clearSourceFolders() {
		getSourceFolders().clear();
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T get(Type type) {
		return (T)map.get(type);
	}
	
	protected void set(Type type, Object object) {
		map.put(type, object);
	}
	
	private File resolveFile(File file) {
		if(file.isAbsolute()) {
			return file.getAbsoluteFile();
		}
		
		File workingDirectory = getWorkingDirectory();
		return new File(workingDirectory, file.getPath()).getAbsoluteFile();
	}
	
	public List<File> lookupFile(String pathname) {
		List<File> lookup = new ArrayList<>();
		
		for (File path : getSourceFolders()) {
			File object = new File(path, pathname);
			if (object.exists()) {
				lookup.add(object);
			}
		}
		
		return lookup;
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
	
	private static void printHelpMessage() {
		try {
			System.out.println(new String(FileUtils.readInputStream(
				Main.class.getResourceAsStream("/command/help.txt")
			)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static CompilerConfiguration parseArgs(String[] args) {
		CompilerConfiguration config = new CompilerConfiguration();
		
		try {
			for(int i = 0; i < args.length; i++) {
				String str = args[i];
				
				switch(str) {
					case "-f", "--format" -> {
						config.setOutputFormat(OutputFormat.get(args[++i].toUpperCase()));
					}
					
					case "-w", "--working-directory" -> {
						config.setWorkingDirectory(args[++i]);
					}
					
					case "-s", "--source-folders" -> {
						config.clearSourceFolders();
						String string = args[++i];
						for (String path : string.split(";")) {
							config.addSourceFolder(path);
						}
					}
					
					case "-i", "--input-file" -> {
						config.setSourceFile(args[++i]);
					}
					
					case "-o", "--output-file" -> {
						config.setOutputFile(args[++i]);
					}
					
					case "-b", "--bytecode" -> {
						config.setTargetFormat(TargetFormat.BYTECODE);
					}
					
					case "-a", "--assembler" -> {
						config.setTargetFormat(TargetFormat.ASSEMBLER);
					}
					
					case "-c", "--compile" -> {
						config.setOperation(Operation.COMPILE);
					}
					
					case "-r", "--run" -> {
						config.setOperation(Operation.RUN);
					}
					
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
