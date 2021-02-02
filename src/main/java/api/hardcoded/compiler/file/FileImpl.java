package hardcoded.compiler.file;

import java.io.*;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public abstract class FileImpl implements CmFile, Serializable {
	private static final long serialVersionUID = 550317981051547108L;
	
	protected final File file;
	
	/**
	 * A computed checksum from the text content of this file.<br>
	 * This value is unique and should be used to determine if the
	 * file should be recompiled or not.
	 */
	private long checksum;
	
	protected FileImpl(File file) {
		this.file = Objects.requireNonNull(file);
	}
	
	@Override
	public String getName() {
		return file.getName();
	}
	
	@Override
	public String getPath() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	@Override
	public long getChecksum() throws IOException {
		if(!isFile()) return -1;
		
		try(FileInputStream stream = new FileInputStream(file)) {
			CheckedInputStream crc = new CheckedInputStream(stream, new CRC32());
			byte[] buffer = new byte[65536];
			
			// This is more memory efficient because not all bytes needs to be
			// read into memory
			while(crc.read(buffer, 0, buffer.length) >= 0);
			
			checksum = crc.getChecksum().getValue();
			return checksum;
		} catch(IOException e) {
			throw e;
		}
	}
	
	@Override
	public byte[] getContent() throws IOException {
		try(FileInputStream stream = new FileInputStream(file)) {
			return stream.readAllBytes();
		} catch(IOException e) {
			throw e;
		}
	}
	
	@Override
	public File toFile() {
		return file;
	}
	
	@Override
	public boolean exists() {
		return file.exists();
	}
	
	@Override
	public boolean isFile() {
		return file.isFile();
	}
	
	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}
}
