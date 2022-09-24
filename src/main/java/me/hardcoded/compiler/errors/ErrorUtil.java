package me.hardcoded.compiler.errors;

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
			Position errorStart = error.getStartPosition();
			Position errorEnd = error.getEndPosition();
			
			int maxLine = errorEnd.line;
			int padSize = (int) Math.ceil(Math.log10(maxLine));
			String numPadding = " ".repeat(padSize);
			List<String> lines = Files.readAllLines(errorStart.file.toPath());
			
			StringBuilder sb = new StringBuilder();
			
			sb.append('\n');
			//			sb.append("%s | ( %s )\n".formatted(numPadding, errorStart.file));
			sb.append("%s | %s\n".formatted(padNumber(errorEnd.line + 1, padSize), lines.get(errorStart.line)));
			sb.append("%s | %s%s\n".formatted(numPadding, " ".repeat(errorStart.column), "^".repeat(errorEnd.column - errorStart.column)));
			sb.append("%s | %s%s".formatted(numPadding, " ".repeat(errorStart.column), message));
			
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
