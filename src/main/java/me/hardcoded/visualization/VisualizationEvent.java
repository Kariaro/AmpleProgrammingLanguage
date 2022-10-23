package me.hardcoded.visualization;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.utils.Position;

public abstract class VisualizationEvent {
	private final Visualization source;
	private final Type type;
	
	protected VisualizationEvent(Visualization source, Type type) {
		this.source = source;
		this.type = type;
	}
	
	public final Visualization getSource() {
		return source;
	}
	
	public final Type getType() {
		return type;
	}
	
	enum Type {
		SELECTION_EVENT,
		SYNTAX_SELECTION_EVENT
	}
	
	public static class SelectionEvent extends VisualizationEvent {
		private final Position position;
		
		public SelectionEvent(Visualization source, Position position) {
			super(source, Type.SELECTION_EVENT);
			this.position = position;
		}
		
		public Position getPosition() {
			return position;
		}
	}
	
	public static class SyntaxSelectionEvent extends VisualizationEvent {
		private final ISyntaxPos syntaxPosition;
		
		public SyntaxSelectionEvent(Visualization source, ISyntaxPos syntaxPosition) {
			super(source, Type.SYNTAX_SELECTION_EVENT);
			this.syntaxPosition = syntaxPosition;
		}
		
		public ISyntaxPos getSyntaxPosition() {
			return syntaxPosition;
		}
	}
}
