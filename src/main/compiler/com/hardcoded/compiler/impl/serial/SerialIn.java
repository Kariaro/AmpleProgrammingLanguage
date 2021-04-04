package com.hardcoded.compiler.impl.serial;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.lexer.Token;

class SerialIn {
	private final DataInputStream in;
	
	public SerialIn(InputStream in) {
		this.in = new DataInputStream(in);
	}
	
	private Map<Integer, Reference> references = new LinkedHashMap<>();
	private Map<Integer, String> strings = new LinkedHashMap<>();
	private Map<Integer, Token> tokens = new LinkedHashMap<>();
	
	public void readStringTable() {
		int length = readPackedInt();
		for(int i = 0; i < length; i++) {
			strings.put(i, readUTF());
		}
	}
	
	public void readTokenTable() {
		int length = readPackedInt();
		for(int i = 0; i < length; i++) {
			tokens.put(i, readTokenPtr());
		}
	}
	
	public void readReferenceTable() {
		int length = readPackedInt();
		for(int i = 0; i < length; i++) {
			references.put(i, readReferencePtr());
		}
	}
	
	public String readUTF() {
		try {
			return in.readUTF();
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public boolean readBool() {
		try {
			return in.readBoolean();
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public int readUnsignedByte() {
		try {
			return in.readUnsignedByte();
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public int readPackedInt() {
		int result = 0;
		int shift = 0;
		int tmp = 0;
		
		while(true) {
			tmp = readUnsignedByte();
			result |= (tmp & 0x7f) << shift;
			shift += 7;
			
			if((tmp & 0x80) == 0) {
				if((shift < 32) && ((tmp & 0x40) != 0)) {
					return result | (~0 << shift);
				}
				
				return result;
			}
		}
	}
	
	public double readDouble() {
		try {
			return in.readDouble();
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public <T> T readEnum(Class<T> clazz) {
		return clazz.getEnumConstants()[readPackedInt()];
	}
	
	public byte[] readBytes() {
		try {
			int length = in.readInt();
			return in.readNBytes(length);
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public int readInt() {
		try {
			return in.readInt();
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public double writeDouble() {
		try {
			return in.readDouble();
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	// POINTER
	public Token readTokenPtr() {
		return new Token(
			readString(),
			readString(),
			readPackedInt(),
			readPackedInt(),
			readPackedInt()
		);
	}
	
	public Reference readReferencePtr() {
		return new Reference(
			readString(),
			readEnum(Reference.Type.class),
			readPackedInt(),
			readPackedInt()
		);
	}
	
	// CUSTOM
	public String readString() {
		return strings.get(readPackedInt());
	}
	
	public Token readToken() {
		int index = readPackedInt();
		if(index == -1) return Token.EMPTY;
		return tokens.get(index);
	}
	
	public Reference readReference() {
		return references.get(readPackedInt());
	}
}
