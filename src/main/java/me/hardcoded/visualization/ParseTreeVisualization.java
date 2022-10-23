package me.hardcoded.visualization;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Associativity;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.utils.Position;
import me.hardcoded.utils.SyntaxUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A visualization of a parse tree
 *
 * @author HardCoded
 */
public final class ParseTreeVisualization extends Visualization implements VisualizationListener {
	private LocalPanel panel;
	private boolean showReferenceType;
	private boolean showReferenceId;
	private ProgStat currentProgram;
	
	public ParseTreeVisualization(VisualizationHandler handler) {
		super("ParseTree - Visualization", handler, 2);
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
		panel.setBackground(colorCache.getColor(0x2b2b2b));
		
		frame.setSize(640, 460);
		frame.setContentPane(panel);
		MouseAdapter adapter = new MouseAdapter() {
			private double selectedX;
			private double selectedY;
			
			private double scrollam = 1;
			private double scroll = 1;
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				double amount = event.getPreciseWheelRotation() / 5.0;
				
				scrollam += amount;
				if (scrollam < -2)
					scrollam = -2;
				if (scrollam > 10)
					scrollam = 10;
				
				double last = scroll;
				scroll = Math.pow(1.3, scrollam - 1);
				
				Point mouse = event.getPoint();
				panel.xpos += mouse.x * (scroll - last);
				panel.ypos += mouse.y * (scroll - last);
				
				panel.zoom = 1.0 / scroll;
				panel.repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
				selectedX = event.getX() * scroll - panel.xpos;
				selectedY = event.getY() * scroll - panel.ypos;
				
				if (event.getButton() == MouseEvent.BUTTON1) {
					unselectAll();
					
					Element selection = getElementAt(selectedX, selectedY);
					if (selection != null && selection.syntaxPosition != null) {
						handler.fireEvent(new VisualizationEvent.SyntaxSelectionEvent(
							ParseTreeVisualization.this,
							selection.syntaxPosition
						));
					}
					
					panel.repaint();
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent event) {
				panel.xpos = event.getX() * scroll - selectedX;
				panel.ypos = event.getY() * scroll - selectedY;
				panel.repaint();
			}
			
			@Override
			public void mouseMoved(MouseEvent event) {
				double mx = event.getX() * scroll - panel.xpos;
				double my = event.getY() * scroll - panel.ypos;
				updateSelection(mx, my);
				panel.repaint();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				updateAll(panel.elements, false);
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
		
		JMenu viewType = new JMenu("Reference");
		{
			ButtonGroup buttonGroup = new ButtonGroup();
			JRadioButtonMenuItem simpleView = new JRadioButtonMenuItem("Simple");
			simpleView.addActionListener(e -> {
				showReferenceType = false;
				showReferenceId = false;
				if (currentProgram != null) {
					showObject(currentProgram);
				}
			});
			simpleView.setSelected(true);
			buttonGroup.add(simpleView);
			viewType.add(simpleView);
			
			JRadioButtonMenuItem advancedView = new JRadioButtonMenuItem("Advanced");
			advancedView.addActionListener(e -> {
				showReferenceType = true;
				showReferenceId = true;
				if (currentProgram != null) {
					showObject(currentProgram);
				}
			});
			buttonGroup.add(advancedView);
			viewType.add(advancedView);
		}
		
		options.add(viewType);
	}
	
	@Override
	protected void showObject(Object value) {
		if (!(value instanceof ProgStat program)) {
			throw new IllegalArgumentException();
		}
		
		currentProgram = program;
		panel.display(program);
		panel.repaint();
		frame.setVisible(true);
	}
	
	private Element forEachElement(List<Element> start, Function<Element, Element> callback) {
		LinkedList<Element> elements = new LinkedList<>(start);
		Element result;
		
		while (!elements.isEmpty()) {
			Element first = elements.pollFirst();
			elements.addAll(0, first.elements);
			if ((result = callback.apply(first)) != null) {
				return result;
			}
		}
		
		return null;
	}
	
	private void unselectAll() {
		forEachElement(panel.elements, e -> {
			e.hover = false;
			e.selected = false;
			return null;
		});
	}
	
	private void updateAll(List<Element> list, boolean hover) {
		forEachElement(list, e -> {
			e.hover = hover;
			return null;
		});
	}
	
	/**
	 * Returns the element at the specified coordinates
	 *
	 * @param mx the x coordinate
	 * @param my the y coordinate
	 * @return the element at the specified coordinates
	 */
	private Element getElementAt(double mx, double my) {
		return forEachElement(panel.elements, e -> {
			boolean fullBox = mx > (e.x) && mx < (e.x + e.offset);
			
			if (fullBox) {
				boolean smallBox = mx > (e.x + (e.offset - e.width) / 2.0)
					&& mx < (e.x + (e.offset + e.width) / 2.0)
					&& my > (e.y - 20)
					&& my < (e.y + e.height + 20);
				
				if (smallBox) {
					return e;
				}
			}
			
			return null;
		});
	}
	
	private void updateSelection(double mx, double my) {
		Element selected = getElementAt(mx, my);
		
		updateAll(panel.elements, false);
		if (selected != null) {
			updateAll(selected.elements, true);
			selected.hover = true;
		}
	}
	
	private void setElementSelected(List<Element> list) {
		for (Element e : list) {
			e.selected = true;
			setElementSelected(e.elements);
		}
	}
	
	private void setElementSelected(List<Element> list, ISyntaxPos position) {
		for (Element e : list) {
			ISyntaxPos syntaxPosition = e.syntaxPosition;
			if (syntaxPosition != null && SyntaxUtils.syntaxIntersect(position, syntaxPosition)) {
				e.selected = true;
				for (Element children : e.elements) {
					children.selected = true;
				}
			}
			
			setElementSelected(e.elements, position);
		}
	}
	
	private Element getSelect(List<Element> elements, Position pos) {
		for (Element element : elements) {
			Element value = getSelect(element.elements, pos);
			if (value != null) {
				return value;
			}
			
			ISyntaxPos syntaxPosition = element.syntaxPosition;
			if (syntaxPosition != null && SyntaxUtils.syntaxIntersect(syntaxPosition, pos)) {
				return element;
			}
		}
		
		return null;
	}
	
	@Override
	public void handleSelection(VisualizationEvent.SelectionEvent event) {
		Element element = getSelect(panel.elements, event.getPosition());
		
		unselectAll();
		if (element != null) {
			setElementSelected(element.elements);
			element.selected = true;
		}
		panel.repaint();
	}
	
	@Override
	public void handleSyntaxSelection(VisualizationEvent.SyntaxSelectionEvent event) {
		unselectAll();
		setElementSelected(panel.elements, event.getSyntaxPosition());
		panel.repaint();
	}
	
	private class LocalPanel extends JPanel {
		private final List<Element> elements = new ArrayList<>();
		private final Font font = new Font("Consolas", Font.PLAIN, 18);
		
		private double zoom = 1;
		private double xpos = 0;
		private double ypos = 0;
		
		@Override
		public void paint(Graphics gr) {
			super.paint(gr);
			
			Graphics2D g = (Graphics2D) gr;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g.setFont(font);
			
			g.scale(zoom, zoom);
			g.translate(xpos, ypos);
			
			synchronized (elements) {
				for (Element e : elements)
					e.paintLines(g);
				for (Element e : elements)
					e.paint(g);
			}
		}
		
		public void display(ProgStat program) {
			synchronized (elements) {
				elements.clear();
				
				Element entry = new Element(program);
				elements.add(entry);
				entry.move(-(entry.x + (entry.offset - entry.width) / 2), -entry.y);
			}
		}
	}
	
	private class Element {
		private final List<Element> elements;
		private final Color body;
		
		public double x;
		public double y;
		public int height;
		public int width;
		public double offset;
		public boolean hover;
		public boolean selected;
		
		private String content;
		private ISyntaxPos syntaxPosition;
		
		private Element(Object object) {
			this.elements = new ArrayList<>();
			
			if (object instanceof Expr) {
				body = colorCache.getColor(0x3d3d3d); // Color.darkGray;
			} else if (object instanceof Stat) {
				body = colorCache.getColor(0x1d1d1d); // Color.gray;
			} else {
				body = colorCache.getColor(0x5d5d5d); // Color.darkGray;
			}
			
			List<Object> children = getElements(object);
			
			if (object instanceof Stat stat) {
				setContent(stat.getTreeType().name());
				syntaxPosition = stat.getSyntaxPosition();
			} else if (object instanceof List list) {
				int size = list.size();
				setContent("<list " + size + (size == 1 ? " element>" : " elements>"));
				
				if (list.size() == 0) {
					return;
				}
			} else {
				if (object instanceof Reference ref) {
					StringBuilder sb = new StringBuilder();
					if (showReferenceType) {
						sb.append(ref.getValueType()).append(' ');
					}
					sb.append(ref.getPath());
					if (showReferenceId) {
						sb.append(' ').append(ref.toSimpleString());
					}
					setContent(sb.toString());
				} else {
					setContent(object.toString());
				}
			}
			
			//			if (parent != null && syntaxPosition == null) {
			//				syntaxPosition = parent.syntaxPosition;
			//			}
			
			if (!children.isEmpty()) {
				offset = 0;
				
				for (Object obj : children) {
					Element e = new Element(obj);
					e.move(offset, 100);
					offset += e.offset;
					elements.add(e);
				}
				
				if (width > offset) {
					double diff = (width - offset) / 2.0;
					for (Element e2 : elements) {
						e2.move(diff, 0);
					}
					
					offset = width;
				}
			}
		}
		
		public void setContent(String value) {
			content = value;
			width = (int) getStringBounds(panel.font, content).getWidth() + 12;
			height = 20;
			offset = width;
		}
		
		public void move(double x, double y) {
			this.x += x;
			this.y += y;
			for (Element e : elements) {
				e.move(x, y);
			}
		}
		
		public void paint(Graphics2D g) {
			int x = (int) this.x;
			int y = (int) this.y;
			
			int xp = x;
			
			if (!elements.isEmpty()) {
				xp += (offset - width) / 2;
			}
			
			if ((xp + width + panel.xpos >= 0)
				&& (xp - width + panel.xpos <= panel.getWidth() / panel.zoom)
				&& (y - 5 + (height + 10) + panel.ypos >= 0)
				&& (y - 5 - (height + 10) + panel.ypos <= panel.getHeight() / panel.zoom)) {
				g.setColor(selected ? body.brighter().brighter() : (hover ? body.brighter() : body));
				g.fillRect(xp, y - 5, width, height + 10);
				
				if (selected) {
					g.setColor(Color.white);
					Stroke stroke = g.getStroke();
					g.setStroke(new BasicStroke(2));
					g.drawRect(xp + 1, y - 5 + 1, width - 2, height + 10 - 2);
					g.setStroke(stroke);
				}
				
				g.setColor(Color.white);
				FontMetrics fm = g.getFontMetrics();
				Rectangle rect = fm.getStringBounds(content, g).getBounds();
				g.drawString(content, x - rect.x + ((int) offset - rect.width) / 2, y - rect.y + (height - rect.height) / 2 + 3);
			}
			
			double xx = xp + (width - offset) / 2.0 + panel.xpos;
			if ((xx + offset > 0) && (xx < panel.getWidth() / panel.zoom)) {
				for (Element elm : elements) {
					elm.paint(g);
				}
			}
		}
		
		public void paintLines(Graphics2D g) {
			int x0 = (int) (x + offset / 2D);
			int y0 = (int) y;
			int xxp = (int) this.x;
			
			if (!elements.isEmpty()) {
				xxp += (offset - width) / 2;
			}
			
			g.setColor(Color.white);
			double xx = xxp + (width - offset) / 2.0 + panel.xpos;
			if ((xx + offset > 0) && (xx < panel.getWidth() / panel.zoom)) {
				for (Element elm : elements) {
					int x1 = (int) (elm.x + elm.offset / 2D);
					int y1 = (int) elm.y;
					
					// g.drawLine(x0, y0, x1, y0);
					// g.drawLine(x1, y0, x1, y1);
					g.drawLine(x0, y0, x1, y1);
					elm.paintLines(g);
				}
			}
		}
	}
	
	private static List<Object> getElements(Object obj) {
		if (obj instanceof Stat stat) {
			return switch (stat.getTreeType()) {
				// Statements
				case PROGRAM -> {
					ProgStat s = (ProgStat) stat;
					yield List.of(s.getElements());
				}
				case FUNC -> {
					FuncStat s = (FuncStat) stat;
					yield List.of(s.getReference(), s.getParameters(), s.getBody());
				}
				case BREAK, CONTINUE, EMPTY -> List.of();
				case FOR -> {
					ForStat s = (ForStat) stat;
					yield List.of(s.getInitializer(), s.getCondition(), s.getAction(), s.getBody());
				}
				case WHILE -> {
					WhileStat s = (WhileStat) stat;
					yield List.of(s.getCondition(), s.getBody());
				}
				case IF -> {
					IfStat s = (IfStat) stat;
					yield List.of(s.getValue(), s.getBody(), s.getElseBody());
				}
				case RETURN -> {
					ReturnStat s = (ReturnStat) stat;
					yield List.of(s.getValue());
				}
				case SCOPE -> {
					ScopeStat s = (ScopeStat) stat;
					yield List.of(s.getElements());
				}
				case VAR -> {
					VarStat s = (VarStat) stat;
					yield List.of(s.getReference(), s.getValue());
				}
				case STACK_ALLOC -> {
					StackAllocExpr s = (StackAllocExpr) stat;
					yield List.of(s.getSize(), s.getValue());
				}
				case NAMESPACE -> {
					NamespaceStat s = (NamespaceStat) stat;
					yield List.of(s.getReference(), s.getElements());
				}
				
				// Expression
				case BINARY -> {
					BinaryExpr e = (BinaryExpr) stat;
					yield List.of(e.getLeft(), e.getOperation().getName(), e.getRight());
				}
				case UNARY -> {
					UnaryExpr e = (UnaryExpr) stat;
					if (e.getOperation().getAssociativity() == Associativity.Left) {
						yield List.of(e.getOperation().getName(), e.getValue());
					}
					yield List.of(e.getValue(), e.getOperation().getName());
				}
				case CALL -> {
					CallExpr e = (CallExpr) stat;
					yield List.of(e.getReference(), e.getParameters());
				}
				case CAST -> {
					CastExpr e = (CastExpr) stat;
					yield List.of(e.getType(), e.getValue());
				}
				case NAME -> List.of(((NameExpr) stat).getReference());
				case NUM, STR -> List.of(stat.toString());
				default -> List.of("<%s Not Implement>".formatted(stat.getTreeType()));
			};
		}
		
		if (obj instanceof List list) {
			return List.<Object>copyOf(list);
		}
		
		return List.of();
	}
}
