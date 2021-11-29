package hardcoded.utils;

import java.util.Arrays;

/**
 * @author HardCoded
 */
public class DebugUtils {
	public static final boolean DEBUG_PARSE_TREE_OPTIMIZATION = true;
	public static final boolean DEBUG_LANGCONTEXT_STACK_TRACE = false;
	
	public static void startDebugTrace() {
		final Thread mainThread = Thread.currentThread();
		Thread thread = new Thread(() -> {
			String last = "";
			try {
				while(true) {
					StackTraceElement[] array = Thread.getAllStackTraces().get(mainThread);
					String curr = Arrays.deepToString(array);
					
					if(!last.equals(curr)) {
						last = curr;
						
						System.out.println("=".repeat(100));
						System.out.println(curr.replace(", ", "\n"));
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	public static boolean isDeveloper() {
		return "true".equalsIgnoreCase(System.getProperty("hardcoded.developer"))
			&& !DebugUtils.isJarRuntime();
	}
	
	public static boolean isJarRuntime() {
		String protocol = DebugUtils.class.getResource("DebugUtils.class").getProtocol();
		return "jar".equals(protocol) || "rsrc".equals(protocol);
	}
}
