package unit.build.config;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import hardcoded.OutputFormat;
import hardcoded.compiler.BuildConfiguration;

public class BuildConfigurationTest {
	private static final File LOCAL = new File("src/test/java/unit_res");
	private static final String BAD_NAME = "**";
	
	private static File getLocal(String path) {
		return new File(LOCAL, path);
	}
	
	@Test
	public void test_working_directory() {
		BuildConfiguration config = new BuildConfiguration();
		
		config.isValid();
		assertEquals("ERROR_WDIR_NULL", BuildConfiguration.ERROR_WDIR_NULL, config.getLastCode());
		
		config.setWorkingDirectory(getLocal(BAD_NAME));
		config.isValid();
		assertEquals("ERROR_WDIR_NOT_FOUND", BuildConfiguration.ERROR_WDIR_NOT_FOUND, config.getLastCode());
		
		config.setWorkingDirectory(getLocal("project.txt"));
		config.isValid();
		assertEquals("ERROR_WDIR_NOT_DIR", BuildConfiguration.ERROR_WDIR_NOT_DIR, config.getLastCode());
		
		config.setWorkingDirectory(getLocal("project/../project"));
		config.isValid();
		assertEquals("ERROR_WDIR_CANONICAL", BuildConfiguration.ERROR_WDIR_CANONICAL, config.getLastCode());
	}
	
	@Test
	public void test_start_file() {
		BuildConfiguration config = new BuildConfiguration();
		config.setWorkingDirectory(getLocal("project"));
		
		config.isValid();
		assertEquals("ERROR_SFILE_NULL", BuildConfiguration.ERROR_SFILE_NULL, config.getLastCode());
		
		config.setStartFile(BAD_NAME);
		config.isValid();
		assertEquals("ERROR_SFILE_NOT_FOUND", BuildConfiguration.ERROR_SFILE_NOT_FOUND, config.getLastCode());
		
		config.setStartFile("src1/");
		config.isValid();
		assertEquals("ERROR_SFILE_NOT_FILE", BuildConfiguration.ERROR_SFILE_NOT_FILE, config.getLastCode());
		
		config.setStartFile("src1/file_1.ample/../file_1.ample");
		config.isValid();
		assertEquals("ERROR_SFILE_CANONICAL", BuildConfiguration.ERROR_SFILE_CANONICAL, config.getLastCode());
	}
	
	@Test
	public void test_output_file() {
		BuildConfiguration config = new BuildConfiguration();
		config.setWorkingDirectory(getLocal("project"));
		config.setStartFile("src2/file_2.ample");
		
		config.isValid();
		assertEquals("ERROR_OFILE_NULL", BuildConfiguration.ERROR_OFILE_NULL, config.getLastCode());
		
		config.setOutputFile(BAD_NAME);
		config.isValid();
		assertEquals("ERROR_OFILE_BAD_NAME", BuildConfiguration.ERROR_OFILE_BAD_NAME, config.getLastCode());
		
		config.setOutputFile("src1/");
		config.isValid();
		assertEquals("ERROR_OFILE_WAS_DIR", BuildConfiguration.ERROR_OFILE_WAS_DIR, config.getLastCode());
		
		config.setOutputFile("bin/out_1.ample/../out_1.ample");
		config.isValid();
		assertEquals("ERROR_OFILE_CANONICAL", BuildConfiguration.ERROR_OFILE_CANONICAL, config.getLastCode());
	}
	
	@Test
	public void test_source_folders() {
		BuildConfiguration config = new BuildConfiguration();
		config.setWorkingDirectory(getLocal("project"));
		config.setStartFile("src2/file_2.ample");
		config.setOutputFile("bin/out_1.ample");
		boolean result;
		
		result = config.addSourceFolder("src1/../src1");
		assertEquals("adding canonical path", false, result);

		result = config.addSourceFolder(BAD_NAME);
		assertEquals("adding invalid path", false, result);
		
		config.isValid();
		assertEquals("ERROR_SFOLDERS_EMPTY", BuildConfiguration.ERROR_SFOLDERS_EMPTY, config.getLastCode());
		
		result = config.addSourceFolder("src1");
		assertEquals("adding a valid path", true, result);
		
		result = config.addSourceFolder("src1");
		assertEquals("no duplicate paths", false, result);
		
		result = config.addSourceFolder("src1/file_1.ample");
		assertEquals("adding a file", false, result);
		
		result = config.addSourceFolder("src4");
		assertEquals("adding a invalid directory", false, result);
	}
	
	@Test
	public void test_output_format() {
		BuildConfiguration config = new BuildConfiguration();
		config.setWorkingDirectory(getLocal("project"));
		config.setStartFile("src3/file_3.ample");
		config.setOutputFile("bin/out_2.ample");
		config.addSourceFolder("src3");
		
		config.isValid();
		assertEquals("ERROR_OFORMAT_NULL", BuildConfiguration.ERROR_OFORMAT_NULL, config.getLastCode());
		
		config.setOutputFormat(OutputFormat.IR);
		assertEquals("expected no errors", true, config.isValid());
	}
}
