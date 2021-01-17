package hardcoded.utils;

public class PathUtils {
	private PathUtils() {}
	
	public static String getFileExtention(String path) {
		String filename = getLastPathSegment(path);
		int index = filename.lastIndexOf('.');
		return (index < 0) ? "":filename.substring(index + 1);
	}
	
	public static String getFileName(String path) {
		String filename = getLastPathSegment(path);
		int index = filename.lastIndexOf('.');
		return (index < 0) ? filename:filename.substring(0, index);
	}
	
	public static String getLastPathSegment(String path) {
		path = normalize(path);
		int index = path.lastIndexOf('/');
		if(index < 0) return path;
		return path.substring(index + 1);
	}
	
	@Deprecated
	public static String[] getPathSegments(String path) {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	public static String normalize(String path) {
		String result = path;
		result = result.replace("\\", "/");
		return result;
	}
}
