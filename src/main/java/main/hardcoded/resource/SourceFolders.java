package hardcoded.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hardcoded.utils.FileUtils;

/**
 * A class used for source code and resource lookup
 * 
 * @author HardCoded
 */
public class SourceFolders {
	private List<File> paths;
	
	public SourceFolders() {
		paths = new ArrayList<>();
	}
	
	public void addSourceFolders(List<String> folders) {
		paths.clear();
		
		for(String path : folders) {
			// TODO: Give errors
			File file = new File(FileUtils.getAbsolutePathString(path));
			if(!file.isDirectory()) continue;
			paths.add(file);
		}
	}
	
	public List<File> lookupFile(String pathname) {
		List<File> lookup = new ArrayList<>();
		
		for(File file : paths) {
			File object = new File(file, pathname);
			if(object.exists()) {
				lookup.add(object);
			}
		}
		
		return lookup;
	}

	public boolean isEmpty() {
		return paths.isEmpty();
	}
}
