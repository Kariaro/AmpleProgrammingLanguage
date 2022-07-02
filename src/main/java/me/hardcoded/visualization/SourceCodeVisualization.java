package me.hardcoded.visualization;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.lexer.Token;
import me.hardcoded.utils.Position;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
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
			private double yposStart;
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				panel.setScroll(panel.getScroll() + event.getPreciseWheelRotation());
				panel.repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
				yposStart = panel.getScroll();
				
				if (event.getButton() == MouseEvent.BUTTON1) {
					dragStart = event.getPoint();
					
					Position mousePos = getMouse(event.getPoint());
					handler.fireEvent(new VisualizationEvent.SelectionEvent(
						SourceCodeVisualization.this,
						mousePos
					));
				} else {
					dragStart = null;
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent event) {
				Point offset = event.getPoint();
				offset.translate(-dragStart.x, -dragStart.y);
				
				panel.setScroll(yposStart - (int) (offset.y / panel.bounds.getHeight()));
				panel.repaint();
			}
			
			public Position getMouse(Point point) {
				int x = (int) ((point.x - 5) / panel.bounds.getWidth());
				int y = (int) (((point.y - 5) / panel.bounds.getHeight()) + panel.getScroll());
				
				return new Position(x - 5, y, 0);
			}
			
			@Override
			public void mouseMoved(MouseEvent event) {
				Position mousePos = getMouse(event.getPoint());
				panel.setHoveredToken(getToken(mousePos));
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
				ISyntaxPosition syntaxPosition = token.syntaxPosition;
				Position s = syntaxPosition.getStartPosition();
				Position e = syntaxPosition.getEndPosition();
				
				if ((pos.line >= s.line && pos.line <= e.line)
				&& (pos.line != s.line || pos.column >= s.column)
				&& (pos.line != e.line || pos.column < e.column)) {
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
		panel.repaint();
	}
	
	private class LocalPanel extends JPanel {
		private double ypos = 0;
		private Font font;
		private Rectangle2D bounds;
		
		private Token hover;
		
		public double getScroll() {
			return ypos;
		}
		
		public void setScroll(double scroll) {
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
		
		public void setHoveredToken(Token token) {
			this.hover = token;
		}
		
		public void setFontSize(int size) {
			FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
			Font font = new Font("Consolas", Font.PLAIN, size);
			
			Rectangle2D bounds = font.getStringBounds(" ", frc);
			bounds.setFrame(
				bounds.getX(),
				bounds.getY(),
				Math.ceil(bounds.getWidth()),
				Math.ceil(bounds.getHeight())
			);
			
			this.font = font;
			this.bounds = bounds;
		}
		
		@Override
		public void paint(Graphics gr) {
			// TODO: Calculate from line index to token index
			if (bounds == null) {
				setFontSize(12);
			}
			
			Graphics2D g = (Graphics2D) gr;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setFont(font);
			
			g.setColor(new Color(0x2b2b2b));
			g.fillRect(0, 0, getWidth(), getHeight());
			
			g.translate(5, 5 - (int) (ypos * bounds.getHeight()));
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
				case FUNC, RETURN, FOR, IF -> new Color(0xCC7832);
				case WHITESPACE -> Color.LIGHT_GRAY;
				case STRING, CHARACTER -> new Color(0x6a8759);
				case INT, LONG -> new Color(0x6897bb);
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
						pointer.line ++;
						drawLine(g, pointer.line);
						g.setColor(color);
						continue;
					}
				}
				
				int cx = pointer.column + 5;
				int cy = pointer.line;
				
				double x = cx * bounds.getWidth();
				double y = cy * bounds.getHeight() - bounds.getY();
				
				if (hover) {
					g.setColor(new Color(0x354945));
					g.fillRect(
						(int) (cx * bounds.getWidth()),
						(int) (cy * bounds.getHeight()),
						(int) bounds.getWidth(),
						(int) bounds.getHeight()
					);
				}
				
				g.setColor(color);
				g.drawString(Character.toString(c), (float) x, (float) y);
				
				pointer.column++;
			}
		}
		
		public void drawLine(Graphics2D g, int line) {
			g.setColor(Color.LIGHT_GRAY);
			
			double x = 0;
			double y = line * bounds.getHeight() - bounds.getY();
			
			g.drawString("%-4d|".formatted(line), (float) x, (float) y);
		}

		public void display(List<Token> tokens) {
		
		}
	}
	
	private static class Pointer {
		public int line = -1;
		public int column = -1;
	}
}
