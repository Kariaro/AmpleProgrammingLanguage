package unit.pathutils;

import org.junit.Test;

import hardcoded.utils.PathUtils;

import static org.junit.Assert.*;

public class PathUtilsTest {
	
	private static void check_normalize(String input, String expected) {
		assertEquals(expected, PathUtils.normalize(input));
	}
	
	private static void check_getLastPathSegment(String input, String expected) {
		assertEquals(expected, PathUtils.getLastPathSegment(input));
	}
	
	private static void check_getFileExtention(String input, String expected) {
		assertEquals(expected, PathUtils.getFileExtention(input));
	}
	
	private static void check_getFileName(String input, String expected) {
		assertEquals(expected, PathUtils.getFileName(input));
	}
	
	@Test
	public void test_normalize() {
		check_normalize("\\a//b\\c//d", "/a//b/c//d");
		check_normalize("\\d\\c\\b\\a", "/d/c/b/a");
	}
	
	@Test
	public void test_getLastPathSegment() {
		check_getLastPathSegment("/a/b/c/d/testing this.mp3", "testing this.mp3");
		check_getLastPathSegment("/a/b/c/d\\abcde.mp4", "abcde.mp4");
		check_getLastPathSegment("sound.wav", "sound.wav");
	}
	
	@Test
	public void test_getFileExtention() {
		check_getFileExtention("/a/b/c/d/testing this.mp3", "mp3");
		check_getFileExtention("/a/b/c/d\\abcde.mp4", "mp4");
		check_getFileExtention("sound.wav", "wav");
		check_getFileExtention("/a.mp4/", "");
		check_getFileExtention("/a.mp4/.flv", "flv");
		check_getFileExtention("abcd.0.1.2.3.number", "number");
	}
	
	@Test
	public void test_getFileName() {
		check_getFileName("/a/b/c/d/testing this.mp3", "testing this");
		check_getFileName("/a/b/c/d\\abcde.mp4", "abcde");
		check_getFileName("sound.wav", "sound");
		check_getFileName("/a.mp4/", "");
		check_getFileName("/a.mp4/.flv", "");
		check_getFileName("abcd.0.1.2.3.number", "abcd.0.1.2.3");
	}
}
