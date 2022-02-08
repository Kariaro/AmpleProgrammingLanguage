package me.hardcoded.compiler.parser.serial;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Atom;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.*;
import me.hardcoded.utils.Position;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkableDeserializer {
	private final Map<Integer, String> stringMap;
	private final Map<Integer, Reference> referenceMap;
	private final Map<Integer, ISyntaxPosition> syntaxPositionMap;
	private final Map<Integer, ValueType> valueTypeMap;
	
	private LinkableDeserializer() {
		this.stringMap = new HashMap<>();
		this.referenceMap = new HashMap<>();
		this.syntaxPositionMap = new HashMap<>();
		this.valueTypeMap = new HashMap<>();
	}
	
	public static LinkableObject deserializeLinkable(byte[] bytes) {
		LinkableDeserializer deserializer = new LinkableDeserializer();
		try {
			return deserializer.deserializer(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private LinkableObject deserializer(byte[] bytes) throws IOException {
		ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
		DataInputStream in = new DataInputStream(bi);
		
		int magic = in.readInt();
		if (magic != LinkableSerializer.MAGIC) {
			throw new IOException("Magic value did not match");
		}
		
		File file = new File(in.readUTF());
		
		readStrings(in);
		readRefs(in);
		List<String> imports = new ArrayList<>();
		List<Reference> exportedReferences = new ArrayList<>();
		List<Reference> importedRefereces = new ArrayList<>();
		readContext(imports, exportedReferences, importedRefereces, in);
		
		ProgStat program = (ProgStat)readTree(in);
		return new LinkableObject(file, program, imports, exportedReferences, importedRefereces);
	}
	
	private Stat readTree(DataInputStream in) throws IOException {
		return deserializeStat(in);
	}
	
	private void readRefs(DataInputStream in) throws IOException {
		int size;
		
		size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			Reference reference = readReference(in);
			referenceMap.put(i, reference);
		}
		
		size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			ValueType valueType = readValueType(in);
			valueTypeMap.put(i, valueType);
		}
		
		size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			ISyntaxPosition syntaxPosition = readISyntaxPosition(in);
			syntaxPositionMap.put(i, syntaxPosition);
		}
	}
	
	private void readContext(List<String> imports, List<Reference> exportedReferences, List<Reference> importedRefereces, DataInputStream in) throws IOException {
		int size;
		
		size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			imports.add(deserializeString(in));
		}
		
		size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			exportedReferences.add(deserializeReference(in));
		}
		
		size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			importedRefereces.add(deserializeReference(in));
		}
	}
	
	private void readStrings(DataInputStream in) throws IOException {
		int size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			String string = in.readUTF();
			stringMap.put(i, string);
		}
	}
	
	private int readVarInt(DataInputStream in) throws IOException {
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
	
	private Reference readReference(DataInputStream in) throws IOException {
		String name = deserializeString(in);
		// TODO: Cache value types?
		ValueType valueType = readValueType(in);
		int usages = readVarInt(in);
		int id = readVarInt(in);
		int flags = readVarInt(in);
		return new Reference(name, valueType, id, flags, usages);
	}
	
	private ISyntaxPosition readISyntaxPosition(DataInputStream in) throws IOException {
		Position start = readPosition(in);
		Position end = readPosition(in);
		return ISyntaxPosition.of(start, end);
	}
	
	private Position readPosition(DataInputStream in) throws IOException {
		String name = deserializeString(in);
		int column = readVarInt(in);
		int line = readVarInt(in);
		int offset = readVarInt(in);
		return new Position(new File(name), column, line, offset);
	}
	
	private ValueType readValueType(DataInputStream in) throws IOException {
		String name = deserializeString(in);
		int flags = readVarInt(in);
		int depth = readVarInt(in);
		int size = readVarInt(in);
		return new ValueType(name, size, depth, flags);
	}
	
	// Statements
	private Stat deserializeStat(DataInputStream in) throws IOException {
		TreeType type = TreeType.VALUES[readVarInt(in)];
		
		return switch (type) {
			/* Statements */
			case BREAK -> deserializeBreakStat(in);
			case CONTINUE -> deserializeContinueStat(in);
			case EMPTY -> deserializeEmptyStat(in);
			case FOR -> deserializeForStat(in);
			case FUNC -> deserializeFuncStat(in);
			case GOTO -> deserializeGotoStat(in);
			case IF -> deserializeIfStat(in);
			case LABEL -> deserializeLabelStat(in);
			case PROG -> deserializeProgStat(in);
			case RETURN -> deserializeReturnStat(in);
			case SCOPE -> deserializeScopeStat(in);
			case VAR -> deserializeVarStat(in);
			case WHILE -> deserializeWhileStat(in);
			case NAMESPACE -> deserializeNamespaceStat(in);
			
			/* Expressions */
			case BINARY -> deserializeBinaryExpr(in);
			case CALL -> deserializeCallExpr(in);
			case CAST -> deserializeCastExpr(in);
			case COMMA -> deserializeCommaExpr(in);
			case NAME -> deserializeNameExpr(in);
			case NULL -> deserializeNullExpr(in);
			case NUM -> deserializeNumExpr(in);
			case STR -> deserializeStrExpr(in);
			case UNARY -> deserializeUnaryExpr(in);
			case CONDITIONAL -> deserializeConditionalExpr(in);
		};
	}
	
	private BreakStat deserializeBreakStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		return new BreakStat(syntaxPosition);
	}
	
	private ContinueStat deserializeContinueStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		return new ContinueStat(syntaxPosition);
	}
	
	private EmptyStat deserializeEmptyStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		return new EmptyStat(syntaxPosition);
	}
	
	private ForStat deserializeForStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Stat start = deserializeStat(in);
		Expr condition = (Expr)deserializeStat(in);
		Expr action = (Expr)deserializeStat(in);
		Stat body = deserializeStat(in);
		return new ForStat(start, condition, action, body, syntaxPosition);
	}
	
	private FuncStat deserializeFuncStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Reference reference = deserializeReference(in);
		
		List<Reference> parameters = new ArrayList<>();
		int size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			Reference paramReference = deserializeReference(in);
			parameters.add(paramReference);
		}
		
		Stat body = deserializeStat(in);
		FuncStat result = new FuncStat(reference, parameters, syntaxPosition);
		result.complete(body);
		return result;
	}
	
	private GotoStat deserializeGotoStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Reference reference = deserializeReference(in);
		return new GotoStat(reference, syntaxPosition);
	}
	
	private IfStat deserializeIfStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Expr condition = (Expr)deserializeStat(in);
		Stat body = deserializeStat(in);
		Stat elseBody = deserializeStat(in);
		return new IfStat(condition, body, elseBody, syntaxPosition);
	}
	
	private LabelStat deserializeLabelStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Reference reference = deserializeReference(in);
		return new LabelStat(reference, syntaxPosition);
	}
	
	private ProgStat deserializeProgStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		
		ProgStat result = new ProgStat(syntaxPosition);
		int size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			result.addElement(deserializeStat(in));
		}
		
		return result;
	}
	
	private ReturnStat deserializeReturnStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Expr value = (Expr)deserializeStat(in);
		return new ReturnStat(value, syntaxPosition);
	}
	
	private ScopeStat deserializeScopeStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		
		ScopeStat result = new ScopeStat(syntaxPosition);
		int size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			result.addElement(deserializeStat(in));
		}
		
		return result;
	}
	
	private VarStat deserializeVarStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Reference reference = deserializeReference(in);
		Expr value = (Expr)deserializeStat(in);
		return new VarStat(reference, value, syntaxPosition);
	}
	
	private WhileStat deserializeWhileStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Expr condition = (Expr)deserializeStat(in);
		Stat body = deserializeStat(in);
		return new WhileStat(condition, body, syntaxPosition);
	}
	
	private NamespaceStat deserializeNamespaceStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Reference reference = deserializeReference(in);
		
		NamespaceStat result = new NamespaceStat(reference, syntaxPosition);
		int size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			result.addElement(deserializeStat(in));
		}
		
		return result;
	}
	
	// Expressions
	private BinaryExpr deserializeBinaryExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Expr left = (Expr)deserializeStat(in);
		Operation operation = Operation.VALUES[readVarInt(in)];
		Expr right = (Expr)deserializeStat(in);
		return new BinaryExpr(left, right, operation, syntaxPosition);
	}
	
	private CallExpr deserializeCallExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Expr caller = (Expr)deserializeStat(in);
		
		List<Expr> parameters = new ArrayList<>();
		int size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			parameters.add((Expr)deserializeStat(in));
		}
		return new CallExpr(caller, parameters, syntaxPosition);
	}
	
	private CastExpr deserializeCastExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		ValueType valueType = deserializeValueType(in);
		Expr value = (Expr)deserializeStat(in);
		return new CastExpr(value, valueType, syntaxPosition);
	}
	
	private CommaExpr deserializeCommaExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		List<Expr> values = new ArrayList<>();
		int size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			values.add((Expr) deserializeStat(in));
		}
		return new CommaExpr(values, syntaxPosition);
	}
	
	private NameExpr deserializeNameExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Reference reference = deserializeReference(in);
		return new NameExpr(reference, syntaxPosition);
	}
	
	private NullExpr deserializeNullExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		return new NullExpr(syntaxPosition);
	}
	
	private NumExpr deserializeNumExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		boolean isFloating = in.readBoolean();
		Atom atom = Atom.VALUES[readVarInt(in)];
		
		if (isFloating) {
			double value = in.readDouble();
			return new NumExpr(value, atom, syntaxPosition);
		} else {
			long value = in.readLong();
			return new NumExpr(value, atom, syntaxPosition);
		}
	}
	
	private StrExpr deserializeStrExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		String string = deserializeString(in);
		return new StrExpr(string, syntaxPosition);
	}
	
	private UnaryExpr deserializeUnaryExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Operation operation = Operation.VALUES[readVarInt(in)];
		Expr value = (Expr)deserializeStat(in);
		return new UnaryExpr(value, operation, syntaxPosition);
	}
	
	private ConditionalExpr deserializeConditionalExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
		Operation operation = Operation.VALUES[readVarInt(in)];
		
		ConditionalExpr result = new ConditionalExpr(operation, syntaxPosition);
		
		int size = readVarInt(in);
		for (int i = 0; i < size; i++) {
			result.addElement((Expr) deserializeStat(in));
		}
		
		return result;
	}
	
	// Type deserializers
	private Reference deserializeReference(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		return referenceMap.get(idx);
	}
	
	private ISyntaxPosition deserializeISyntaxPosition(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		return syntaxPositionMap.get(idx);
	}
	
	private ValueType deserializeValueType(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		return valueTypeMap.get(idx);
	}
	
	private String deserializeString(DataInputStream in) throws IOException {
		int idx = readVarInt(in);
		return stringMap.get(idx);
	}
}
