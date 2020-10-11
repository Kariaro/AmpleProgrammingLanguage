package hardcoded.exporter.spooky;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

class ByteOutputWriter {
	private ByteArrayOutputStream stream;
	
	public ByteOutputWriter() {
		stream = new ByteArrayOutputStream(0xffff);
	}
	
	public ByteOutputWriter(int capacity) {
		stream = new ByteArrayOutputStream(capacity);
	}

	public void write(Address addr) { writeAddress(addr); }
	public void write(String value) { writeString(value); }
	public void write(byte[] bytes) { writeBytes(bytes); }
	public void write(OpCode op) { writeOpcode(op); }
	public void write(int value) { writeInt(value); }
	
	// TODO: Create a exception class.
	public void writeString(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
		
		if(bytes.length > 255)
			throw new AssertionError("byte array string was out of range. max 255 got '" + bytes.length + "'");
		
		stream.write(bytes.length);
		stream.write(bytes, 0, bytes.length);
	}
	
	public void writeInt(int value) {
		stream.write(value >>> 24);
		stream.write((value >> 16) & 0xff);
		stream.write((value >> 8) & 0xff);
		stream.write(value & 0xff);
	}

	public void writeAddress(Address addr) {
		writeInt(addr.baseAddr);
		writeInt(addr.offset);
	}

	public void writeOpcode(OpCode op) {
		stream.write(op.code);
	}

	public void writeBytes(byte[] bytes) {
		stream.write(bytes, 0, bytes.length);
	}
	
	public int index() {
		return stream.size();
	}
	
	public void clear() {
		stream.reset();
	}
	
	public byte[] toByteArray() {
		return stream.toByteArray();
	}
}
