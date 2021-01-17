package hardcoded;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hardcoded.configuration.AmpleOptions;
import hardcoded.configuration.Config;
import hardcoded.utils.FileUtils;

/**
 * Util class for parsing command line options
 * 
 * @author HardCoded
 * @since v0.2
 */
public class CommandLine {
	
	private static void help() {
		// TODO: Locale
		try {
			System.out.println(new String(FileUtils.readInputStream(
				CommandLine.class.getResourceAsStream("/command/help2.txt")
			)));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *<pre>
	 *ample --run "pathname"
	 *ample --compile "start file relative to the source paths" [Options]
	 *</pre>
	 *
	 *<pre>
	 *[--help, -h, -?]  "help message"
	 *[--version]       "version"
	 *
	 *[--paths, -p]     "/relative;C:/Absolute/"
	 *[--format, -x]    "target format"
	 *[--output, -o]    "output file"
	 *[--dir, -d]       "project directory"
	 *</pre>
	 */
	public static Config load(String[] args) {
		Config config = new Config();
		config.set(AmpleOptions.COMPILER_MODE, "none");
		
		if(args.length < 1) {
			help();
			return config;
		}
		
		ListIterator<String> list = List.of(args).listIterator();
		String cmd = list.next().toLowerCase();
		
		if(cmd.equals("--run")) return load_run(list);
		if(cmd.equals("--compile")) return load_compile(list);
		
		help();
		return config;
	}
	
	private static Config load_run(ListIterator<String> args) {
		Config config = new Config();
		config.set(AmpleOptions.COMPILER_DIRECTORY, "");
		config.set(AmpleOptions.COMPILER_MODE, "none");
		
		if(!args.hasNext()) {
			System.err.println("No file specified. [--run]");
			help();
			return config;
		}
		
		File file = new File(args.next());
		if(!file.isFile()) {
			System.err.println("That file does not exist. [--run]");
			help();
			return config;
		}
		
		config.set(AmpleOptions.COMPILER_MODE, "run");
		config.set(AmpleOptions.COMPILER_INPUT_FILE, file.getAbsolutePath());
		return config;
	}
	
	private static Config load_compile(ListIterator<String> args) {
		Config config = new Config();
		config.set(AmpleOptions.COMPILER_MODE, "none");
		config.set(AmpleOptions.COMPILER_SOURCE_PATHS, new LinkedHashSet<String>());
		config.set(AmpleOptions.COMPILER_DIRECTORY, "");
		config.set(AmpleOptions.COMPILER_FORMAT, "ir");
		
		if(!args.hasNext()) {
			System.err.println("Expected a paths relative file. [--compile]");
			return config;
		}
		
		String cmd = args.next();
		config.set(AmpleOptions.COMPILER_INPUT_FILE, cmd);
		
		while(args.hasNext()) {
			cmd = args.next().toLowerCase();
			
			switch(cmd) {
				case "-p":
				case "--paths": {
					if(args.hasNext()) {
						Set<String> set = config.get(AmpleOptions.COMPILER_SOURCE_PATHS);
						String str = args.next();
						set.addAll(List.of(str.split(";")));
					}
					break;
				}
				
				case "-d":
				case "--dir": {
					if(args.hasNext()) config.set(AmpleOptions.COMPILER_DIRECTORY, args.next());
					break;
				}
				
				case "-x":
				case "--format": {
					if(args.hasNext()) config.set(AmpleOptions.COMPILER_FORMAT, args.next());
					break;
				}
				
				case "-o":
				case "--output": {
					if(args.hasNext()) config.set(AmpleOptions.COMPILER_OUTPUT_FILE, args.next());
					break;
				}
				
				case "--version": {
					// Show version information
				}
				
				default: {
					System.err.println("Invalid command '" + cmd + "'");
				}
				case "-?":
				case "-h":
				case "--help": {
					help();
					return config;
				}
			}
		}
		
		File directory; {
			String target = config.get(AmpleOptions.COMPILER_DIRECTORY);
			
			if(target.isBlank()) {
				System.err.println("Project directory was not specified. [--dir, -d]");
				config.set(AmpleOptions.COMPILER_MODE, "none");
				help();
				return config;
			}
			
			directory = new File(target);
			if(!directory.isAbsolute()) {
				System.err.println("Project directory must be an absolute path. [--dir, -d]");
				config.set(AmpleOptions.COMPILER_MODE, "none");
				help();
				return config;
			}
		}
		
		{
			Set<String> converted = new LinkedHashSet<>();
			Set<String> paths = config.get(AmpleOptions.COMPILER_SOURCE_PATHS);
			
			for(String path : paths) {
				try {
					File file = new File(path);
					
					if(file.isAbsolute()) {
						converted.add(path);
					} else {
						converted.add(new File(directory, path).getAbsolutePath());
					}
					
				} catch(Exception e) {
					System.err.println("Failed to add '" + path + "' as a source path. [--paths]");
				}
			}
			paths.clear();
			paths.addAll(converted);
		}
		
		{
			String target = config.get(AmpleOptions.COMPILER_OUTPUT_FILE);
			if(target.isBlank()) {
				System.err.println("Output file was not specified. [--output, -o]");
				return config;
			}
			
			File output = new File(target);
			if(!output.isAbsolute()) {
				config.set(AmpleOptions.COMPILER_OUTPUT_FILE, new File(directory, target).getAbsolutePath());
			}
		}

		config.set(AmpleOptions.COMPILER_MODE, "compile");
		return config;
	}
}
