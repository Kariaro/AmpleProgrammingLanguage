package com.hardcoded.compiler.impl.serial;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.hardcoded.compiler.api.AtomType;
import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.context.IRefContainer;
import com.hardcoded.compiler.impl.context.LinkerScope;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.expression.*;
import com.hardcoded.compiler.impl.statement.*;
import com.hardcoded.compiler.lexer.Token;

/**
 * A serializable class to write and read parse trees
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class SerialParseTree {
	private Statement stat;
	private LinkerScope link;
	
	private SerialParseTree() {
		
	}
	
	public Statement getStatement() {
		return stat;
	}
	
	public LinkerScope getLinkerScope() {
		return link;
	}
	
	public static SerialParseTree read(InputStream stream) {
		System.out.println("---------------------------------------------");
		SerialParseTree serial = new SerialParseTree();
		serial.read(new SerialIn(stream));
		return serial;
	}
	
	public static SerialParseTree write(OutputStream stream, Statement stat, LinkerScope link) {
		System.out.println("---------------------------------------------");
		SerialParseTree serial = new SerialParseTree();
		serial.stat = stat;
		serial.link = link;
		serial.write(new SerialOut(stream));
		return serial;
	}
	
	void write(SerialOut out) {
		SerialOut out_stat = new SerialOut();
		writeLink(link, out_stat);
		writeStat(stat, out_stat);
		
		SerialOut out_tref = new SerialOut(out_stat); {
			Map<Token, Integer> tokens = out_tref.getTokens();
			out_tref.writePackedInt(tokens.size());
			for(Token token : tokens.keySet()) {
				out_tref.writeTokenPtr(token);
			}
			
			Map<Reference, Integer> references = out_tref.getReferences();
			out_tref.writePackedInt(references.size());
			for(Reference ref : references.keySet()) {
				out_tref.writeReferencePtr(ref);
			}
		}
		
		{
			Map<String, Integer> strings = out_tref.getStrings();
			out.writePackedInt(strings.size());
			for(String string : strings.keySet()) {
				out.writeUTF(string);
			}
		}
		
		out.writeBytes(out_tref.toByteArray());
		out.writeBytes(out_stat.toByteArray());
	}
	
	private void writeLink(LinkerScope link, SerialOut out) {
		List<String> imported_files = link.getImportedFiles();
		out.writePackedInt(imported_files.size());
		for(String str : imported_files) {
			out.writeString(str);
		}
		
		List<Reference> list_export = link.getExport();
		out.writePackedInt(list_export.size());
		for(Reference ref : list_export) {
			out.writeReference(ref);
		}
		
		List<Reference> list_import = link.getImport();
		out.writePackedInt(list_import.size());
		for(Reference ref : list_import) {
			out.writeReference(ref);
		}
		
		List<Reference> list_global = link.getGlobals();
		out.writePackedInt(list_global.size());
		for(Reference ref : list_global) {
			out.writeReference(ref);
		}
	}
	
	private void writeStat(Statement stat, SerialOut out) {
		// System.out.printf("Writing: [%s], %s\n", stat.getType(), stat);
		if(stat.getType() == Statement.Type.NONE) {
			out.writeEnum(Statement.Type.NONE);
			return;
		}
		
		out.writeEnum(stat.getType());
		Class<?> clazz = stat.getClass();
		String func = "write_" + stat.getType();
		if(stat instanceof Stat) {
			Stat s = (Stat)stat;
			out.writePackedInt(s.getStartOffset());
			out.writePackedInt(s.getEndOffset());
		}
		
		try {
			Method method = SerialParseTree.class.getDeclaredMethod(func, clazz, SerialOut.class);
			method.invoke(this, stat, out);
		} catch(NoSuchMethodException e) {
			e.printStackTrace();
		} catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	void writeList(Statement stat, SerialOut out) {
		List<Statement> list = stat.getStatements();
		out.writePackedInt(list.size());
		for(Statement s : list) writeStat(s, out);
	}
	
	<T extends Statement> void writeList(List<T> list, SerialOut out) {
		out.writePackedInt(list.size());
		for(Statement s : list) writeStat(s, out);
	}
	
	// STATEMENTS
	void write_PROGRAM(ProgramStat stat, SerialOut out) {
		writeList(stat, out);
	}
	
	void write_FUNCTION(FuncStat stat, SerialOut out) {
		out.writeToken(stat.getReturnType());
		writeIRef(stat, out);
		writeList(stat.getArguments(), out);
		writeList(stat.getStatements(), out);
	}
	
	void write_DEFINE(DefineStat stat, SerialOut out) {
		out.writeToken(stat.getValueType());
		writeIRef(stat, out);
		writeList(stat, out);
	}
	
	void write_EXPR(ExprStat stat, SerialOut out) {
		writeExprList(stat.getExpressions(), out);
	}
	
	void write_IMPORT(ImportStat stat, SerialOut out) {
		out.writeToken(stat.getPath());
	}
	
	void write_CLASS(ClassStat stat, SerialOut out) {
		writeIRef(stat, out);
		writeList(stat, out);
	}
	
	void write_SCOPE(ScopeStat stat, SerialOut out) {
		writeList(stat, out);
	}
	
	void write_FOR(ForStat stat, SerialOut out) {
		writeList(stat, out);
	}
	
	void write_WHILE(WhileStat stat, SerialOut out) {
		writeList(stat, out);
	}
	
	void write_IF(IfStat stat, SerialOut out) {
		writeList(stat, out);
	}
	
	void write_IF_ELSE(IfElseStat stat, SerialOut out) {
		writeList(stat, out);
	}
	
	void write_DO_WHILE(DoWhileStat stat, SerialOut out) {
		writeList(stat, out);
	}
	
	void write_GOTO(GotoStat stat, SerialOut out) {
		writeIRef(stat, out);
	}
	
	void write_LABEL(LabelStat stat, SerialOut out) {
		writeIRef(stat, out);
	}
	
	void write_CONTINUE(ContinueStat stat, SerialOut out) {}
	void write_BREAK(BreakStat stat, SerialOut out) {}
	
	void write_RETURN(ReturnStat stat, SerialOut out) {
		writeList(stat, out);
	}
	
	void writeExprList(List<Expression> list, SerialOut out){
		out.writePackedInt(list.size());
		for(Expression e : list) writeExpr(e, out);
	}
	
	void writeExpr(Expression expr, SerialOut out) {
		if(EmptyExpr.isEmpty(expr)) {
			out.writeByte(0);
			return;
		}
		
		int start = expr.getStartOffset();
		int len = expr.getEndOffset() - start;
		
		if(expr instanceof AtomExpr) {
			AtomExpr e = (AtomExpr)expr;
			out.writeByte(1);
			out.writePackedInt(start);
			out.writePackedInt(len);
			out.writeToken(e.getRefToken());
			out.writeEnum(e.getAtomType());
			if(e.isNumber()) out.writeDouble(e.getNumber());
			if(e.isReference()) out.writeReference(e.getReference());
			if(e.isString()) out.writeString(e.getString());
			return;
		}
		
		if(expr instanceof UnaryExpr) {
			UnaryExpr e = (UnaryExpr)expr;
			out.writeByte(2);
			out.writePackedInt(start);
			out.writePackedInt(len);
			out.writeEnum(e.getType());
			writeExprList(expr.getExpressions(), out);
			return;
		}
		
		if(expr instanceof BinaryExpr) {
			BinaryExpr e = (BinaryExpr)expr;
			out.writeByte(3);
			out.writePackedInt(start);
			out.writePackedInt(len);
			out.writeEnum(e.getType());
			writeExprList(expr.getExpressions(), out);
			return;
		}
		
		throw new SerialException("Invalid expression: " + ((expr == null) ? "<null>":expr.getClass()));
	}
	
	void writeIRef(IRefContainer ref, SerialOut out) {
		out.writeToken(ref.getRefToken());
		out.writeReference(ref.getReference());
	}
	
	
	// READING
	private void read(SerialIn in) {
		// TABLES
		in.readStringTable();
		in.readTokenTable();
		in.readReferenceTable();
		
		link = readLink(in);
		stat = readStat(in);
	}
	
	private LinkerScope readLink(SerialIn in) {
		LinkerScope link = new LinkerScope();
		int len = in.readPackedInt();
		for(int i = 0; i < len; i++) {
			link.addImportedFile(in.readString());
		}
		
		len = in.readPackedInt();
		for(int i = 0; i < len; i++) {
			link.addDirectExported(in.readReference());
		}

		len = in.readPackedInt();
		for(int i = 0; i < len; i++) {
			link.addDirectImported(in.readReference());
		}
		
		len = in.readPackedInt();
		for(int i = 0; i < len; i++) {
			link.addDirectGlobal(in.readReference());
		}
		
		return link;
	}
	
	private Statement readStat(SerialIn in) {
		Statement.Type type = in.readEnum(Statement.Type.class);
		//System.out.printf("Reading: [%s]\n", type);
		
		if(type == Statement.Type.NONE) {
			return EmptyStat.get();
		}
		
		String func = "read_" + type;
		
		try {
			Method method = SerialParseTree.class.getDeclaredMethod(func, SerialIn.class);
			return (Statement)method.invoke(this, in);
		} catch(NoSuchMethodException e) {
			e.printStackTrace();
			
			throw new SerialException(e);
		} catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			
			if(e.getCause() instanceof SerialException) {
				throw (SerialException)e.getCause();
			}
			
			throw new SerialException(e);
		}
		
		// Throw exception
		// return EmptyStat.get();
	}
	
	void readList(Statement stat, SerialIn in) {
		List<Statement> list = stat.getStatements();
		int len = in.readPackedInt();
		for(int i = 0; i < len; i++) {
			list.add(readStat(in));
		}
	}
	
	@SuppressWarnings("unchecked")
	<T extends Statement> void readList(List<T> list, SerialIn in) {
		int len = in.readPackedInt();
		for(int i = 0; i < len; i++) {
			list.add((T)readStat(in));
		}
	}
	
	void readExprList(List<Expression> list, SerialIn in) {
		int len = in.readPackedInt();
		for(int i = 0; i < len; i++) {
			list.add(readExpr(in));
		}
	}
	
	Expression readExpr(SerialIn in) {
		switch(in.readUnsignedByte()) {
			case 0: return EmptyExpr.get(); // EmptyExpr
			case 1: { // AtomExpr
				int start = in.readPackedInt();
				int len = in.readPackedInt();
				Token token = in.readToken();
				AtomExpr atom = null;
				switch(in.readEnum(AtomType.class)) {
					case NUMBER: atom = AtomExpr.get(token, in.readDouble()); break;
					case REF: atom = AtomExpr.get(token, in.readReference()); break;
					case STRING: atom = AtomExpr.get(token, in.readString()); break;
					default: throw new SerialException("Invalid atom expr");
				}
				
				atom.setLocation(start, start + len);
				return atom;
			}
			case 2: { // UnaryExpr
				int start = in.readPackedInt();
				int len = in.readPackedInt();
				Expression.Type type = in.readEnum(Expression.Type.class);
				UnaryExpr expr = UnaryExpr.get(type, Token.EMPTY);
				expr.setLocation(start, start + len);
				readExprList(expr.getExpressions(), in);
				return expr;
			}
			case 3: { // BinaryExpr
				int start = in.readPackedInt();
				int len = in.readPackedInt();
				Expression.Type type = in.readEnum(Expression.Type.class);
				BinaryExpr expr = BinaryExpr.get(type, Token.EMPTY);
				expr.setLocation(start, start + len);
				readExprList(expr.getExpressions(), in);
				return expr;
			}
		}
		
		throw new SerialException("Invalid expression");
	}
	
	ProgramStat read_PROGRAM(SerialIn in) {
		ProgramStat stat = ProgramStat.get();
		readList(stat, in);
		return stat;
	}
	
	DefineStat read_DEFINE(SerialIn in) {
		int start = in.readPackedInt();
		int end = in.readPackedInt();
		
		Token value_type = in.readToken();
		Token token = in.readToken();
		Reference ref = in.readReference();
		
		DefineStat stat = DefineStat.get(value_type, token);
		stat.setLocation(start, end);
		stat.setReference(ref);
		readList(stat, in);
		
		return stat;
	}
	
	ExprStat read_EXPR(SerialIn in) {
		ExprStat stat = ExprStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		readExprList(stat.getExpressions(), in);
		
		return stat;
	}
	
	ImportStat read_IMPORT(SerialIn in) {
		int start = in.readPackedInt();
		int end = in.readPackedInt();
		Token token = in.readToken();
		
		ImportStat stat = ImportStat.get(Token.EMPTY, token);
		stat.setLocation(start, end);
		
		return stat;
	}
	
	ClassStat read_CLASS(SerialIn in) {
		int start = in.readPackedInt();
		int end = in.readPackedInt();
		
		Token token = in.readToken();
		Reference ref = in.readReference();
		
		ClassStat stat = ClassStat.get(Token.EMPTY, token);
		stat.setLocation(start, end);
		stat.setReference(ref);
		readList(stat, in);
		
		return stat;
	}
	
	FuncStat read_FUNCTION(SerialIn in) {
		int start = in.readPackedInt();
		int end = in.readPackedInt();
		
		Token return_type = in.readToken();
		Token token = in.readToken();
		Reference ref = in.readReference();
		
		FuncStat stat = FuncStat.get(return_type, token);
		stat.setLocation(start, end);
		stat.setReference(ref);
		
		readList(stat.getArguments(), in);
		readList(stat, in);
		
		return stat;
	}
	
	ScopeStat read_SCOPE(SerialIn in) {
		ScopeStat stat = ScopeStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		readList(stat, in);
		
		return stat;
	}
	
	ForStat read_FOR(SerialIn in) {
		ForStat stat = ForStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		readList(stat, in);
		
		return stat;
	}
	
	WhileStat read_WHILE(SerialIn in) {
		WhileStat stat = WhileStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		readList(stat, in);
		
		return stat;
	}
	
	DoWhileStat read_DO_WHILE(SerialIn in) {
		DoWhileStat stat = DoWhileStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		readList(stat, in);
		
		return stat;
	}
	
	GotoStat read_GOTO(SerialIn in) {
		int start = in.readPackedInt();
		int end = in.readPackedInt();
		
		Token token = in.readToken();
		Reference ref = in.readReference();
		
		GotoStat stat = GotoStat.get(Token.EMPTY, token);
		stat.setLocation(start, end);
		stat.setReference(ref);
		
		return stat;
	}
	
	LabelStat read_LABEL(SerialIn in) {
		int start = in.readPackedInt();
		int end = in.readPackedInt();
		
		Token token = in.readToken();
		Reference ref = in.readReference();
		
		LabelStat stat = LabelStat.get(token);
		stat.setLocation(start, end);
		stat.setReference(ref);
		
		return stat;
	}
	
	ContinueStat read_CONTINUE(SerialIn in) {
		ContinueStat stat = ContinueStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		return stat;
	}
	
	BreakStat read_BREAK(SerialIn in) {
		BreakStat stat = BreakStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		return stat;
	}
	
	ReturnStat read_RETURN(SerialIn in) {
		ReturnStat stat = ReturnStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		readList(stat, in);
		
		return stat;
	}
	
	IfStat read_IF(SerialIn in) {
		IfStat stat = IfStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		readList(stat, in);
		
		return stat;
	}
	
	IfElseStat read_IF_ELSE(SerialIn in) {
		IfElseStat stat = IfElseStat.get(Token.EMPTY);
		stat.setLocation(in.readPackedInt(), in.readPackedInt());
		readList(stat, in);
		
		return stat;
	}
}
