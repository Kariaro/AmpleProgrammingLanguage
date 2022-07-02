package me.hardcoded.compiler.parser.serial;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LinkableDeserializer {
	private final LinkableHeader header;

	private LinkableDeserializer() {
		this.header = new LinkableHeader();
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

		header.readHeader(in);
		File file = header.getFile();
		
		List<String> imports = new ArrayList<>();
		List<Reference> exportedReferences = new ArrayList<>();
		List<Reference> importedReferences = new ArrayList<>();
		readContext(imports, exportedReferences, importedReferences, in);

		ProgStat program = (ProgStat) readTree(in);
		return new LinkableObject(file, program, imports, exportedReferences, importedReferences);
	}

	private Stat readTree(DataInputStream in) throws IOException {
		return deserializeStat(in);
	}

	private void readContext(List<String> imports, List<Reference> exportedReferences, List<Reference> importedReferences, DataInputStream in) throws IOException {
		for (int i = 0, size = header.readVarInt(in); i < size; i++) {
			imports.add(header.deserializeString(in));
		}
		
		for (int i = 0, size = header.readVarInt(in); i < size; i++) {
			exportedReferences.add(header.deserializeReference(in));
		}
		
		for (int i = 0, size = header.readVarInt(in); i < size; i++) {
			importedReferences.add(header.deserializeReference(in));
		}
	}

	// Statements
	private Stat deserializeStat(DataInputStream in) throws IOException {
		TreeType type = TreeType.VALUES[header.readVarInt(in)];

		return switch (type) {
			/* Statements */
			case PROGRAM -> deserializeProgStat(in);
//			case BREAK -> deserializeBreakStat(in);
//			case CONTINUE -> deserializeContinueStat(in);
			case EMPTY -> deserializeEmptyStat(in);
			case FOR -> deserializeForStat(in);
			case FUNC -> deserializeFuncStat(in);
			case IF -> deserializeIfStat(in);
			case RETURN -> deserializeReturnStat(in);
			case SCOPE -> deserializeScopeStat(in);
			case VAR -> deserializeVarStat(in);
			case COMPILER -> deserializeCompilerStat(in);
//			case WHILE -> deserializeWhileStat(in);
//			case NAMESPACE -> deserializeNamespaceStat(in);

			/* Expressions */
			case STACK_ALLOC -> deserializeStackAllocExpr(in);
			case BINARY -> deserializeBinaryExpr(in);
			case CALL -> deserializeCallExpr(in);
			case CAST -> deserializeCastExpr(in);
//			case COMMA -> deserializeCommaExpr(in);
			case NAME -> deserializeNameExpr(in);
			case NONE -> deserializeNoneExpr(in);
//			case NONE -> deserializeNoneExpr(in);
			case NUM -> deserializeNumExpr(in);
			case STRING -> deserializeStrExpr(in);
			case UNARY -> deserializeUnaryExpr(in);
			
			default -> throw new RuntimeException("%s".formatted(type));
		};
	}
	
	private Expr deserializeExpr(DataInputStream in) throws IOException {
		return (Expr) deserializeStat(in);
	}

//	private BreakStat deserializeBreakStat(DataInputStream in) throws IOException {
//		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
//		return new BreakStat(syntaxPosition);
//	}
//
//	private ContinueStat deserializeContinueStat(DataInputStream in) throws IOException {
//		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
//		return new ContinueStat(syntaxPosition);
//	}

	private EmptyStat deserializeEmptyStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		return new EmptyStat(syntaxPosition);
	}

	private ForStat deserializeForStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Stat initializer = deserializeStat(in);
		Expr condition = deserializeExpr(in);
		Expr action = deserializeExpr(in);
		Stat body = deserializeStat(in);
		return new ForStat(syntaxPosition, initializer, condition, action, body);
	}

	private FuncStat deserializeFuncStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Reference reference = header.deserializeReference(in);

		List<Reference> parameters = new ArrayList<>();
		int size = header.readVarInt(in);
		for (int i = 0; i < size; i++) {
			Reference paramReference = header.deserializeReference(in);
			parameters.add(paramReference);
		}

		Stat body = deserializeStat(in);
		FuncStat result = new FuncStat(syntaxPosition, parameters, reference);
		result.setBody(body);
		return result;
	}

//	private GotoStat deserializeGotoStat(DataInputStream in) throws IOException {
//		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
//		Reference reference = deserializeReference(in);
//		return new GotoStat(reference, syntaxPosition);
//	}

	private IfStat deserializeIfStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Expr condition = deserializeExpr(in);
		Stat body = deserializeStat(in);
		Stat elseBody = deserializeStat(in);
		return new IfStat(syntaxPosition, condition, body, elseBody);
	}

//	private LabelStat deserializeLabelStat(DataInputStream in) throws IOException {
//		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
//		Reference reference = header.deserializeReference(in);
//		return new LabelStat(reference, syntaxPosition);
//	}

	private ProgStat deserializeProgStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);

		ProgStat result = new ProgStat(syntaxPosition);
		int size = header.readVarInt(in);
		for (int i = 0; i < size; i++) {
			result.addElement(deserializeStat(in));
		}

		return result;
	}

	private ReturnStat deserializeReturnStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Expr value = deserializeExpr(in);
		return new ReturnStat(syntaxPosition, value);
	}

	private ScopeStat deserializeScopeStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);

		ScopeStat result = new ScopeStat(syntaxPosition);
		int size = header.readVarInt(in);
		for (int i = 0; i < size; i++) {
			result.addElement(deserializeStat(in));
		}

		return result;
	}

	private VarStat deserializeVarStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Reference reference = header.deserializeReference(in);
		Expr value = deserializeExpr(in);
		return new VarStat(syntaxPosition, reference, value);
	}
	
	private CompilerStat deserializeCompilerStat(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		String targetType = header.deserializeString(in);
		List<CompilerStat.Part> parts = new ArrayList<>();
		int size = header.readVarInt(in);
		for (int i = 0; i < size; i++) {
			ISyntaxPosition partSyntaxPosition = header.deserializeISyntaxPosition(in);
			String command = header.deserializeString(in);
			int count = header.readVarInt(in);
			List<Reference> references = new ArrayList<>();
			for (int j = 0; j < count; j++) {
				references.add(header.deserializeReference(in));
			}
			parts.add(new CompilerStat.Part(partSyntaxPosition, command, references));
		}
		
		return new CompilerStat(syntaxPosition, targetType, parts);
	}

//	private WhileStat deserializeWhileStat(DataInputStream in) throws IOException {
//		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
//		Expr condition = deserializeExpr(in);
//		Stat body = deserializeStat(in);
//		return new WhileStat(condition, body, syntaxPosition);
//	}

//	private NamespaceStat deserializeNamespaceStat(DataInputStream in) throws IOException {
//		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
//		Reference reference = deserializeReference(in);
//
//		NamespaceStat result = new NamespaceStat(reference, syntaxPosition);
//		int size = readVarInt(in);
//		for (int i = 0; i < size; i++) {
//			result.addElement(deserializeStat(in));
//		}
//
//		return result;
//	}

	// Expressions
	private StackAllocExpr deserializeStackAllocExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Expr value = deserializeExpr(in);
		int size = header.readVarInt(in);
		ValueType type = header.deserializeValueType(in);
		return new StackAllocExpr(syntaxPosition, type, size, value);
	}
	
	private BinaryExpr deserializeBinaryExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Expr left = deserializeExpr(in);
		Operation operation = Operation.VALUES[header.readVarInt(in)];
		Expr right = deserializeExpr(in);
		return new BinaryExpr(syntaxPosition, operation, left, right);
	}

	private CallExpr deserializeCallExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Reference reference = header.deserializeReference(in);
		
		List<Expr> parameters = new ArrayList<>();
		int size = header.readVarInt(in);
		for (int i = 0; i < size; i++) {
			parameters.add(deserializeExpr(in));
		}
		
		return new CallExpr(syntaxPosition, reference, parameters);
	}

	private CastExpr deserializeCastExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		ValueType type = header.deserializeValueType(in);
		Expr value = deserializeExpr(in);
		return new CastExpr(syntaxPosition, type, value);
	}

//	private CommaExpr deserializeCommaExpr(DataInputStream in) throws IOException {
//		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
//		List<Expr> values = new ArrayList<>();
//		int size = readVarInt(in);
//		for (int i = 0; i < size; i++) {
//			values.add((Expr) deserializeStat(in));
//		}
//		return new CommaExpr(values, syntaxPosition);
//	}

	private NameExpr deserializeNameExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Reference reference = header.deserializeReference(in);
		return new NameExpr(syntaxPosition, reference);
	}

	private NoneExpr deserializeNoneExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		return new NoneExpr(syntaxPosition);
	}

	private NumExpr deserializeNumExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		ValueType type = header.deserializeValueType(in);
		long value = in.readLong();
		return new NumExpr(syntaxPosition, type, value);
	}

	private StrExpr deserializeStrExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		String string = header.deserializeString(in);
		return new StrExpr(syntaxPosition, string);
	}

	private UnaryExpr deserializeUnaryExpr(DataInputStream in) throws IOException {
		ISyntaxPosition syntaxPosition = header.deserializeISyntaxPosition(in);
		Operation operation = Operation.VALUES[header.readVarInt(in)];
		Expr value = deserializeExpr(in);
		return new UnaryExpr(syntaxPosition, operation, value);
	}

//	private ConditionalExpr deserializeConditionalExpr(DataInputStream in) throws IOException {
//		ISyntaxPosition syntaxPosition = deserializeISyntaxPosition(in);
//		Operation operation = Operation.VALUES[readVarInt(in)];
//
//		ConditionalExpr result = new ConditionalExpr(operation, syntaxPosition);
//
//		int size = readVarInt(in);
//		for (int i = 0; i < size; i++) {
//			result.addElement((Expr) deserializeStat(in));
//		}
//
//		return result;
//	}
}
