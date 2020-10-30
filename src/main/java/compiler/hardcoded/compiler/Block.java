package hardcoded.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.impl.IBlock;
import hardcoded.compiler.statement.Statement;
import hardcoded.lexer.Token;
import hardcoded.visualization.Printable;

@Deprecated
abstract class Block implements IBlock, Printable {
	// TODO: Remove
	public abstract boolean hasElements();
	public abstract List<Statement> getElements();
	
	public File getDeclaringFile() {
		return null;
	}
	
	public int getLineIndex() {
		return 0;
	}
	
	public static class NestedBlock extends Block {
		public List<Statement> list = new ArrayList<>();
		
		@Override
		public boolean hasElements() {
			return true;
		}
		
		public List<Statement> getElements() {
			return list;
		}
		
		public String asString() { return "null"; }
		public Object[] asList() { return list.toArray(); }
	}
	
	public static class ClassBlock extends NestedBlock {
		// TODO: Variables, Constructor, Methods, Operator overrides
		
		public Token[] getTokens() {
			return null;
		}
	}
}