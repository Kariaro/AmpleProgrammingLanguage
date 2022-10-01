package me.hardcoded.visualization;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.lexer.Token;
import me.hardcoded.utils.Position;
import me.hardcoded.utils.SyntaxUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A visualization of the source code
 *
 * @author HardCoded
 */
public final class SourceCodeVisualization extends Visualization implements VisualizationListener {
	private LocalPanel panel;
	private List<Token> sourceCode;
	
	public SourceCodeVisualization(VisualizationHandler handler) {
		super("SourceCode - Visualization", handler, 2);
	}
	
	@Override
	protected Image getIcon() {
		try (InputStream stream = ParseTreeVisualization.class.getResourceAsStream("/icons/parse_tree.png")) {
			if (stream != null) {
				return ImageIO.read(stream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	protected void setup() {
		panel = new LocalPanel();
		panel.setOpaque(true);
		
		frame.setSize(640, 460);
		frame.setContentPane(panel);
		MouseAdapter adapter = new MouseAdapter() {
			private Point dragStart;
			private int yposStart;
			private int xposStart;
			private Position startPosition;
			private boolean mouseSelection;
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				if ((event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
					int newSize = panel.fontSize - (int) event.getPreciseWheelRotation();
					if (newSize < 4)
						newSize = 4;
					if (newSize > 32)
						newSize = 32;
					panel.setFontSize(newSize);
				} else {
					panel.setScroll(panel.getScroll() + (int) event.getPreciseWheelRotation());
				}
				
				panel.repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
				yposStart = panel.ypos;
				xposStart = panel.xpos;
				dragStart = null;
				mouseSelection = false;
				if (event.getButton() == MouseEvent.BUTTON1) {
					startPosition = null;
				}
				
				if ((event.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
					// Selection
					if (event.getButton() == MouseEvent.BUTTON1) {
						mouseSelection = true;
						startPosition = getMouse(event.getPoint());
					}
				} else {
					dragStart = event.getPoint();
					
					if (event.getButton() == MouseEvent.BUTTON1) {
						Position mousePos = getMouse(event.getPoint());
						handler.fireEvent(new VisualizationEvent.SelectionEvent(
							SourceCodeVisualization.this,
							mousePos
						));
					}
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent event) {
				if (dragStart != null) {
					Point offset = event.getPoint();
					offset.translate(-dragStart.x, -dragStart.y);
					panel.setScroll(yposStart - (int) (offset.y / panel.bounds.getHeight()));
					panel.setScrollX(xposStart - (int) (offset.x / panel.bounds.getWidth()));
				}
				
				if (startPosition != null && mouseSelection) {
					Position mousePos = getMouse(event.getPoint());
					ISyntaxPosition syntaxPosition;
					
					if (mousePos.line < startPosition.line
						|| (mousePos.line == startPosition.line && mousePos.column < startPosition.column)) {
						Position test = new Position(startPosition.column + 1, startPosition.line);
						syntaxPosition = ISyntaxPosition.of(mousePos, test);
					} else {
						Position test = new Position(mousePos.column + 1, mousePos.line);
						syntaxPosition = ISyntaxPosition.of(startPosition, test);
					}
					
					handler.fireEvent(new VisualizationEvent.SyntaxSelectionEvent(
						SourceCodeVisualization.this,
						syntaxPosition
					));
				}
				
				panel.repaint();
			}
			
			public Position getMouse(Point point) {
				int x = (int) ((point.x - 5) / panel.bounds.getWidth()) + panel.getScrollX();
				int y = (int) ((point.y - 5) / panel.bounds.getHeight()) + panel.getScroll();
				
				if (x < panel.columnWidth) {
					x = panel.columnWidth;
				}
				
				return new Position(x - panel.columnWidth, y);
			}
			
			@Override
			public void mouseMoved(MouseEvent event) {
				Position mousePos = getMouse(event.getPoint());
				panel.setHoveredToken(getToken(mousePos));
				panel.repaint();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				panel.setHoveredToken(null);
				panel.repaint();
			}
		};
		
		panel.addMouseListener(adapter);
		panel.addMouseMotionListener(adapter);
		panel.addMouseWheelListener(adapter);
	}
	
	@Override
	protected void setupMenu(JMenuBar menuBar) {
		JMenu options = new JMenu("Options");
		menuBar.add(options);
	}
	
	@Override
	protected void showObject(Object value) {
		if (!(value instanceof List tokens)) {
			throw new IllegalArgumentException();
		}
		
		sourceCode = tokens;
		panel.display(tokens);
		panel.repaint();
		frame.setVisible(true);
	}
	
	private Token getToken(Position pos) {
		List<Token> tokens = sourceCode;
		if (tokens != null) {
			// TODO: This could use binary search
			for (Token token : tokens) {
				if (SyntaxUtils.syntaxIntersect(token.syntaxPosition, pos)) {
					return token;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void handleSelection(VisualizationEvent.SelectionEvent event) {
		Token token = getToken(event.getPosition());
		panel.setHoveredToken(token);
		panel.setSyntaxSelection(null);
		panel.repaint();
	}
	
	@Override
	public void handleSyntaxSelection(VisualizationEvent.SyntaxSelectionEvent event) {
		panel.setSyntaxSelection(event.getSyntaxPosition());
		panel.setHoveredToken(null);
		panel.repaint();
	}
	
	private class LocalPanel extends JPanel {
		private int columnWidth = 5;
		private int xpos = 0;
		private int ypos = 0;
		private int fontSize = 0;
		private Font font;
		private Rectangle2D bounds;
		
		private ISyntaxPosition syntaxSelection;
		private Token hover;
		
		public int getScroll() {
			return ypos;
		}
		
		public int getScrollX() {
			return xpos;
		}
		
		public void setScroll(int scroll) {
			if (scroll < 0) {
				scroll = 0;
			}
			
			List<Token> tokens = sourceCode;
			if (tokens != null && !tokens.isEmpty()) {
				Position end = tokens.get(tokens.size() - 1).syntaxPosition.getEndPosition();
				if (scroll > end.line) {
					scroll = end.line;
				}
			}
			
			this.ypos = scroll;
		}
		
		public void setScrollX(int scroll) {
			if (scroll < 0) {
				scroll = 0;
			}
			
			// TODO: Find max x scroll
			
			this.xpos = scroll;
		}
		
		public void setHoveredToken(Token token) {
			this.hover = token;
		}
		
		public void setSyntaxSelection(ISyntaxPosition syntaxPosition) {
			this.syntaxSelection = syntaxPosition;
		}
		
		public void setFontSize(int size) {
			Font font = new Font("Consolas", Font.PLAIN, size);
			
			Rectangle2D bounds = getStringBounds(font, " ");
			bounds.setFrame(
				bounds.getX(),
				bounds.getY(),
				Math.ceil(bounds.getWidth()),
				Math.ceil(bounds.getHeight())
			);
			
			this.font = font;
			this.fontSize = size;
			this.bounds = bounds;
		}
		
		@Override
		public void paint(Graphics gr) {
			// TODO: Calculate from line index to token index
			if (bounds == null) {
				setFontSize(16);
			}
			
			Graphics2D g = (Graphics2D) gr;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g.setFont(font);
			
			g.setColor(colorCache.getColor(0x2b2b2b));
			g.fillRect(0, 0, getWidth(), getHeight());
			
			g.translate(
				5 - (int) (xpos * bounds.getWidth()),
				5 - (int) (ypos * bounds.getHeight())
			);
			
			{
				ISyntaxPosition pos = syntaxSelection;
				if (pos != null) {
					Position s = pos.getStartPosition();
					Position e = pos.getEndPosition();
					
					int intWidth = (int) (getWidth() / bounds.getWidth()) + 1;
					int of = xpos;
					
					int sp = columnWidth + s.column;
					int se = 0;
					if (s.column - xpos < 0) {
						se = s.column - xpos;
						sp -= se;
					}
					
					g.setColor(colorCache.getColor(0x214283));
					if (s.line != e.line) {
						drawBox(g, s.line, sp, intWidth, 1);
						drawBox(g, e.line, columnWidth + of, e.column - of, 1);
						drawBox(g, s.line + 1, columnWidth + of, intWidth, e.line - s.line - 1);
					} else {
						drawBox(g, s.line, sp, e.column - s.column + se, 1);
					}
				}
			}
			
			List<Token> tokens = sourceCode;
			if (tokens != null) {
				Pointer pointer = new Pointer();
				for (Token token : sourceCode) {
					paintToken(g, pointer, token);
				}
			}
		}
		
		public void paintToken(Graphics2D g, Pointer pointer, Token token) {
			ISyntaxPosition pos = token.syntaxPosition;
			Position startPos = pos.getStartPosition();
			
			boolean showLine = pointer.line != startPos.line;
			boolean hover = this.hover == token;
			
			pointer.column = startPos.column;
			pointer.line = startPos.line;
			
			String value = token.value;
			if (showLine) {
				drawLine(g, pointer.line);
			}
			
			Color color = switch (token.type) {
				case FUNC, WHILE, RETURN, FOR, IF, ELSE, CONTINUE, BREAK -> colorCache.getColor(0xCC7832);
				case WHITESPACE -> Color.LIGHT_GRAY;
				case STRING, CHARACTER -> colorCache.getColor(0x6a8759);
				case INT, LONG -> colorCache.getColor(0x6897bb);
				default -> Color.WHITE;
			};
			
			g.setColor(color);
			for (int i = 0, len = value.length(); i < len; i++) {
				char c = value.charAt(i);
				
				switch (c) {
					case '\t' -> {
						pointer.column += 4;
						continue;
					}
					case '\r' -> {
						continue;
					}
					case '\n' -> {
						pointer.column = 0;
						pointer.line++;
						drawLine(g, pointer.line);
						g.setColor(color);
						continue;
					}
				}
				
				int cx = pointer.column + columnWidth;
				int cy = pointer.line;
				pointer.column++;
				
				if (pointer.column <= xpos) {
					continue;
				}
				
				double x = cx * bounds.getWidth();
				double y = cy * bounds.getHeight() - bounds.getY();
				
				if (hover) {
					g.setColor(colorCache.getColor(0x354945));
					drawBox(g, cy, cx, 1, 1);
				}
				
				g.setColor(color);
				g.drawString(Character.toString(c), (float) x, (float) y);
			}
		}
		
		public void drawLine(Graphics2D g, int line) {
			g.setColor(Color.LIGHT_GRAY);
			
			double x = xpos * bounds.getWidth();
			double y = line * bounds.getHeight() - bounds.getY();
			
			g.drawString(("%-" + (columnWidth - 1) + "d|").formatted(line), (float) x, (float) y);
		}
		
		public void drawBox(Graphics2D g, int line, int column, int width, int height) {
			g.fillRect(
				(int) (column * bounds.getWidth()),
				(int) (line * bounds.getHeight()),
				(int) (width * bounds.getWidth()),
				(int) (height * bounds.getHeight())
			);
		}
		
		public void display(List<Token> tokens) {
		
		}
	}
	
	private static class Pointer {
		public int line = -1;
		public int column = -1;
	}
}
