package me.hardcoded.compiler.parser.serial;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ReferenceSyntax;
import me.hardcoded.compiler.parser.type.ValueType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LinkableStream {
	private LinkableHeader header;
	private DataInputStream streamIn;
	private DataOutputStream streamOut;
	
	public LinkableStream() {
		header = new LinkableHeader();
	}
	
	// Specific types
	public int readVarInt() throws IOException {
		return header.readVarInt(streamIn);
	}
	
	public void writeVarInt(int value) throws IOException {
		header.writeVarInt(value, streamOut);
	}
	
	public long readLong() throws IOException {
		return streamIn.readLong();
	}
	
	public void writeLong(long value) throws IOException {
		streamOut.writeLong(value);
	}
	
	
	// Type deserializers
	public String deserializeString() throws IOException {
		return header.deserializeString(streamIn);
	}
	
	public ValueType deserializeValueType() throws IOException {
		return header.deserializeValueType(streamIn);
	}
	
	public Namespace deserializeNamespace() throws IOException {
		return header.deserializeNamespace(streamIn);
	}
	
	public Reference deserializeReference() throws IOException {
		return header.deserializeReference(streamIn);
	}
	
	public ISyntaxPos deserializeISyntaxPosition() throws IOException {
		return header.deserializeISyntaxPosition(streamIn);
	}
	
	
	// Type serializers
	public void serializeString(String string) throws IOException {
		header.serializeString(string, streamOut);
	}
	
	public void serializeValueType(ValueType valueType) throws IOException {
		header.serializeValueType(valueType, streamOut);
	}
	
	public void serializeNamespace(Namespace namespace) throws IOException {
		header.serializeNamespace(namespace, streamOut);
	}
	
	public void serializeReference(Reference reference) throws IOException {
		header.serializeReference(reference, streamOut);
	}
	
	public void serializeISyntaxPosition(ISyntaxPos syntaxPosition) throws IOException {
		header.serializeISyntaxPosition(syntaxPosition, streamOut);
	}
	
	
	// Helpers
	public static record Header(TreeType type, ISyntaxPos syntaxPos) {
	}
	
	public void writeObjectHeader(Stat stat) throws IOException {
		header.writeVarInt(stat.getTreeType().ordinal(), streamOut);
		header.serializeISyntaxPosition(stat.getSyntaxPosition(), streamOut);
	}
	
	public Header readObjectHeader() throws IOException {
		TreeType type = TreeType.VALUES[header.readVarInt(streamIn)];
		ISyntaxPos syntaxPos = header.deserializeISyntaxPosition(streamIn);
		return new Header(type, syntaxPos);
	}
	
	public Expr deserializeExpr() throws IOException {
		return (Expr) deserializeStat();
	}
	
	public Stat deserializeStat() throws IOException {
		// Get type without overwriting
		streamIn.mark(100);
		Header head = readObjectHeader();
		streamIn.reset();
		
		return switch (head.type()) {
			/* Statements */
			case PROGRAM -> ProgStat.deserialize(this);
			case BREAK -> BreakStat.deserialize(this);
			case CONTINUE -> ContinueStat.deserialize(this);
			case EMPTY -> EmptyStat.deserialize(this);
			case FOR -> ForStat.deserialize(this);
			case FUNC -> FuncStat.deserialize(this);
			case IF -> IfStat.deserialize(this);
			case RETURN -> ReturnStat.deserialize(this);
			case SCOPE -> ScopeStat.deserialize(this);
			case VAR -> VarStat.deserialize(this);
			case COMPILER -> CompilerStat.deserialize(this);
			case WHILE -> WhileStat.deserialize(this);
			case NAMESPACE -> NamespaceStat.deserialize(this);
			case STRUCT -> StructStat.deserialize(this);
			
			/* Expressions */
			case STACK_ALLOC -> StackAllocExpr.deserialize(this);
			case BINARY -> BinaryExpr.deserialize(this);
			case CALL -> CallExpr.deserialize(this);
			case CAST -> CastExpr.deserialize(this);
			case NAME -> NameExpr.deserialize(this);
			case NONE -> NoneExpr.deserialize(this);
			case NUM -> NumExpr.deserialize(this);
			case STR -> StrExpr.deserialize(this);
			case UNARY -> UnaryExpr.deserialize(this);
			case SIZEOF -> SizeofExpr.deserialize(this);
			case BUILTIN -> BuiltinExpr.deserialize(this);
			
			default -> throw new RuntimeException("%s".formatted(head.type()));
		};
	}
	
	// Static
	private static final Logger LOGGER = LogManager.getLogger(LinkableStream.class);
	
	private byte[] writeContext(LinkableObject obj) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bs);
		
		header.writeVarInt(obj.getImports().size(), out);
		for (String str : obj.getImports()) {
			header.serializeString(str, out);
		}
		
		header.writeVarInt(obj.getExportedReferences().size(), out);
		for (ReferenceSyntax referenceSyntax : obj.getExportedReferences()) {
			header.serializeReference(referenceSyntax.getReference(), out);
			header.serializeISyntaxPosition(referenceSyntax.getSyntaxPosition(), out);
		}
		
		header.writeVarInt(obj.getImportedReferences().size(), out);
		for (ReferenceSyntax referenceSyntax : obj.getImportedReferences()) {
			header.serializeReference(referenceSyntax.getReference(), out);
			header.serializeISyntaxPosition(referenceSyntax.getSyntaxPosition(), out);
		}
		
		return bs.toByteArray();
	}
	
	private void readContext(List<String> imports, List<ReferenceSyntax> exportedReferences, List<ReferenceSyntax> importedReferences, DataInputStream in) throws IOException {
		for (int i = 0, size = header.readVarInt(in); i < size; i++) {
			imports.add(header.deserializeString(in));
		}
		
		for (int i = 0, size = header.readVarInt(in); i < size; i++) {
			Reference reference = header.deserializeReference(in);
			ISyntaxPos syntaxPosition = header.deserializeISyntaxPosition(in);
			exportedReferences.add(new ReferenceSyntax(reference, syntaxPosition));
		}
		
		for (int i = 0, size = header.readVarInt(in); i < size; i++) {
			Reference reference = header.deserializeReference(in);
			ISyntaxPos syntaxPosition = header.deserializeISyntaxPosition(in);
			importedReferences.add(new ReferenceSyntax(reference, syntaxPosition));
		}
	}
	
	public static byte[] serializeLinkable(LinkableObject obj) throws IOException {
		LinkableStream stream = new LinkableStream();
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		stream.streamOut = new DataOutputStream(bs);
		
		obj.getProgram().serialize(stream);
		
		byte[] treeBytes = bs.toByteArray();
		byte[] contextBytes = stream.writeContext(obj);
		
		ByteArrayOutputStream full = new ByteArrayOutputStream();
		
		{
			// Write file
			DataOutputStream out = new DataOutputStream(full);
			stream.header.setFile(obj.getFile());
			stream.header.setChecksum(obj.getChecksum());
			stream.header.writeHeader(out);
		}
		
		full.writeBytes(contextBytes);
		full.writeBytes(treeBytes);
		
		return full.toByteArray();
	}
	
	public static LinkableObject deserializeLinkable(byte[] bytes) {
		LinkableStream stream = new LinkableStream();
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
			DataInputStream in = new DataInputStream(bi);
			stream.streamIn = in;
			
			stream.header.readHeader(in);
			File file = stream.header.getFile();
			String checksum = stream.header.getChecksum();
			
			List<String> imports = new ArrayList<>();
			List<ReferenceSyntax> exportedReferences = new ArrayList<>();
			List<ReferenceSyntax> importedReferences = new ArrayList<>();
			stream.readContext(imports, exportedReferences, importedReferences, in);
			
			ProgStat program = (ProgStat) stream.deserializeStat();
			return new LinkableObject(file, checksum, program, imports, exportedReferences, importedReferences);
		} catch (IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		
		return null;
	}
	
}
