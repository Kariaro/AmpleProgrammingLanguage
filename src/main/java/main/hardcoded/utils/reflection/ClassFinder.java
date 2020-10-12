package hardcoded.utils.reflection;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * This class will only work if it is inside the same jar as the calling
 * class is.
 * 
 * @author HardCoded
 */
@Deprecated
public class ClassFinder {
	public static List<Class<?>> findClasses(String path) {
		boolean isJar = isRunningFromJar();
		
		if(isJar) {
			return _findClassesInJar(path);
		} else {
			return _findClassesInProject(path);
		}
	}
	
	private static List<Class<?>> _findClassesInJar(String path) {
		File basePath = getBasePath();
		if(basePath == null || basePath.isDirectory()) return Collections.emptyList();
		
		try {
			ZipFile file = new ZipFile(basePath);
			String zipPath = path.replace('.', '/');
			
			List<String> entries = file.stream()
				.filter(x -> x.getName().startsWith(zipPath))
				.filter(x -> !x.isDirectory())
				.map(x -> x.getName().substring(zipPath.length()))
				.collect(Collectors.toList());
			
			List<Class<?>> list = new ArrayList<>();
			for(String name : entries) {
				if(!name.endsWith(".class")) continue;
				name = name.substring(0, name.length() - 6);
				if(name.startsWith("/")) name = name.substring(1);
				
				try {
					list.add(Class.forName(path + "." + name));
				} catch(ClassNotFoundException e) {
					// e.printStackTrace();
				}
			}
			
			file.close();
			return list;
		} catch(ZipException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return Collections.emptyList();
	}
	
	private static List<Class<?>> _findClassesInProject(String path) {
		File basePath = getBasePath();
		if(basePath == null || !basePath.isDirectory()) return Collections.emptyList();
		
		File current = basePath;
		for(String pack : path.split("\\.")) {
			current = new File(current, pack);
			if(!current.exists() || !current.isDirectory()) return Collections.emptyList(); // Failed to find this directory
		}
		
		List<Class<?>> list = new ArrayList<>();
		for(String name : current.list()) {
			if(!name.endsWith(".class")) continue;
			name = name.substring(0, name.length() - 6);
			
			try {
				list.add(Class.forName(path + "." + name));
			} catch(ClassNotFoundException e) {
				// e.printStackTrace();
			}
		}
		
		return list;
	}
	
	private static File getBasePath() {
		CodeSource source = ClassFinder.class.getProtectionDomain().getCodeSource();
		if(source == null) {
			// T O D O : Find a fallback method for getting the base path
			return null;
		}
		
		URL location = source.getLocation();
		if(location == null) {
			// ???
			return null;
		}
		
		File file = toFile(location);
		if(file == null) {
			// This could mean that this file was loaded virtually.
			return null;
		}
		
		return file;
	}
	
	private static File toFile(URL url) {
		try {
			return new File(url.toURI());
		} catch(URISyntaxException e) {
			// e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean isRunningFromJar() {
		String protocol = ClassFinder.class.getResource("ClassFinder.class").getProtocol();
		return "jar".equals(protocol) || "rsrc".equals(protocol);
	}
}
