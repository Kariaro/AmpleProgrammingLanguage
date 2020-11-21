package hardcoded.configuration;

import java.io.File;
import java.util.*;

import hardcoded.OutputFormat;
import hardcoded.utils.FileUtils;

/**
 * Testing how a properties configuration system would look
 */
class BuildConfiguration2 extends AmpleConfiguration {
	public static final String KEY_WORKINGDIR = "workingdir";
	public static final String KEY_OUTPUTFILE = "output";
	public static final String KEY_FORMAT = "format";
	public static final String KEY_MAIN = "main";
	
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
	
	private File resolveFile(File file) {
		if(file == null) return null;
		
		if(file.isAbsolute()) {
			return file.getAbsoluteFile();
		}
		
		if(getWorkingDirectory() == null) {
			setMessage(ERROR_WDIR_NULL, "workingDirectory was null");
			return null;
		}
		
		return new File(getWorkingDirectory(), file.getPath()).getAbsoluteFile();
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
			setMessage(ERROR_INVALID_PATH, "Source folder path was canonical. '" + folder + "'");
			return false;
		}
		
		if(!FileUtils.isValidPath(folder)) {
			setMessage(ERROR_INVALID_PATH, "Source folder path was not valid. '" + folder + "'");
			return false;
		}
		
		if(!folder.exists()) {
			setMessage(ERROR_INVALID_PATH, "Source folder path does not exist. '" + folder + "'");
			return false;
		}
		
		if(!folder.isDirectory()) {
			setMessage(ERROR_INVALID_PATH, "Source folder path was not a directory. '" + folder + "'");
			return false;
		}
		
		if(sourceFolders.contains(folder)) {
			setMessage(ERROR_DUPLICATE_PATH, "Duplicate source folder path. '" + folder + "'");
			return false;
		}
		
		sourceFolders.add(folder);
		return true;
	}
	
	public void setStartFile(String path) {
		set(KEY_MAIN, resolveFile(new File(path)));
	}
	
	public void setStartFile(File file) {
		set(KEY_MAIN, FileUtils.makeAbsolute(file));
	}
	
	public void setOutputFile(String path) {
		set(KEY_OUTPUTFILE, resolveFile(new File(path)));
	}
	
	public void setOutputFile(File file) {
		set(KEY_OUTPUTFILE, FileUtils.makeAbsolute(file));
	}
	
	public void setWorkingDirectory(String pathname) {
		setWorkingDirectory(new File(pathname));
	}
	
	public void setWorkingDirectory(File workingDirectory) {
		set(KEY_WORKINGDIR, workingDirectory.getAbsoluteFile());
	}
	
	public void setOutputFormat(OutputFormat format) {
		set(KEY_FORMAT, format);
	}
	
	
	public File getStartFile() { return get(KEY_MAIN); }
	public File getOutputFile() { return get(KEY_OUTPUTFILE); }
	public File getWorkingDirectory() { return get(KEY_WORKINGDIR); }
	public OutputFormat getOutputFormat() { return get(KEY_FORMAT); }
	
	public Set<File> getSourceFolders() {
		return Set.copyOf(sourceFolders);
	}
	
	public boolean isValid() {
		File workingDirectory = getWorkingDirectory();
		File startFile = getStartFile();
		File outputFile = getOutputFile();
		OutputFormat outputFormat = getOutputFormat();
		
		if(workingDirectory == null) return setMessage(ERROR_WDIR_NULL, "No working directory specified");
		if(!workingDirectory.exists()) return setMessage(ERROR_WDIR_NOT_FOUND, "The working directory does not exist. '" + workingDirectory + "'");
		if(!workingDirectory.isDirectory()) return setMessage(ERROR_WDIR_NOT_DIR, "The working directory was not a directory. '" + workingDirectory + "'");
		if(FileUtils.isCanonical(workingDirectory)) return setMessage(ERROR_WDIR_CANONICAL, "The working directory had a canonical path");
		if(!FileUtils.isValidPath(workingDirectory)) return setMessage(ERROR_WDIR_NULL, "The working directory path is invalid. '" + workingDirectory + "'");
		
		// ================================================
		if(startFile == null) return setMessage(ERROR_SFILE_NULL, "Start file was not specified");
		if(!startFile.exists()) return setMessage(ERROR_SFILE_NOT_FOUND, "Start file does not exist. '" + startFile + "'");
		if(!startFile.isFile()) return setMessage(ERROR_SFILE_NOT_FILE, "Start file was not a file");
		if(FileUtils.isCanonical(startFile)) return setMessage(ERROR_SFILE_CANONICAL, "Start file had a canonical path");
		if(!FileUtils.isValidPath(startFile)) return setMessage(ERROR_SFILE_NULL, "Start file path is invalid. '" + startFile + "'");
		
		// ================================================
		if(outputFile == null) return setMessage(ERROR_OFILE_NULL, "Output file was not specified");
		if(outputFile.isDirectory()) return setMessage(ERROR_OFILE_WAS_DIR, "Output file cannot be a directory. '" + outputFile + "'");
		if(!FileUtils.isValidPath(outputFile)) return setMessage(ERROR_OFILE_BAD_NAME, "Output file path is invalid. '" + outputFile + "'");
		if(FileUtils.isCanonical(outputFile)) return setMessage(ERROR_OFILE_CANONICAL, "Output file had a canonical path");
		
		// ================================================
		if(sourceFolders.isEmpty()) return setMessage(ERROR_SFOLDERS_EMPTY, "Source folders was empty");
		
		// Can we be sure that the source folders are going to be 100% accurate
		
		// ================================================
		if(outputFormat == null) return setMessage(ERROR_OFORMAT_NULL, "Output format was not specified");
		
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
}
