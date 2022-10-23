package me.hardcoded.utils.error;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.utils.AmpleCache;
import me.hardcoded.utils.Position;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ErrorUtil {
	private static String padNumber(int number, int padding) {
		return ("%" + padding + "d").formatted(number);
	}
	
	public static String createError(ISyntaxPos error, ISyntaxPos causedBy, String message) {
		return createError(error, message);
	}
	
	@Deprecated
	public static String createError(ISyntaxPos error, String message) {
		try {
			File file = error.getFile();
			
			return createError(error, Files.readString(file.toPath()), message);
		} catch (IOException e) {
			return null;
		}
	}
	
	public static String createError(ISyntaxPos error, String content, String message) {
		try {
			int errorLine = error.getEndPosition().line() + 1;
			int errorStart = error.getStartPosition().column();
			int errorEnd = error.getEndPosition().column();
			int columns = Math.max(1, errorEnd - errorStart);
			int padSize = Math.max(1, (int) Math.floor(Math.log10(errorLine)) + 1);
			
			String numPadding = " ".repeat(padSize);
			String numFormat = "%" + padSize + "d";
			String errPadding = " ".repeat(errorStart);
			
			StringBuilder sb = new StringBuilder();
			
			if (content != null) {
				List<String> lines = content.lines().toList();
				String errString = lines.get(errorLine - 1);
				
				sb.append('\n');
				sb.append("%s | %s\n".formatted(numFormat.formatted(errorLine), errString));
				sb.append("%s | %s%s\n".formatted(numPadding, errPadding, "^".repeat(columns)));
				sb.append("%s | %s%s".formatted(numPadding, errPadding, message));
			} else {
				sb.append('\n');
				sb.append("%s | %s".formatted(numFormat.formatted(errorLine), message));
			}
			
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Deprecated
	public static String createFullError(ISyntaxPos error, String message) {
		try {
			File file = error.getFile();
			
			String content = AmpleCache.getFileSource(file);
			if (content == null && file.exists()) {
				// Bad
				content = Files.readString(file.toPath());
			}
			
			return createFullError(error, content, message);
		} catch (IOException e) {
			return null;
		}
	}
	
	private static String createFullError(ISyntaxPos error, String content, String message) {
		StringBuilder sb = new StringBuilder();
		
		Position position = error.getStartPosition();
		sb.append("(").append(error.getFile()).append(") (line: ").append(position.line() + 1).append(", column: ").append(position.column() + 1).append("): ")
			.append(createError(error, content, message));
		
		return sb.toString();
	}
}
