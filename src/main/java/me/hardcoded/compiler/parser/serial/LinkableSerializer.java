package me.hardcoded.compiler.parser.serial;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Reference;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class LinkableSerializer {
	private final LinkableHeader header;
	
	private LinkableSerializer() {
		this.header = new LinkableHeader();
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
		byte[] contextBytes = writeContext(obj);
		
		{
			// Write file
			DataOutputStream out = new DataOutputStream(full);
			header.setFile(obj.getFile());
			header.setChecksum(obj.getChecksum());
			header.writeHeader(out);
		}
		
		full.writeBytes(contextBytes);
		full.writeBytes(treeBytes);
		
		header.clear();
		return full.toByteArray();
	}
	
	private byte[] writeTree(LinkableObject obj) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		serializeStat(obj.getProgram(), new DataOutputStream(bs));
		return bs.toByteArray();
	}
	
	private byte[] writeContext(LinkableObject obj) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bs);
		
		header.writeVarInt(obj.getImports().size(), out);
		for (String str : obj.getImports()) {
			header.serializeString(str, out);
		}
		
		header.writeVarInt(obj.getExportedReferences().size(), out);
		for (Reference reference : obj.getExportedReferences()) {
			header.serializeReference(reference, out);
		}
		
		header.writeVarInt(obj.getImportedReferences().size(), out);
		for (Reference reference : obj.getImportedReferences()) {
			header.serializeReference(reference, out);
		}
		
		return bs.toByteArray();
	}
	
	// Statements
	private void serializeStat(Stat stat, DataOutputStream out) throws IOException {
		TreeType type = stat.getTreeType();
		ISyntaxPosition syntaxPosition = stat.getSyntaxPosition();
		
		// Write statement header (TYPE, SYNTAX_POSITION)
		header.writeVarInt(type.ordinal(), out);
		header.serializeISyntaxPosition(syntaxPosition, out);
		
		switch (type) {
			/* Statements */
			case PROGRAM -> serializeProgStat((ProgStat) stat, out);
			case BREAK -> serializeBreakStat((BreakStat) stat, out);
			case CONTINUE -> serializeContinueStat((ContinueStat) stat, out);
			case EMPTY -> serializeEmptyStat((EmptyStat) stat, out);
			case FOR -> serializeForStat((ForStat) stat, out);
			case FUNC -> serializeFuncStat((FuncStat) stat, out);
			//			case GOTO -> serializeGotoStat((GotoStat) stat, out);
			case IF -> serializeIfStat((IfStat) stat, out);
			//			case LABEL -> serializeLabelStat((LabelStat) stat, out);
			case RETURN -> serializeReturnStat((ReturnStat) stat, out);
			case SCOPE -> serializeScopeStat((ScopeStat) stat, out);
			case VAR -> serializeVarStat((VarStat) stat, out);
			case COMPILER -> serializeCompilerStat((CompilerStat) stat, out);
			//			case WHILE -> serializeWhileStat((WhileStat) stat, out);
			case NAMESPACE -> serializeNamespaceStat((NamespaceStat) stat, out);
			
			/* Expressions */
			case STACK_ALLOC -> serializeStackAllocExpr((StackAllocExpr) stat, out);
			case BINARY -> serializeBinaryExpr((BinaryExpr) stat, out);
			case CALL -> serializeCallExpr((CallExpr) stat, out);
			case CAST -> serializeCastExpr((CastExpr) stat, out);
			//			case COMMA -> serializeCommaExpr((CommaExpr) stat, out);
			case NAME -> serializeNameExpr((NameExpr) stat, out);
			case NONE -> serializeNoneExpr((NoneExpr) stat, out);
			case NUM -> serializeNumExpr((NumExpr) stat, out);
			case STRING -> serializeStrExpr((StrExpr) stat, out);
			case UNARY -> serializeUnaryExpr((UnaryExpr) stat, out);
			//			case CONDITIONAL -> serializeConditionalExpr((ConditionalExpr) stat, out);
			
			default -> throw new RuntimeException("%s".formatted(type));
		}
	}
	
	private void serializeBreakStat(BreakStat stat, DataOutputStream out) throws IOException {
	
	}
	
	private void serializeContinueStat(ContinueStat stat, DataOutputStream out) throws IOException {
	
	}
	
	private void serializeEmptyStat(EmptyStat stat, DataOutputStream out) throws IOException {
	
	}
	
	private void serializeForStat(ForStat stat, DataOutputStream out) throws IOException {
		serializeStat(stat.getInitializer(), out);
		serializeStat(stat.getCondition(), out);
		serializeStat(stat.getAction(), out);
		serializeStat(stat.getBody(), out);
	}
	
	private void serializeFuncStat(FuncStat stat, DataOutputStream out) throws IOException {
		header.serializeReference(stat.getReference(), out);
		List<Reference> parameters = stat.getParameters();
		header.writeVarInt(parameters.size(), out);
		for (Reference param : parameters) {
			header.serializeReference(param, out);
		}
		serializeStat(stat.getBody(), out);
	}
	
	private void serializeIfStat(IfStat stat, DataOutputStream out) throws IOException {
		serializeStat(stat.getValue(), out);
		serializeStat(stat.getBody(), out);
		serializeStat(stat.getElseBody(), out);
	}
	
	private void serializeProgStat(ProgStat stat, DataOutputStream out) throws IOException {
		List<Stat> elements = stat.getElements();
		header.writeVarInt(elements.size(), out);
		for (Stat s : elements) {
			serializeStat(s, out);
		}
	}
	
	private void serializeReturnStat(ReturnStat stat, DataOutputStream out) throws IOException {
		serializeStat(stat.getValue(), out);
	}
	
	private void serializeScopeStat(ScopeStat stat, DataOutputStream out) throws IOException {
		List<Stat> elements = stat.getElements();
		header.writeVarInt(elements.size(), out);
		for (Stat s : elements) {
			serializeStat(s, out);
		}
	}
	
	private void serializeVarStat(VarStat stat, DataOutputStream out) throws IOException {
		header.serializeReference(stat.getReference(), out);
		serializeStat(stat.getValue(), out);
	}
	
	private void serializeCompilerStat(CompilerStat stat, DataOutputStream out) throws IOException {
		header.serializeString(stat.getTargetType(), out);
		List<CompilerStat.Part> parts = stat.getParts();
		header.writeVarInt(parts.size(), out);
		for (CompilerStat.Part part : parts) {
			header.serializeISyntaxPosition(part.syntaxPosition(), out);
			header.serializeString(part.command(), out);
			List<Reference> references = part.references();
			header.writeVarInt(references.size(), out);
			for (Reference reference : references) {
				header.serializeReference(reference, out);
			}
		}
	}
	
	//	private void serializeWhileStat(WhileStat stat, DataOutputStream out) throws IOException {
	//		serializeStat(stat.getValue(), out);
	//		serializeStat(stat.getBody(), out);
	//	}
	
	private void serializeNamespaceStat(NamespaceStat stat, DataOutputStream out) throws IOException {
		header.serializeReference(stat.getReference(), out);
		
		List<Stat> elements = stat.getElements();
		header.writeVarInt(elements.size(), out);
		for (Stat s : elements) {
			serializeStat(s, out);
		}
	}
	
	// Expressions
	private void serializeStackAllocExpr(StackAllocExpr expr, DataOutputStream out) throws IOException {
		serializeStat(expr.getValue(), out);
		header.writeVarInt(expr.getSize(), out);
		header.serializeValueType(expr.getType(), out);
	}
	
	private void serializeBinaryExpr(BinaryExpr expr, DataOutputStream out) throws IOException {
		serializeStat(expr.getLeft(), out);
		header.writeVarInt(expr.getOperation().ordinal(), out);
		serializeStat(expr.getRight(), out);
	}
	
	private void serializeCallExpr(CallExpr expr, DataOutputStream out) throws IOException {
		header.serializeReference(expr.getReference(), out);
		List<Expr> parameters = expr.getParameters();
		header.writeVarInt(parameters.size(), out);
		for (Expr e : parameters) {
			serializeStat(e, out);
		}
	}
	
	private void serializeCastExpr(CastExpr expr, DataOutputStream out) throws IOException {
		header.serializeValueType(expr.getType(), out);
		serializeStat(expr.getValue(), out);
	}
	
	//	private void serializeCommaExpr(CommaExpr expr, DataOutputStream out) throws IOException {
	//		List<Expr> values = expr.getValues();
	//		writeVarInt(values.size(), out);
	//		for (Expr e : values) {
	//			serializeStat(e, out);
	//		}
	//	}
	
	private void serializeNameExpr(NameExpr expr, DataOutputStream out) throws IOException {
		header.serializeReference(expr.getReference(), out);
	}
	
	private void serializeNoneExpr(NoneExpr expr, DataOutputStream out) throws IOException {
	
	}
	
	private void serializeNumExpr(NumExpr expr, DataOutputStream out) throws IOException {
		header.serializeValueType(expr.getType(), out);
		out.writeLong(expr.getValue());
	}
	
	private void serializeStrExpr(StrExpr expr, DataOutputStream out) throws IOException {
		header.serializeString(expr.getValue(), out);
	}
	
	private void serializeUnaryExpr(UnaryExpr expr, DataOutputStream out) throws IOException {
		header.writeVarInt(expr.getOperation().ordinal(), out);
		serializeStat(expr.getValue(), out);
	}
	
	//	private void serializeConditionalExpr(ConditionalExpr expr, DataOutputStream out) throws IOException {
	//		writeVarInt(expr.getOperation().ordinal(), out);
	//		List<Expr> values = expr.getValues();
	//		writeVarInt(values.size(), out);
	//		for (Expr e : values) {
	//			serializeStat(e, out);
	//		}
	//	}
}
