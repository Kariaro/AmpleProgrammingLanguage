package hardcoded.utils;

public final class DomainUtils {
	private DomainUtils() {}
	
	public static boolean isJarRuntime() {
		String protocol = DomainUtils.class.getResource("DomainUtils.class").getProtocol();
		return "jar".equals(protocol) || "rsrc".equals(protocol);
	}
}
