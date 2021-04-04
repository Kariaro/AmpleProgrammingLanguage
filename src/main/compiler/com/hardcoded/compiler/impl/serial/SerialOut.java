package com.hardcoded.compiler.impl.serial;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.lexer.Token;

class SerialOut {
	private final DataOutputStream out;
	private ByteArrayOutputStream bs;
	
	public SerialOut(ByteArrayOutputStream out) {
		this.out = new DataOutputStream(out);
	}
	
	public SerialOut(OutputStream out) {
		this.out = new DataOutputStream(out);
	}
	
	public SerialOut() {
		this.bs = new ByteArrayOutputStream();
		this.out = new DataOutputStream(bs);
	}
	
	public SerialOut(SerialOut parent) {
		this.references.putAll(parent.references);
		this.strings.putAll(parent.strings);
		this.tokens.putAll(parent.tokens);
		
		this.bs = new ByteArrayOutputStream();
		this.out = new DataOutputStream(bs);
	}
	
	public byte[] toByteArray() {
		return bs.toByteArray();
	}
	
	private Map<String, Integer> strings = new LinkedHashMap<>();
	public Map<String, Integer> getStrings() {
		return strings;
	}
	
	private Map<Reference, Integer> references = new LinkedHashMap<>();
	public Map<Reference, Integer> getReferences() {
		return references;
	}
	
	private Map<Token, Integer> tokens = new LinkedHashMap<>();
	public Map<Token, Integer> getTokens() {
		return tokens;
	}
	
	public void writeUTF(String str) {
		try {
			out.writeUTF(str);
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public void writeBool(boolean value) {
		try {
			out.writeBoolean(value);
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public void writePackedInt(int value) {
		while(true) {
			int tmp = value & 0x7f;
			value >>= 7;
			
			int sign = tmp & 0x40;
			if((value == 0 && (sign == 0)) || (value == -1 && (sign != 0))) {
				writeByte(tmp);
				return;
			}
			
			writeByte(tmp | 0x80);
		}
	}
	
	public void writeByte(int value) {
		try {
			out.writeByte(value);
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public void writeEnum(Enum<?> value) {
		writePackedInt(value.ordinal());
	}
	
	public void writeBytes(byte[] bytes) {
		try {
			//out.writeInt(bytes.length);
			out.write(bytes);
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public void writeInt(int value) {
		try {
			out.writeInt(value);
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	public void writeDouble(double value) {
		try {
			out.writeDouble(value);
		} catch(IOException e) {
			throw new SerialException(e);
		}
	}
	
	// CUSTOM
	public void writeTokenPtr(Token token) {
		writeString(token.value);
		writeString(token.group);
		writePackedInt(token.offset);
		writePackedInt(token.line);
		writePackedInt(token.column);
	}
	
	public void writeReferencePtr(Reference ref) {
		writeString(ref.getName());
		writeEnum(ref.getType());
		writePackedInt(ref.getTempIndex());
		writePackedInt(ref.getUniqueIndex());
	}
	
	// POINTER
	public void writeToken(Token token) {
		if(token == Token.EMPTY) {
			writePackedInt(-1);
			return;
		}
		
		int index;
		if(tokens.containsKey(token)) {
			index = tokens.get(token);
		} else {
			index = tokens.size();
			tokens.put(token, index);
		}
		
		writePackedInt(index);
	}
	
	public void writeReference(Reference ref) {
		int index;
		if(references.containsKey(ref)) {
			index = references.get(ref);
		} else {
			index = references.size();
			references.put(ref, index);
		}
		
		writePackedInt(index);
	}
	
	public void writeString(String str) {
		int index;
		if(strings.containsKey(str)) {
			index = strings.get(str);
		} else {
			index = strings.size();
			strings.put(str, index);
		}
		
		writePackedInt(index);
	}
}
