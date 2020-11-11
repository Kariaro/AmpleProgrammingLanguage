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
	public static final int ERROR_WDIR_NULL			= 1;
	public static final int ERROR_WDIR_NOT_FOUND	= 2;
	public static final int ERROR_WDIR_NOT_DIR		= 3;
	@Deprecated
	public static final int ERROR_WDIR_CANONICAL	= 4;
	
	public static final int ERROR_SFILE_NULL		= 5;
	public static final int ERROR_SFILE_NOT_FOUND	= 6;
	public static final int ERROR_SFILE_NOT_FILE	= 7;
	@Deprecated
	public static final int ERROR_SFILE_CANONICAL	= 8;
	
	public static final int ERROR_OFILE_NULL		= 9;
	public static final int ERROR_OFILE_WAS_DIR		= 10;
	public static final int ERROR_OFILE_BAD_NAME	= 11;
	@Deprecated
	public static final int ERROR_OFILE_CANONICAL	= 12;
	
	public static final int ERROR_SFOLDERS_EMPTY	= 13;
	public static final int ERROR_SFOLDERS_BAD_NAME	= 14;
	public static final int ERROR_OFORMAT_NULL		= 15;
	
	public static final int ERROR_INVALID_PATH		= 100;
	public static final int ERROR_DUPLICATE_PATH	= 101;
	
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
		
		if(workingDirectory == null) {
			errorMessage = "workingDirectory was null";
			errorCode = ERROR_WDIR_NULL;
			return null;
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
		
		if(FileUtils.isCanonical(folder)) {
			errorMessage = "Source folder path was canonical. '" + folder + "'";
			errorCode = ERROR_INVALID_PATH;
			return false;
		}
		
		if(!FileUtils.isValidPath(folder)) {
			errorMessage = "Source folder path was not valid. '" + folder + "'";
			errorCode = ERROR_INVALID_PATH;
			return false;
		}
		
		if(!folder.exists()) {
			errorMessage = "Source folder path does not exist. '" + folder + "'";
			errorCode = ERROR_INVALID_PATH;
			return false;
		}
		
		if(!folder.isDirectory()) {
			errorMessage = "Source folder path was not a directory. '" + folder + "'";
			errorCode = ERROR_INVALID_PATH;
			return false;
		}
		
		if(sourceFolders.contains(folder)) {
			errorMessage = "Duplicate source folder path. '" + folder + "'";
			errorCode = ERROR_DUPLICATE_PATH;
			return false;
		}
		
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
	
	public boolean isValid() {
		if(workingDirectory == null) {
			errorMessage = "No working directory specified";
			errorCode = ERROR_WDIR_NULL;
			return false;
		}
		if(!workingDirectory.exists()) {
			errorMessage = "The working directory does not exist. '" + workingDirectory + "'";
			errorCode = ERROR_WDIR_NOT_FOUND;
			return false;
		}
		if(!workingDirectory.isDirectory()) {
			errorMessage = "The working directory was not a directory. '" + workingDirectory + "'";
			errorCode = ERROR_WDIR_NOT_DIR;
			return false;
		}
		if(FileUtils.isCanonical(workingDirectory)) {
			errorMessage = "The working directory had a canonical path";
			errorCode = ERROR_WDIR_CANONICAL;
			return false;
		}
		if(!FileUtils.isValidPath(workingDirectory)) {
			errorMessage = "The working directory path is invalid. '" + workingDirectory + "'";
			errorCode = ERROR_WDIR_NULL;
			return false;
		}
		
		// ================================================
		if(startFile == null) {
			errorMessage = "Start file was not specified";
			errorCode = ERROR_SFILE_NULL;
			return false;
		}
		if(!startFile.exists()) {
			errorMessage = "Start file does not exist. '" + startFile + "'";
			errorCode = ERROR_SFILE_NOT_FOUND;
			return false;
		}
		if(!startFile.isFile()) {
			errorMessage = "Start file was not a file";
			errorCode = ERROR_SFILE_NOT_FILE;
			return false;
		}
		if(FileUtils.isCanonical(startFile)) {
			errorMessage = "Start file had a canonical path";
			errorCode = ERROR_SFILE_CANONICAL;
			return false;
		}
		if(!FileUtils.isValidPath(startFile)) {
			errorMessage = "Start file path is invalid. '" + startFile + "'";
			errorCode = ERROR_SFILE_NULL;
			return false;
		}
		
		// ================================================
		if(outputFile == null) {
			errorMessage = "Output file was not specified";
			errorCode = ERROR_OFILE_NULL;
			return false;
		}
		if(outputFile.isDirectory()) {
			errorMessage = "Output file cannot be a directory. '" + outputFile + "'";
			errorCode = ERROR_OFILE_WAS_DIR;
			return false;
		}
		if(!FileUtils.isValidPath(outputFile)) {
			errorMessage = "Output file path is invalid. '" + outputFile + "'";
			errorCode = ERROR_OFILE_BAD_NAME;
			return false;
		}
		if(FileUtils.isCanonical(outputFile)) {
			errorMessage = "Output file had a canonical path";
			errorCode = ERROR_OFILE_CANONICAL;
			return false;
		}
		
		// ================================================
		if(sourceFolders.isEmpty()) {
			errorMessage = "Source folders was empty";
			errorCode = ERROR_SFOLDERS_EMPTY;
			return false;
		}
		
		// Can we be sure that the source folders are going to be 100% accurate
		
		// ================================================
		if(outputFormat == null) {
			errorMessage = "Output format was not specified";
			errorCode = ERROR_OFORMAT_NULL;
			return false;
		}
		
		errorMessage = null;
		errorCode = 0;
		return true;
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
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean pretty) {
		StringBuilder sb = new StringBuilder();
		sb.append("BuildConfiguration");
		if(pretty) sb.append(" {");
		else sb.append("{");

		// ==================================
		if(pretty) sb.append("\n\t");
		sb.append("\"workingDirectory\":");
		if(pretty) sb.append(" ");
		if(workingDirectory == null) sb.append("null,");
		else sb.append('"').append(workingDirectory).append('"').append(",");
		
		// ==================================
		if(pretty) sb.append("\n\t");
		sb.append("\"startFile\":");
		if(pretty) sb.append(" ");
		if(startFile == null) sb.append("null,");
		else sb.append('"').append(startFile).append('"').append(",");

		// ==================================
		if(pretty) sb.append("\n\t");
		sb.append("\"outputFile\":");
		if(pretty) sb.append(" ");
		if(outputFile == null) sb.append("null,");
		else sb.append('"').append(outputFile).append('"').append(",");

		// ==================================
		if(pretty) sb.append("\n\t");
		sb.append("\"outputFormat\":");
		if(pretty) sb.append(" ");
		sb.append(outputFormat).append(",");
		
		// ==================================
		if(pretty) sb.append("\n\t");
		sb.append("\"sourceFolders\":");
		if(pretty) sb.append(" ");
		sb.append("[");
		for(File file : sourceFolders) {
			if(pretty) sb.append("\n\t\t");
			if(file == null) sb.append("null,");
			else sb.append('"').append(file).append('"').append(",");
		}
		if(!sourceFolders.isEmpty()) {
			sb.deleteCharAt(sb.length() - 1);
			if(pretty) sb.append("\n\t");
		}
		sb.append("]");
		if(pretty) sb.append("\n");
		sb.append("}");
		
		return sb.toString();
	}
}
