package hardcoded.compiler;

import java.io.File;
import java.util.*;

import hardcoded.OutputFormat;
import hardcoded.utils.FileUtils;

/**
 * A build configuration class.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class BuildConfiguration {
	private Set<File> sourceFolders = new LinkedHashSet<>();
	private OutputFormat outputFormat;
	private File workingDirectory;
	private File startFile;
	private File outputFile;
	
	private String errorMessage;
	private int errorCode;
	
	private File resolveFile(File file) {
		if(file == null) return null;
		
		if(file.isAbsolute()) {
			return file.getAbsoluteFile();
		}
		
		return new File(workingDirectory, file.getPath()).getAbsoluteFile();
	}
	
	/**
	 * @param	pathname	a pathname string
	 * @return	{@code true} if the operation was successfull, otherwise check {@linkplain #getLastError()}
	 */
	public boolean addSourceFolder(String pathname) {
		return addSourceFolder(new File(pathname));
	}
	
	/**
	 * @param	file	a file
	 * @return	{@code true} if the operation was successfull, otherwise check {@linkplain #getLastError()}
	 */
	public boolean addSourceFolder(File file) {
		File folder = resolveFile(file);
		if(folder == null) return false;
		
		sourceFolders.add(folder);
		return true;
	}
	
	public void setStartFile(String path) {
		startFile = resolveFile(new File(path));
	}
	
	public void setStartFile(File file) {
		startFile = FileUtils.makeAbsolute(file);
	}
	
	public void setOutputFile(String path) {
		outputFile = resolveFile(new File(path));
	}
	
	public void setOutputFile(File file) {
		outputFile = FileUtils.makeAbsolute(file);
	}
	
	public void setWorkingDirectory(String pathname) {
		setWorkingDirectory(new File(pathname));
	}
	
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory.getAbsoluteFile();
	}
	
	public void setOutputFormat(OutputFormat format) {
		this.outputFormat = format;
	}
	
	
	
	
	public File getWorkingDirectory() {
		return workingDirectory;
	}
	
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}
	
	public Set<File> getSourceFolders() {
		return Set.copyOf(sourceFolders);
	}
	
	public File getStartFile() {
		return startFile;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	public List<File> lookupFile(String pathname) {
		List<File> lookup = new ArrayList<>();
		
		for(File file : sourceFolders) {
			File object = new File(file, pathname);
			if(object.exists()) {
				lookup.add(object);
			}
		}
		
		return lookup;
	}
	
	/**
	 * @return the latest error message
	 */
	public String getLastError() {
		return errorMessage;
	}
	
	/**
	 * @return the latest error code
	 */
	public int getLastCode() {
		return errorCode;
	}
}
