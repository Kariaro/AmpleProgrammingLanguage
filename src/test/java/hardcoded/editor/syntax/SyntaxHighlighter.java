package hardcoded.editor.syntax;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.constants.Keywords;
import hardcoded.compiler.constants.Modifiers;
import hardcoded.compiler.constants.Primitives;
import hardcoded.lexer.*;

public class SyntaxHighlighter {
	private final static Tokenizer lexer;
	
	static {
		Tokenizer lex = null;
		try {
			lex = TokenizerFactory.loadFromFile(new File("res/project/lexer.lex"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		lexer = lex;
		lexer.setAutoDiscard(false);
	}
	
	public static class Highlight {
		public Color color;
		public int start;
		public int length;
		
		public Highlight(Color c, int s, int l) {
			color = c;
			start = s;
			length = l;
		}
	}
	
	public List<Highlight> createHighlight(String text) {
		Token token = TokenizerOld.generateTokenChain(lexer, text.getBytes());
		List<Highlight> list = new ArrayList<>();
		
		while(token != null) {
			int offset = token.fileOffset();
			String value = token.toString();
			int length;
			
			if(value == null) {
				length = 0;
			} else {
				length = token.toString().length();
			}
			
			if(token.groupEquals("COMMENT")) {
				list.add(new Highlight(new Color(136, 181, 101), offset, length));
			}
			
			if(token.groupEquals("STRING")
			|| token.groupEquals("CHAR")) {
				list.add(new Highlight(new Color(96, 181, 65), offset, length));
			}
			
			if(token.groupEquals("IDENTIFIER")) {
				list.add(new Highlight(new Color(33, 33, 33), offset, length));
			}
			
			if(token.groupEquals("DOUBLE")
			|| token.groupEquals("FLOAT")
			|| token.groupEquals("LONF")
			|| token.groupEquals("INT")) {
				list.add(new Highlight(new Color(61, 154, 191), offset, length));
			}
			
			if(Keywords.contains(value) || Primitives.contains(value)) {
				list.add(new Highlight(new Color(128, 128, 192), offset, length));
			}
			
			if(Modifiers.contains(value)) {
				list.add(new Highlight(Color.magenta, offset, length));
			}
			
			// DO highlight
			token = token.next();
		}
		
		return list;
	}
}
