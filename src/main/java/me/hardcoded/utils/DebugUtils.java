package me.hardcoded.utils;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HardCoded
 */
public class DebugUtils {
	public static final boolean DEBUG_LANGCONTEXT_STACK_TRACE = false;
	public static final boolean DEBUG_REFERENCE_INFORMATION = true;
	
	public static void startDebugTrace() {
		final Thread mainThread = Thread.currentThread();
		Thread thread = new Thread(() -> {
			String last = "";
			try {
				while (true) {
					StackTraceElement[] array = Thread.getAllStackTraces().get(mainThread);
					String curr = Arrays.deepToString(array);
					
					if (!last.equals(curr)) {
						last = curr;
						
						System.out.println("=".repeat(100));
						System.out.println(curr.replace(", ", "\n"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	 * Returns the highest file name that matches the file name pattern.
	 *
	 * @param directory       the directory
	 * @param fileNamePattern the file name pattern where {@code %d} is the index
	 * @return the absolute path of the next file name
	 */
	public static String getNextFileId(File directory, String fileNamePattern) {
		// First we need to make sure that all characters are escaped properly
		String[] parts = fileNamePattern.split("%d", -1);
		if (parts.length != 2) {
			throw new IllegalArgumentException("File pattern did not contain '%d'");
		}
		
		// Why we quote each part is because characters such as '.' should be properly escaped
		String regexPattern = Pattern.quote(parts[0]) + "([0-9]+)" + Pattern.quote(parts[1]);
		Pattern pattern = Pattern.compile(regexPattern);
		String[] fileNames = directory.list();
		
		long highestId = 0;
		if (fileNames != null) {
			for (String fileName : fileNames) {
				Matcher matcher = pattern.matcher(fileName);
				
				if (matcher.find()) {
					try {
						highestId = Math.max(highestId, Long.parseLong(matcher.group(1)));
					} catch (NumberFormatException ignore) {
						// We didn't match a number ignore.
					}
				}
			}
		}
		
		return new File(directory, fileNamePattern.formatted(highestId + 1)).getAbsolutePath();
	}
	
	public static boolean isDeveloper() {
		return "true".equalsIgnoreCase(System.getProperty("me.hardcoded.developer"))
			&& !DebugUtils.isJarRuntime();
	}
	
	public static boolean isJarRuntime() {
		String protocol = DebugUtils.class.getResource("DebugUtils.class").getProtocol();
		return "jar".equals(protocol) || "rsrc".equals(protocol);
	}
}
