package hc.compiler;

import java.io.*;
import java.nio.charset.Charset;

public class HFile {
	private File file;
	
	public HFile(File file) {
		this(file.getAbsolutePath());
	}
	
	public HFile(String path) {
		file = new File(path);
	}
	
	public HFile(String path, String name) {
		file = new File(path, name);
	}
	
	public HFile(File path, String name) {
		file = new File(path, name);
	}
	
	
	public File getFile() {
		return file;
	}
	
	public String getPath() {
		return file.getAbsolutePath();
	}
	
	public String getName() {
		return file.getName();
	}
	
	public byte[] readBytes() throws IOException {
		if(!file.exists() || file.isDirectory())
			throw new IOException("The file you are trying to read does not exist");
		
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataInputStream stream = new DataInputStream(new FileInputStream(file));
		
		int readBytes = 0;
		byte[] buffer = new byte[8192];
		while((readBytes = stream.read(buffer)) != -1) {
			bs.write(buffer, 0, readBytes);
		}
		
		stream.close();
		return bs.toByteArray();
	}
	
	public char[] readChars(Charset charset) throws IOException {
		if(!file.exists() || file.isDirectory())
			throw new IOException("The file you are trying to read does not exist");
		
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charset);
		CharArrayWriter writer = new CharArrayWriter();
		
		int readBytes = 0;
		char[] buffer = new char[8192];
		while((readBytes = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, readBytes);
		}
		
		reader.close();
		return writer.toCharArray();
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof HFile)) return false;
		return ((HFile)obj).hashCode() == hashCode();
	}
	
	public int hashCode() {
		return file.getAbsolutePath().hashCode();
	}
	
	public boolean exists() {
		return file.exists();
	}
}
