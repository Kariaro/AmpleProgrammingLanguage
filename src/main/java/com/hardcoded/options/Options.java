package com.hardcoded.options;

import java.util.HashMap;
import java.util.Map;

/**
 * An options utility class.
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class Options {
	public enum Key {
		/**
		 * The help message.
		 */
		HELP,
		
		/**
		 * The output format of the ample compiler.
		 */
		OUTPUT_FORMAT,

		/**
		 * The project xml file.
		 * <p><i>(Will take priority over {@link#INPUT_FILE} and {@link#OUTPUT_FILE})</i>
		 */
		PROJECT_XML,
		
		/**
		 * A code input file.
		 * <p><i>(Only used when compiling a single file)</i>
		 */
		INPUT_FILE,
		
		/**
		 * The output file for {@link#INPUT_FILE}
		 * <p><i>(Only used when compiling a single file)</i>
		 */
		OUTPUT_FILE,
		
		/**
		 * The file that should be run.
		 * <p><i>(Can not be used with other compiler flags)</i>
		 */
		RUNNING_FILE,
	}
	
	protected Map<Key, String> map;
	protected Options() {
		map = new HashMap<>();
	}
	
	public Options set(Key key, String value) {
		map.put(key, value);
		return this;
	}
	
	public String get(Key key) {
		return map.getOrDefault(key, "");
	}
	
	public String get(Key key, String defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}
	
	public boolean has(Key key) {
		return map.containsKey(key);
	}
	
	public String toString() {
		return map.toString();
	}
	
	/**
	 * Parse command line options and return a options class
	 * 
	 * @param args
	 * @return
	 */
	public static Options parse(String[] args) {
		Options options = new Options();
		if(args.length == 0) {
			return options.set(Key.HELP, "true");
		}
		
		for(int i = 0; i < args.length; i++) {
			String str = args[i];
			
			switch(str) {
				case "-?":
				case "-h": {
					return options.set(Key.HELP, "true");
				}
			}
			
			if(i + 2 > args.length) {
				return options.set(Key.HELP, "true");
			}
			
			switch(str) {
				// OUTPUT FORMAT
				case "-f": {
					options.set(Key.OUTPUT_FORMAT, args[++i]);
					break;
				}
				
				// INPUT FILE
				case "-i": {
					options.set(Key.INPUT_FILE, args[++i]);
					break;
				}
				
				// OUTPUT FILE
				case "-o": {
					options.set(Key.OUTPUT_FILE, args[++i]);
					break;
				}
				
				// RUNNING FILE
				case "-r": {
					options.set(Key.RUNNING_FILE, args[++i]);
					break;
				}
				
				// PROJECT XML
				case "-xml": {
					options.set(Key.PROJECT_XML, args[++i]);
					break;
				}
				
				default: {
					return options.set(Key.HELP, "true");
				}
			}
		}
		
		return options;
	}
}
