package me.hardcoded.utils.error;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.utils.Position;

import java.nio.file.Files;
import java.util.List;

public class ErrorUtil {
	private static String padNumber(int number, int padding) {
		return ("%" + padding + "d").formatted(number);
	}
	
	public static String createError(ISyntaxPosition error, ISyntaxPosition causedBy, String message) {
		return createError(error, message);
	}
	
	public static String createError(ISyntaxPosition error, String message) {
		try {
			List<String> lines = Files.readAllLines(error.getStartPosition().file.toPath());
			
			int errorLine = error.getEndPosition().line + 1;
			int errorStart = error.getStartPosition().column;
			int errorEnd = error.getEndPosition().column;
			int columns = Math.max(1, errorEnd - errorStart);
			int padSize = Math.max(1, (int) Math.floor(Math.log10(errorLine)) + 1);
			
			String numPadding = " ".repeat(padSize);
			String numFormat = "%" + padSize + "d";
			String errPadding = " ".repeat(errorStart);
			String errString = lines.get(errorLine - 1);
			
			StringBuilder sb = new StringBuilder();
			
			sb.append('\n');
			sb.append("%s | %s\n".formatted(numFormat.formatted(errorLine), errString));
			sb.append("%s | %s%s\n".formatted(numPadding, errPadding, "^".repeat(columns)));
			sb.append("%s | %s%s".formatted(numPadding, errPadding, message));
			
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String createFullError(ISyntaxPosition error, String message) {
		StringBuilder sb = new StringBuilder();
		
		Position position = error.getStartPosition();
		sb.append("(").append(position.file).append(") (line: ").append(position.line + 1).append(", column: ").append(position.column + 1).append("): ")
			.append(createError(error, message));
		
		return sb.toString();
	}
}
