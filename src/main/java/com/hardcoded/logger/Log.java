package com.hardcoded.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A simple Logging utility class.
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public final class Log {
	private static boolean SHOW_LINE_INDEX = true;
	private static int LOG_LEVEL = 900;
	public static void setLogLevel(Level level) {
		LOG_LEVEL = level.level;
	}
	
	public static void showLineIndex(boolean enable) {
		SHOW_LINE_INDEX = enable;
	}
	
	private static final DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("HH:mm:ss SSS");  
	
	private final String customName;
	private Log(Class<?> clazz) {
		this(clazz.getName());
	}
	
	private Log(String name) {
		this.customName = name;
	}
	
	public static Log getLogger(Class<?> clazz) {
		return new Log(clazz);
	}
	
	public static Log getLogger(String name) {
		return new Log(name);
	}
	
	private void log(Level level, int line_index, String format, Object... args) {
		if(!shouldLog(level)) return;
		if(format == null) format = "";
		
		if(level == Level.ERROR) {
			System.err.println(formatString(level, line_index, format, args));
		} else {
			System.out.println(formatString(level, line_index, format, args));
		}
	}
	
	public void log(Level level, String format, Object... args) {
		log(level, getLineIndex(), format, args);
	}
	
	public void info() {
		log(Level.INFO, getLineIndex(), "");
	}
	
	public void info(String format, Object... args) {
		log(Level.INFO, getLineIndex(), format, args);
	}
	
	public void debug() {
		log(Level.DEBUG, getLineIndex(), "");
	}
	
	public void debug(Object format, Object... args) {
		log(Level.DEBUG, Objects.toString(format), args);
	}
	
	public void warn(String format, Object... args) {
		log(Level.WARNING, getLineIndex(), format, args);
	}
	
	public void error(String format, Object... args) {
		log(Level.ERROR, getLineIndex(), format, args);
	}
	
	public void throwing(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		log(Level.ERROR, getLineIndex(), sw.toString());
	}
	
	private boolean shouldLog(Level level) {
		return level.level <= LOG_LEVEL;
	}
	
	private int getLineIndex() {
		if(!SHOW_LINE_INDEX) return -1;
		
		StackTraceElement[] stack = Thread.getAllStackTraces().get(Thread.currentThread());
		if(stack == null) return -1;
		StackTraceElement last = stack[stack.length - 1];
		return last.getLineNumber();
	}
	
	
	private String formatHeader(Level level, int line_index) {
		if(SHOW_LINE_INDEX) {
			return String.format("[%s] [%s] [%s:%d]: ", date_formatter.format(LocalDateTime.now()), level.name(), customName, line_index);
		}
		
		return String.format("[%s] [%s] [%s]: ", date_formatter.format(LocalDateTime.now()), level.name(), customName);
	}
	
	private String formatString(Level level, int line_index, String format, Object... args) {
		String header = formatHeader(level, line_index);
		String message = String.format(format, args);
		
		if(message.indexOf('\n') != -1) {
			return header + message.replaceAll("\n", "\n" + header);
		}
		
		return String.format("%s%s", header, message);
	}
	
	public enum Level {
		ALL(10000),
		
		DEBUG(1000),
		INFO(900),
		WARNING(200),
		ERROR(100);
		
		private final int level;
		private Level(int level) {
			this.level = level;
		}
	}
}
