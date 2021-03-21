package com.hardcoded.main;

import com.hardcoded.compiler.AmpleCompiler;
import com.hardcoded.logger.Log;
import com.hardcoded.logger.Log.Level;
import com.hardcoded.options.Options;
import com.hardcoded.options.Options.Key;
import com.hardcoded.utils.Utils;

/**
 * The main entry point for the ample compiler
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class CompilerMain {
	private static final Log LOGGER = Log.getLogger(CompilerMain.class);
	
	public static final String AUTHOR = "HardCoded";
	public static final String VERSION = "0.2.0";
	
	public static void main(String[] args) {
		LOGGER.info("--------------------------------------------------------------");
		LOGGER.info("AmpleProgrammingLanguage compiler %s (2021-03-20) (c)", VERSION);
		LOGGER.info();
		LOGGER.info("Made by %s%s", AUTHOR, Utils.isDeveloper() ? " (Developer Mode)":"");
		LOGGER.info("--------------------------------------------------------------");
		LOGGER.info();
		
		if(Utils.isDeveloper()) {
			Log.setLogLevel(Level.DEBUG);
			
			args = new String[] {
				"-f", "ample",
				"-i", "res/main.ample",
				"-o", "res/main.ir"
			};
		}
		
		Options options = Options.parse(args);
		LOGGER.info("Options: %s", options);
		LOGGER.info();
		
		if(options.has(Key.HELP)) {
			show_help(options);
			return;
		}
		
		if(options.has(Key.PROJECT_XML)) {
			build_project(options);
			return;
		}
		
		if(options.has(Key.INPUT_FILE)) {
			build_single(options);
			return;
		}
		
		if(options.has(Key.RUNNING_FILE)) {
			run_file(options);
			return;
		}
	}
	
	private static void show_help(Options options) {
		LOGGER.info(Utils.getResourceFileAsString("/cli/cmd/help.txt"));
	}
	
	private static void build_project(Options options) {
		LOGGER.info("Building a project:");
		LOGGER.info("  Format : '%s'", options.get(Key.OUTPUT_FORMAT));
		LOGGER.info("  Project: '%s'", options.get(Key.PROJECT_XML));
		LOGGER.info();

		LOGGER.warn("Project builds are not supported yet");
	}
	
	private static void build_single(Options options) {
		LOGGER.info("Building a single file:");
		LOGGER.info("  Format: '%s'", options.get(Key.OUTPUT_FORMAT));
		LOGGER.info("  Input : '%s'", options.get(Key.INPUT_FILE));
		LOGGER.info("  Output: '%s'", options.get(Key.OUTPUT_FILE));
		LOGGER.info();
		
		AmpleCompiler compiler = new AmpleCompiler();
		compiler.singleBuild(options);
	}
	
	private static void run_file(Options options) {
		LOGGER.info("Running a single file:");
		LOGGER.info();
		
		LOGGER.warn("Running files is not supported yet");
	}
}
