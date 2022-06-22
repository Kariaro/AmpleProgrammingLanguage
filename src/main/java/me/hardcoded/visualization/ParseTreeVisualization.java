package me.hardcoded.visualization;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;

import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Associativity;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.utils.StringUtils;

/**
 * A visualization of a parse tree
 *
 * @author HardCoded
 */
public final class ParseTreeVisualization extends Visualization<ProgStat> {
	private PTPanel panel;
	private boolean showReferenceType;
	private boolean showReferenceId;
	private ProgStat currentProgram;
	
	public ParseTreeVisualization() {
		super("ParseTree - Visualization", 2);
	}

	@Override
	protected void setup() {
		try {
			InputStream stream = ParseTreeVisualization.class.getResourceAsStream("/icons/parse_tree.png");
			if (stream != null) {
				frame.setIconImage(ImageIO.read(stream));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		panel = new PTPanel();
		panel.setOpaque(true);

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
				if (scrollam < -2) scrollam = -2;
				if (scrollam > 20) scrollam = 20;

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
				if (event.getButton() == MouseEvent.BUTTON1) {
					selectedX = event.getX() * scroll - panel.xpos;
					selectedY = event.getY() * scroll - panel.ypos;
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
				updateSelection(mx, my, panel.elements, false, 0);
				panel.repaint();
			}

			private void updateSelection(double mx, double my, List<Element> list, boolean parent, int depth) {
				for (Element e : list) {
					boolean fullBox = mx > (e.x) && mx < (e.x + e.offset);

					if (fullBox) {
						boolean smallBox = mx > (e.x + (e.offset - e.width) / 2.0)
							&& mx < (e.x + (e.offset + e.width) / 2.0)
							&& my > (e.y - 20)
							&& my < (e.y + e.height + 20);

						e.hover = smallBox;
						if (smallBox) {
							updateAll(e.elements, true);
						} else {
							updateSelection(mx, my, e.elements, false, depth + 1);
						}
					} else {
						e.hover = false;
						updateAll(e.elements, false);
					}
				}
			}

			private void updateAll(List<Element> list, boolean hover) {
				for (Element e : list) {
					e.hover = hover;
					updateAll(e.elements, hover);
				}
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
	protected void showObject(ProgStat program) {
		currentProgram = program;
		
		panel.display(program);
		panel.repaint();
		frame.setVisible(true);
	}

	@Override
	public void hide() {
		frame.setVisible(false);
	}

	private class PTPanel extends JPanel {
		private final List<Element> elements = new ArrayList<>();
		private final Font font = new Font("Consolas", Font.PLAIN, 18);

		private double zoom = 1;
		private double xpos = 0;
		private double ypos = 0;

		@Override
		public void paint(Graphics gr) {
			Graphics2D g = (Graphics2D)gr;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setFont(font);

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, panel.getWidth(), panel.getHeight());

			g.scale(zoom, zoom);
			g.translate(xpos, ypos);

			synchronized (elements) {
				for (Element e : elements) e.paintLines(g);
				for (Element e : elements) e.paint(g);
			}
		}

		public void display(ProgStat program) {
			synchronized (elements) {
				elements.clear();

				Element entry = new Element(null, program);
				elements.add(entry);
				entry.move(-(entry.x + (entry.offset - entry.width) / 2), -entry.y);
			}
		}
	}

	private FontRenderContext _frc;
	private double getStringWidth(String string) {
		if (_frc == null) {
			_frc = new FontRenderContext(new AffineTransform(), true, true);
		}
		return panel.font.getStringBounds(string, _frc).getWidth();
	}

	private class Element {
		public double x;
		public double y;
		public int height;
		public int width;
		public double offset;
		public boolean hover;

		private final List<Element> elements;
		private String content;
		private Color body = Color.lightGray;

		private Element(Element parent, Object object) {
			this.elements = new ArrayList<>();

			if (object instanceof Expr) {
				body = Color.white;
			}

			List<Object> children = getElements(object);

			if (object instanceof Stat stat) {
				setContent(stat.getTreeType().name());
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
					sb.append(ref.getName());
					if (showReferenceId) {
						sb.append(' ').append(ref.toSimpleString());
					}
					setContent(sb.toString());
				} else {
					setContent(object.toString());
				}
			}

			if (!children.isEmpty()) {
				offset = 0;

				for (Object obj : children) {
					Element e = new Element(this, obj);
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
			width = content.length() * 12 + 10;
			width = (int)getStringWidth(content) + 10;
			height = 20;
			offset = width + 1;
		}

		public void move(double x, double y) {
			this.x += x;
			this.y += y;
			for (Element e : elements) {
				e.move(x, y);
			}
		}

		public void paint(Graphics2D g) {
			int x = (int)this.x;
			int y = (int)this.y;

			int xp = x;

			if (!elements.isEmpty()) {
				xp += (offset - width) / 2;
			}

			if ((xp + width + panel.xpos >= 0)
			&& (xp - width + panel.xpos <= panel.getWidth() / panel.zoom)
			&& (y - 5 + (height + 10) + panel.ypos >= 0)
			&& (y - 5 - (height + 10) + panel.ypos <= panel.getHeight() / panel.zoom)) {
				g.setColor(hover ? body.darker():body);
				g.fillRoundRect(xp, y - 5, width, height + 10, 10, 10);

				g.setColor(Color.black);
				Stroke stroke = g.getStroke();
				g.setStroke(new BasicStroke(2));
				g.drawRoundRect(xp, y - 5, width, height + 10, 10, 10);
				g.setStroke(stroke);

				g.setColor(Color.black);
				FontMetrics fm = g.getFontMetrics();
				Rectangle rect = fm.getStringBounds(content, g).getBounds();
				g.drawString(content, x - rect.x + ((int)offset - rect.width) / 2, y - rect.y + (height - rect.height) / 2 + 3);
			}

//			g.setColor(hover ? Color.red : Color.gray);
//			g.fillRect(xp - (int)((offset - width) / 2.0), y - 10, (int)offset, 10);

			double xx = xp + (width - offset) / 2.0 + panel.xpos;
			if ((xx + offset > 0) && (xx < panel.getWidth() / panel.zoom)) {
				for (Element elm : elements) {
					elm.paint(g);
				}
			}
		}

		public void paintLines(Graphics2D g) {
			int x0 = (int)(x + offset / 2D);
			int y0 = (int)y;

			int xxp = (int)this.x;

			if (!elements.isEmpty()) {
				xxp += (offset - width) / 2;
			}

			double xx = xxp + (width - offset) / 2.0 + panel.xpos;
			if ((xx + offset > 0) && (xx < panel.getWidth() / panel.zoom)) {
				g.setColor(Color.black);
				for (Element elm : elements) {
					int x1 = (int)(elm.x + elm.offset / 2D);
					int y1 = (int)elm.y;

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
					ProgStat s = (ProgStat)stat;
					yield List.of(s.getElements());
				}
				case FUNC -> {
					FuncStat s = (FuncStat)stat;
					yield List.of(s.getReference(), s.getParameters(), s.getBody());
				}
				case BREAK -> List.of();
				case CONTINUE -> List.of();
				case EMPTY -> List.of();
				case FOR -> {
					ForStat s = (ForStat) stat;
					yield List.of(s.getInitializer(), s.getCondition(), s.getAction(), s.getBody());
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
				case STACK_DATA -> {
					StackDataExpr s = (StackDataExpr) stat;
					yield List.of(s.getSize(), s.getValue());
				}
//				case WHILE -> {
//					WhileStat s = (WhileStat)stat;
//					yield List.of(s.getCondition(), s.getBody());
//				}
//				case NAMESPACE -> {
//					NamespaceStat s = (NamespaceStat)stat;
//					yield List.of(s.getReference(), s.getElements());
//				}

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
//				case CAST -> {
//					CastExpr e = (CastExpr)stat;
//					yield List.of(e.getCastType(), e.getValue());
//				}
//				case COMMA -> List.<Object>copyOf(((CommaExpr)stat).getValues());
				case NAME -> List.of(((NameExpr) stat).getReference());
//				case NULL -> List.of();
				case STRING -> List.of('"' + StringUtils.escapeString(stat.toString()) + '"');
				case NUM -> List.of(stat.toString());
				default -> List.of("<%s Not Implement>".formatted(stat.getTreeType()));
			};
		}

		if (obj instanceof List list) {
			return List.<Object>copyOf(list);
		}

		return List.of();
	}
}
