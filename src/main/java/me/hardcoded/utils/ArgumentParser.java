package me.hardcoded.utils;

import jdk.dynalink.beans.StaticClass;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Argument parser for the ample programming language.
 *
 * This parser is a copy of getopt
 */
public class ArgumentParser {
	public static class Option {
		private final String longName;
		private final Character shortName;
		private final boolean hasArgument;
		
		public Option(Character shortName) {
			this(shortName, null, false);
		}
		
		public Option(Character shortName, boolean hasArgument) {
			this(shortName, null, hasArgument);
		}
		
		public Option(String longName) {
			this(null, longName, false);
		}
		
		public Option(String longName, boolean hasArgument) {
			this(null, longName, hasArgument);
		}
		
		public Option(Character shortName, String longName, boolean hasArgument) {
			this.shortName = shortName;
			this.longName = longName;
			this.hasArgument = hasArgument;
		}
	}
	
	public static void parse(String[] args, List<Option> options, IArgument consumer) {
		for (int i = 0, len = args.length; i < len; i++) {
			String data = args[i];
			
			if (data.startsWith("-")) {
			
			}
		}
	}
	
	public static void main(String[] args) {
		args = new String[] {
			"-abcd",
			"Test"
		};
		
		ArgumentParser.parse(args, List.of(
			new Option('a'),
			new Option('b', true),
			new Option('c'),
			new Option('d', true)
		), (option, argument) -> {
		
		});
	}
	
	@FunctionalInterface
	public interface IArgument {
		void accept(Option value, String argument);
	}
}
