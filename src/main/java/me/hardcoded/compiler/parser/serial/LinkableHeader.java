package me.hardcoded.compiler.parser.serial;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.Position;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class LinkableHeader {
	static final int MAGIC = 0x414d4C46; // 'AMLF' A Linkable File
	
	protected final RefPos<String> stringMap;
	protected final RefPos<ValueType> valueTypeMap;
	protected final RefPos<Namespace> namespaceMap;
	protected final RefPos<Reference> referenceMap;
	protected final RefPos<ISyntaxPos> syntaxPositionMap;
	protected File file;
	protected String checksum;
	
	public LinkableHeader() {
		this.stringMap = new RefPos<>();
		this.valueTypeMap = new RefPos<>();
		this.namespaceMap = new RefPos<>();
		this.referenceMap = new RefPos<>();
		this.syntaxPositionMap = new RefPos<>();
	}
	
	public void clear() {
		stringMap.clear();
		valueTypeMap.clear();
		namespaceMap.clear();
		referenceMap.clear();
		syntaxPositionMap.clear();
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getChecksum() {
		return checksum;
	}
	
	public void readHeader(DataInputStream in) throws IOException {
		int magic = in.readInt();
		if (magic != MAGIC) {
			throw new IOException("Wrong magic value");
		}
		
		file = new File(readString(in));
		checksum = readString(in);
		
		clear();
		
		for (int i = 0, size = readVarInt(in); i < size; i++) {
			String string = readString(in);
			stringMap.put(string);
		}
		
		for (int i = 0, size = readVarInt(in); i < size; i++) {
			ValueType valueType = readValueType(in);
			valueTypeMap.put(valueType);
		}
		
		for (int i = 0, size = readVarInt(in); i < size; i++) {
			Namespace valueType = readNamespace(in);
			namespaceMap.put(valueType);
		}
		
		for (int i = 0, size = readVarInt(in); i < size; i++) {
			Reference reference = readReference(in);
			referenceMap.put(reference);
		}
		
		for (int i = 0, size = readVarInt(in); i < size; i++) {
			ISyntaxPos syntaxPosition = readISyntaxPosition(in);
			syntaxPositionMap.put(syntaxPosition);
		}
	}
	
	public void writeHeader(DataOutputStream out) throws IOException {
		out.writeInt(MAGIC);
		out.writeUTF(file.getAbsolutePath());
		out.writeUTF(checksum);
		
		byte[] refsBytes = writeRefs();
		byte[] strsBytes = writeStrings();
		
		out.write(strsBytes);
		out.write(refsBytes);
	}
	
	private void processRefs() throws IOException {
		DataOutputStream dummy = new DataOutputStream(OutputStream.nullOutputStream());
		
		for (Namespace namespace : namespaceMap.list) {
			writeNamespace(namespace, dummy);
		}
		
		for (Reference reference : referenceMap.list) {
			writeReference(reference, dummy);
		}
	}
	
	private byte[] writeRefs() throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bs);
		
		// Initialize all value types
		processRefs();
		
		writeVarInt(valueTypeMap.list.size(), out);
		for (ValueType valueType : valueTypeMap.list) {
			writeValueType(valueType, out);
		}
		
		writeVarInt(namespaceMap.list.size(), out);
		for (Namespace namespace : namespaceMap.list) {
			writeNamespace(namespace, out);
		}
		
		writeVarInt(referenceMap.list.size(), out);
		for (Reference reference : referenceMap.list) {
			writeReference(reference, out);
		}
		
		writeVarInt(syntaxPositionMap.list.size(), out);
		for (ISyntaxPos syntaxPosition : syntaxPositionMap.list) {
			writeISyntaxPosition(syntaxPosition, out);
		}
		
		return bs.toByteArray();
	}
	
	private byte[] writeStrings() throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bs);
		
		writeVarInt(stringMap.list.size(), out);
		for (String str : stringMap.list) {
			out.writeUTF(str);
		}
		
		return bs.toByteArray();
	}
	
	// Specific types
	public int readVarInt(DataInputStream in) throws IOException {
		int numRead = 0;
		int result = 0;
		int read;
		
		do {
			read = in.read();
			int value = (read & 0b01111111);
			result |= (value << (7 * numRead));
			
			numRead++;
			if (numRead > 5) {
				throw new RuntimeException("VarInt is too big");
			}
		} while ((read & 0b10000000) != 0);
		
		return result;
	}
	
	public void writeVarInt(int value, DataOutputStream out) throws IOException {
		do {
			int temp = (value & 0b01111111);
			value >>>= 7;
			if (value != 0) {
				temp |= 0b10000000;
			}
			
			out.write(temp);
		} while (value != 0);
	}
	
	
	// Type readers
	private String readString(DataInputStream in) throws IOException {
		return in.readUTF();
	}
	
	private ValueType readValueType(DataInputStream in) throws IOException {
		String name = deserializeString(in);
		int flags = readVarInt(in);
		int depth = readVarInt(in);
		int size = readVarInt(in);
		return new ValueType(name, size, depth, flags);
	}
	
	private Namespace readNamespace(DataInputStream in) throws IOException {
		String name = deserializeString(in);
		return new Namespace(name);
	}
	
	private Reference readReference(DataInputStream in) throws IOException {
		String name = deserializeString(in);
		String mangledName = deserializeString(in);
		Namespace namespace = deserializeNamespace(in);
		ValueType valueType = deserializeValueType(in);
		int usages = readVarInt(in);
		int id = readVarInt(in);
		int flags = readVarInt(in);
		
		Reference reference = new Reference(name, namespace, valueType, id, flags, usages);
		reference.setMangledName(mangledName);
		return reference;
	}
	
	private ISyntaxPos readISyntaxPosition(DataInputStream in) throws IOException {
		String name = deserializeString(in);
		Position start = readPosition(in);
		Position end = readPosition(in);
		return ISyntaxPos.of(name, start, end);
	}
	
	private Position readPosition(DataInputStream in) throws IOException {
		int column = readVarInt(in);
		int line = readVarInt(in);
		return new Position(column, line);
	}
	
	
	// Type writers
	private void writeValueType(ValueType valueType, DataOutputStream out) throws IOException {
		serializeString(valueType.getName(), out);
		writeVarInt(valueType.getFlags(), out);
		writeVarInt(valueType.getDepth(), out);
		writeVarInt(valueType.getSize(), out);
	}
	
	private void writeNamespace(Namespace namespace, DataOutputStream out) throws IOException {
		serializeString(namespace.getPath(), out);
	}
	
	private void writeReference(Reference reference, DataOutputStream out) throws IOException {
		serializeString(reference.getName(), out);
		serializeString(reference.getMangledName(), out);
		serializeNamespace(reference.getNamespace(), out);
		serializeValueType(reference.getValueType(), out);
		writeVarInt(reference.getUsages(), out);
		writeVarInt(reference.getId(), out);
		writeVarInt(reference.getFlags(), out);
	}
	
	private void writeISyntaxPosition(ISyntaxPos syntaxPos, DataOutputStream out) throws IOException {
		serializeString(syntaxPos.getPath(), out);
		writePosition(syntaxPos.getStartPosition(), out);
		writePosition(syntaxPos.getEndPosition(), out);
	}
	
	private void writePosition(Position position, DataOutputStream out) throws IOException {
		writeVarInt(position.column(), out);
		writeVarInt(position.line(), out);
	}
	
	
	// Type deserializers
	public String deserializeString(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		if (idx == 0) {
			return null;
		} else {
			return stringMap.get(idx - 1);
		}
	}
	
	public ValueType deserializeValueType(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		return valueTypeMap.get(idx);
	}
	
	public Namespace deserializeNamespace(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		return namespaceMap.get(idx);
	}
	
	public Reference deserializeReference(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		return referenceMap.get(idx);
	}
	
	public ISyntaxPos deserializeISyntaxPosition(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		return syntaxPositionMap.get(idx);
	}
	
	
	// Type serializers
	public void serializeString(String string, DataOutputStream out) throws IOException {
		if (string == null) {
			writeVarInt(0, out);
		} else {
			int idx = stringMap.put(string);
			writeVarInt(idx + 1, out);
		}
	}
	
	public void serializeValueType(ValueType valueType, DataOutputStream out) throws IOException {
		int idx = valueTypeMap.put(valueType);
		writeVarInt(idx, out);
	}
	
	public void serializeNamespace(Namespace namespace, DataOutputStream out) throws IOException {
		int idx = namespaceMap.put(namespace);
		writeVarInt(idx, out);
	}
	
	public void serializeReference(Reference reference, DataOutputStream out) throws IOException {
		int idx = referenceMap.put(reference);
		writeVarInt(idx, out);
	}
	
	public void serializeISyntaxPosition(ISyntaxPos syntaxPosition, DataOutputStream out) throws IOException {
		int idx = syntaxPositionMap.put(syntaxPosition);
		writeVarInt(idx, out);
	}
	
	
	// Classes
	private static class RefPos<T> {
		private final List<T> list;
		private final Map<T, Integer> map;
		
		private RefPos() {
			this.list = new ArrayList<>();
			this.map = new LinkedHashMap<>();
		}
		
		int put(T elm) {
			Integer val = map.get(elm);
			
			if (val == null) {
				int size = list.size();
				map.put(elm, size);
				list.add(elm);
				return size;
			}
			
			return val;
		}
		
		T get(int idx) {
			return list.get(idx);
		}
		
		void clear() {
			map.clear();
			list.clear();
		}
		
		@Override
		public String toString() {
			return map.toString();
		}
	}
}
