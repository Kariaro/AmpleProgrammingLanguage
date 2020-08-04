package hardcoded.visualization;

/**
 * Abstract syntax tree
 * https://en.wikipedia.org/wiki/Abstract_syntax_tree
 * 
 * @author HardCoded
 */
public final class ASTVisualization extends Visualization {
	protected ASTVisualization(String name) {
		super("Abstract syntax tree - visualizer");
	}

	@Override
	public void show(Object... args) {
		throw new UnsupportedOperationException("Implement me");
	}

	@Override
	public void hide() {
		
	}	
}
