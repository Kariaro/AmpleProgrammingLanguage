package me.hardcoded.compiler.context;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.lexer.Token;
import me.hardcoded.lexer.Token.Type;
import me.hardcoded.utils.DebugUtils;
import me.hardcoded.utils.Position;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class LangReader {
	private static final Logger LOGGER = LogManager.getLogger(LangReader.class);
	
	private final List<Token> list;
	private final Token start;
	private final Token end;
	private int index;
	
	private LangReader(File file, List<Token> list) {
		this.list = list;
		this.start = new Token(Type.WHITESPACE, ISyntaxPos.empty(file));
		
		if (!list.isEmpty()) {
			Token t1 = list.get(list.size() - 1);
			Position end = t1.syntaxPosition.getEndPosition();
			this.end = new Token(Type.EOF, ISyntaxPos.of(file, end, end));
		} else {
			this.end = new Token(Type.EOF, ISyntaxPos.empty(file));
		}
	}
	
	//	public int readerIndex() {
	//		return index;
	//	}
	//
	//	public void readerIndex(int index) {
	//		this.index = index;
	//	}
	
	public void advance() {
		index++;
	}
	
	//	public void recede() {
	//		index--;
	//	}
	//
	//	public int indexOf(Token token) {
	//		return list.indexOf(token);
	//	}
	
	public int remaining() {
		return list.size() - index;
	}
	
	public Token token() {
		Token token = peak(0);
		
		if (DebugUtils.DEBUG_LANGCONTEXT_STACK_TRACE) {
			Position pos = token.syntaxPosition.getStartPosition();
			LOGGER.debug("[{}] (Line: {}, Column: {}), {}", Thread.getAllStackTraces().get(Thread.currentThread())[4], pos.line(), pos.column(), token);
		}
		
		return token;
	}
	
	public String value() {
		return token().value;
	}
	
	public Type type() {
		return token().type;
	}
	
	public Position position() {
		return token().syntaxPosition.getStartPosition();
	}
	
	public Position lastPositionEnd() {
		return peak(-1).syntaxPosition.getEndPosition();
	}
	
	public Position nextPositionEnd() {
		return peak(0).syntaxPosition.getEndPosition();
	}
	
	public ISyntaxPos syntaxPosition() {
		return token().syntaxPosition;
	}
	
	/**
	 * Returns the token at the specified relative position.
	 * <br>Note that this method will never return {@code null}.
	 *
	 * @param offset
	 * @return the token at the specified relative position
	 */
	public Token peak(int offset) {
		int idx = index + offset;
		if (idx < 0)
			return start;
		if (idx >= list.size())
			return end;
		return list.get(idx);
	}
	
	public String peakString(int offset, int count) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < count; i++) {
			sb.append(peak(offset + i).value).append(" ");
		}
		
		return sb.toString().trim();
	}
	
	@Override
	public String toString() {
		return value();
	}
	
	public static LangReader wrap(File file, List<Token> list) {
		return new LangReader(file, list);
	}
}
