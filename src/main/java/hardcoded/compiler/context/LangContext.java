package hardcoded.compiler.context;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hardcoded.compiler.impl.ISyntaxPosition;
import hardcoded.lexer.Token;
import hardcoded.lexer.Token.Type;
import hardcoded.utils.DebugUtils;
import hardcoded.utils.Position;

public class LangContext {
	private static final Logger LOGGER = LogManager.getLogger(LangContext.class);
	
	private final List<Token> list;
	private final Token start;
	private final Token end;
	private int index;
	
	private LangContext(List<Token> list) {
		this.list = list;
		this.start = new Token(Type.WHITESPACE, ISyntaxPosition.empty());
		
		if(!list.isEmpty()) {
			Token t1 = list.get(list.size() - 1);
			Position end = t1.syntaxPosition.getEndPosition();
			this.end = new Token(Type.EOF, ISyntaxPosition.of(end, end));
		} else {
			this.end = new Token(Type.EOF, ISyntaxPosition.empty());
		}
	}
	
	public int readerIndex() {
		return index;
	}
	
	public void advance() {
		index++;
	}
	
	public void recede() {
		index--;
	}
	
	public int indexOf(Token token) {
		return list.indexOf(token);
	}
	
	public int remaining() {
		return list.size() - index;
	}
	
	public Token token() {
		Token token = peak(0);
		
		if(DebugUtils.DEBUG_LANGCONTEXT_STACK_TRACE) {
			Position pos = token.syntaxPosition.getStartPosition();
			LOGGER.debug("[{}] (Line: {}, Column: {}), {}", Thread.getAllStackTraces().get(Thread.currentThread())[4], pos.line, pos.column, token);
		}
		
		return peak(0);
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
	
	/**
	 * Returns the token at the specified relative position.
	 * <br>Note that this method will never return {@code null}.
	 * 
	 * @param	offset
	 * @return	the token at the specified relative position
	 */
	public Token peak(int offset) {
		int idx = index + offset;
		if(idx < 0) return start;
		if(idx >= list.size()) return end;
		return list.get(idx);
	}
	
	public String peakString(int offset, int count) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < count; i++) {
			sb.append(peak(offset + i).value).append(" ");
		}
		
		return sb.toString().trim();
	}
	
	@Override
	public String toString() {
		return value();
	}
	
	public static LangContext wrap(List<Token> list) {
		return new LangContext(list);
	}
}
