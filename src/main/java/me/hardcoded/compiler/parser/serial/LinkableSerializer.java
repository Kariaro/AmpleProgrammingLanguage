package me.hardcoded.compiler.parser.serial;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.FuncParam;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.Position;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class LinkableSerializer {
	static final int MAGIC = 0x48434C46; // 'HCLF' HardCoded Linkable File
	
	private final RefPos<String> stringPositions;
	private final RefPos<Reference> references;
	private final RefPos<ISyntaxPosition> syntaxPositions;
	private final RefPos<ValueType> valueTypes;
	
	private LinkableSerializer() {
		this.references = new RefPos<>();
		this.syntaxPositions = new RefPos<>();
		this.valueTypes = new RefPos<>();
		this.stringPositions = new RefPos<>();
	}
	
	public static byte[] serializeLinkable(LinkableObject obj) {
		LinkableSerializer serializer = new LinkableSerializer();
		try {
			return serializer.serialize(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private byte[] serialize(LinkableObject obj) throws IOException {
		ByteArrayOutputStream full = new ByteArrayOutputStream();
		
		byte[] treeBytes = writeTree(obj);
		byte[] refsBytes = writeRefs();
		byte[] strnBytes = writeStrings();
		
		{
			// Write file
			DataOutputStream out = new DataOutputStream(full);
			out.writeInt(MAGIC);
			out.writeUTF(obj.getFile().getAbsolutePath());
		}
		
		full.writeBytes(strnBytes);
		full.writeBytes(refsBytes);
		full.writeBytes(treeBytes);
		
		stringPositions.clear();
		references.clear();
		syntaxPositions.clear();
		valueTypes.clear();
		return full.toByteArray();
	}
	
	private byte[] writeTree(LinkableObject obj) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		serializeStat(obj.getProgram(), new DataOutputStream(bs));
		return bs.toByteArray();
	}
	
	private byte[] writeRefs() throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bs);
		
		writeVarInt(references.list.size(), out);
		for (Reference reference : references.list) {
			writeReference(reference, out);
		}
		
		writeVarInt(syntaxPositions.list.size(), out);
		for (ISyntaxPosition syntaxPosition : syntaxPositions.list) {
			writeISyntaxPosition(syntaxPosition, out);
		}
		
		writeVarInt(valueTypes.list.size(), out);
		for (ValueType valueType : valueTypes.list) {
			writeValueType(valueType, out);
		}
		
		return bs.toByteArray();
	}
	
	private byte[] writeStrings() throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bs);
		
		writeVarInt(stringPositions.list.size(), out);
		for (String str : stringPositions.list) {
			out.writeUTF(str);
		}
		
		return bs.toByteArray();
	}
	
	private void writeVarInt(int value, DataOutputStream out) throws IOException {
		do {
			int temp = (value & 0b01111111);
			value >>>= 7;
			if (value != 0) {
				temp |= 0b10000000;
			}
			
			out.write(temp);
		} while (value != 0);
	}
	
	private void writeReference(Reference reference, DataOutputStream out) throws IOException {
		serializeString(reference.getName(), out);
		writeVarInt(reference.getUsages(), out);
		writeVarInt(reference.getId(), out);
		writeVarInt(reference.getFlags(), out);
	}
	
	private void writeISyntaxPosition(ISyntaxPosition syntaxPosition, DataOutputStream out) throws IOException {
		writePosition(syntaxPosition.getStartPosition(), out);
		writePosition(syntaxPosition.getEndPosition(), out);
	}
	
	private void writePosition(Position position, DataOutputStream out) throws IOException {
		serializeString(position.file == null ? "" : position.file.getAbsolutePath(), out);
		writeVarInt(position.column, out);
		writeVarInt(position.line, out);
		writeVarInt(position.offset, out);
	}
	
	private void writeValueType(ValueType valueType, DataOutputStream out) throws IOException {
		serializeString(valueType.getName(), out);
		writeVarInt(valueType.getFlags(), out);
		writeVarInt(valueType.getDepth(), out);
		writeVarInt(valueType.getSize(), out);
	}
	
	// Statements
	private void serializeStat(Stat stat, DataOutputStream out) throws IOException {
		TreeType type = stat.getTreeType();
		ISyntaxPosition syntaxPosition = stat.getSyntaxPosition();
		
		// Write statement header (TYPE, SYNTAX_POSITION)
		writeVarInt(type.ordinal(), out);
		serializeISyntaxPosition(syntaxPosition, out);
		
		switch (type) {
			/* Statements */
			case BREAK -> serializeBreakStat((BreakStat)stat, out);
			case CONTINUE -> serializeContinueStat((ContinueStat)stat, out);
			case EMPTY -> serializeEmptyStat((EmptyStat)stat, out);
			case FOR -> serializeForStat((ForStat)stat, out);
			case FUNC -> serializeFuncStat((FuncStat)stat, out);
			case GOTO -> serializeGotoStat((GotoStat)stat, out);
			case IF -> serializeIfStat((IfStat)stat, out);
			case LABEL -> serializeLabelStat((LabelStat)stat, out);
			case PROG -> serializeProgStat((ProgStat)stat, out);
			case RETURN -> serializeReturnStat((ReturnStat)stat, out);
			case SCOPE -> serializeScopeStat((ScopeStat)stat, out);
			case VAR -> serializeVarStat((VarStat)stat, out);
			case WHILE -> serializeWhileStat((WhileStat)stat, out);
			
			/* Expressions */
			case BINARY -> serializeBinaryExpr((BinaryExpr)stat, out);
			case CALL -> serializeCallExpr((CallExpr)stat, out);
			case CAST -> serializeCastExpr((CastExpr)stat, out);
			case COMMA -> serializeCommaExpr((CommaExpr)stat, out);
			case NAME -> serializeNameExpr((NameExpr)stat, out);
			case NULL -> serializeNullExpr((NullExpr)stat, out);
			case NUM -> serializeNumExpr((NumExpr)stat, out);
			case STR -> serializeStrExpr((StrExpr)stat, out);
			case UNARY -> serializeUnaryExpr((UnaryExpr)stat, out);
		}
	}
	
	private void serializeBreakStat(BreakStat stat, DataOutputStream out) throws IOException {
	
	}
	
	private void serializeContinueStat(ContinueStat stat, DataOutputStream out) throws IOException {
	
	}
	
	private void serializeEmptyStat(EmptyStat stat, DataOutputStream out) throws IOException {
	
	}
	
	private void serializeForStat(ForStat stat, DataOutputStream out) throws IOException {
		serializeStat(stat.getStart(), out);
		serializeStat(stat.getCondition(), out);
		serializeStat(stat.getAction(), out);
		serializeStat(stat.getBody(), out);
	}
	
	private void serializeFuncStat(FuncStat stat, DataOutputStream out) throws IOException {
		serializeValueType(stat.getReturnType(), out);
		serializeReference(stat.getReference(), out);
		List<FuncParam> parameters = stat.getParameters();
		writeVarInt(parameters.size(), out);
		for (FuncParam param : parameters) {
			serializeValueType(param.getType(), out);
			serializeReference(param.getReference(), out);
		}
		serializeStat(stat.getBody(), out);
	}
	
	private void serializeGotoStat(GotoStat stat, DataOutputStream out) throws IOException {
		serializeReference(stat.getReference(), out);
	}
	
	private void serializeIfStat(IfStat stat, DataOutputStream out) throws IOException {
		serializeStat(stat.getCondition(), out);
		serializeStat(stat.getBody(), out);
		serializeStat(stat.getElseBody(), out);
	}
	
	private void serializeLabelStat(LabelStat stat, DataOutputStream out) throws IOException {
		serializeReference(stat.getReference(), out);
	}
	
	private void serializeProgStat(ProgStat stat, DataOutputStream out) throws IOException {
		List<Stat> elements = stat.getElements();
		writeVarInt(elements.size(), out);
		for (Stat s : elements) {
			serializeStat(s, out);
		}
	}
	
	private void serializeReturnStat(ReturnStat stat, DataOutputStream out) throws IOException {
		serializeStat(stat.getValue(), out);
	}
	
	private void serializeScopeStat(ScopeStat stat, DataOutputStream out) throws IOException {
		List<Stat> elements = stat.getElements();
		writeVarInt(elements.size(), out);
		for (Stat s : elements) {
			serializeStat(s, out);
		}
	}
	
	private void serializeVarStat(VarStat stat, DataOutputStream out) throws IOException {
		serializeReference(stat.getReference(), out);
		serializeValueType(stat.getType(), out);
		serializeStat(stat.getValue(), out);
	}
	
	private void serializeWhileStat(WhileStat stat, DataOutputStream out) throws IOException {
		serializeStat(stat.getCondition(), out);
		serializeStat(stat.getBody(), out);
	}
	
	// Expressions
	private void serializeBinaryExpr(BinaryExpr expr, DataOutputStream out) throws IOException {
		serializeStat(expr.getLeft(), out);
		writeVarInt(expr.getOperation().ordinal(), out);
		serializeStat(expr.getRight(), out);
	}
	
	private void serializeCallExpr(CallExpr expr, DataOutputStream out) throws IOException {
		serializeStat(expr.getCaller(), out);
		List<Expr> parameters = expr.getParameters();
		writeVarInt(parameters.size(), out);
		for (Expr e : parameters) {
			serializeStat(e, out);
		}
	}
	
	private void serializeCastExpr(CastExpr expr, DataOutputStream out) throws IOException {
		serializeValueType(expr.getCastType(), out);
		serializeStat(expr.getValue(), out);
	}
	
	private void serializeCommaExpr(CommaExpr expr, DataOutputStream out) throws IOException {
		List<Expr> values = expr.getValues();
		writeVarInt(values.size(), out);
		for (Expr e : values) {
			serializeStat(e, out);
		}
	}
	
	private void serializeNameExpr(NameExpr expr, DataOutputStream out) throws IOException {
		serializeReference(expr.getReference(), out);
	}
	
	private void serializeNullExpr(NullExpr expr, DataOutputStream out) throws IOException {
	
	}
	
	private void serializeNumExpr(NumExpr expr, DataOutputStream out) throws IOException {
		// TODO: Save the number as a large number
		boolean isFloating = expr.getAtom().isFloating();
		out.writeBoolean(isFloating);
		writeVarInt(expr.getAtom().ordinal(), out);
		
		if (isFloating) {
			out.writeDouble(expr.getFloatingValue());
		} else {
			out.writeLong(expr.getIntegerValue());
		}
	}
	
	private void serializeStrExpr(StrExpr expr, DataOutputStream out) throws IOException {
		serializeString(expr.getValue(), out);
	}
	
	private void serializeUnaryExpr(UnaryExpr expr, DataOutputStream out) throws IOException {
		writeVarInt(expr.getOperation().ordinal(), out);
		serializeStat(expr.getValue(), out);
	}
	
	
	// Type serializers
	private void serializeReference(Reference reference, DataOutputStream out) throws IOException {
		int idx = references.put(reference);
		writeVarInt(idx, out);
	}
	
	private void serializeISyntaxPosition(ISyntaxPosition syntaxPosition, DataOutputStream out) throws IOException {
		int idx = syntaxPositions.put(syntaxPosition);
		writeVarInt(idx, out);
	}
	
	private void serializeValueType(ValueType valueType, DataOutputStream out) throws IOException {
		int idx = valueTypes.put(valueType);
		writeVarInt(idx, out);
	}
	
	private void serializeString(String string, DataOutputStream out) throws IOException {
		int idx = stringPositions.put(string);
		writeVarInt(idx, out);
	}
	
	private static class RefPos<T> {
		private final List<T> list;
		private final Map<T, Integer> map;
		
		private RefPos() {
			this.list = new ArrayList<>();
			this.map = new HashMap<>();
		}
		
		public int put(T elm) {
			Integer val = map.get(elm);
			
			if (val == null) {
				int size = list.size();
				map.put(elm, size);
				list.add(elm);
				return size;
			}
			
			return val;
		}
		
		public void clear() {
			map.clear();
			list.clear();
		}
	}
}
