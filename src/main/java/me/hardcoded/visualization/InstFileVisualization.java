package me.hardcoded.visualization;

import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.compiler.intermediate.inst.Procedure;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Reference;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A visualization of an instruction tree
 *
 * @author HardCoded
 */
public final class InstFileVisualization extends Visualization {
	private LocalPanel panel;
	private boolean showReferenceType;
	private boolean showReferenceId;
	private InstFile currentProgram;
	
	public InstFileVisualization(VisualizationHandler handler) {
		super("InstFile - Visualization", handler, 2);
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
				updateSelection(mx, my, panel.elements, 0);
				panel.repaint();
			}

			private void updateSelection(double mx, double my, List<Element> list, int depth) {
				for (Element e : list) {
					boolean fullBox = mx > (e.x) && mx < (e.x + e.x_offset);

					if (fullBox) {
						boolean smallBox = mx > (e.x + (e.x_offset - e.width) / 2.0)
							&& mx < (e.x + (e.x_offset + e.width) / 2.0)
							&& my > (e.y - 20)
							&& my < (e.y + e.height + 20);

						e.hover = smallBox;
						if (smallBox) {
							updateAll(e.elements, true);
						} else {
							updateSelection(mx, my, e.elements, depth + 1);
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
	protected void showObject(Object value) {
		if (!(value instanceof InstFile program)) {
			throw new IllegalArgumentException();
		}
		
		currentProgram = program;
		panel.display(program);
		panel.repaint();
		frame.setVisible(true);
	}

	private class LocalPanel extends JPanel {
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

		public void display(InstFile program) {
			synchronized (elements) {
				elements.clear();

				Element entry = new Element(program);
				elements.add(entry);
				entry.move(-(entry.x + (entry.x_offset - entry.width) / 2), -entry.y);
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
		public double x_offset;
		public double y_offset;
		public boolean hover;

		private final List<Element> elements;
		private String content;
		private Color body = Color.lightGray;

		private Element(Object object) {
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
				y_offset = 0;
				
				double y_off = 0;

				for (Object obj : children) {
					Element e = new Element(obj);
					e.move(width + 10, y_off);
					y_off += e.height + 15;
					elements.add(e);
				}
				
				height = (int) y_off - 15;

//				if (height > offset) {
//					double diff = (width - offset) / 2.0;
//					for (Element e2 : elements) {
//						e2.move(0, diff);
//					}
//
//					offset = width;
//				}
			}
		}

		public void setContent(String value) {
			content = value;
			width = content.length() * 12 + 10;
			width = (int)getStringWidth(content) + 10;
			height = 20;
			x_offset = width + 1;
			y_offset = 0;
		}

		public void move(double x, double y) {
			this.x += x;
			this.y += y;
			for (Element e : elements) {
				e.move(x, y);
			}
		}

		public void paint(Graphics2D g) {
			drawContent(g, x, y);
			
			for (Element elm : elements) {
				elm.paint(g);
			}
		}
		
		/**
		 * Draw the content of this element at the specified position
		 *
		 * @param g the graphics context
		 * @param x the x position of the content
		 * @param y the y position of the content
		 */
		public void drawContent(Graphics2D g, double x, double y) {
			int xp = (int) x;
			int yp = (int) y;
			int margin = 5;
			int dm = margin * 2;
			
			g.setColor(hover ? body.darker() : body);
			g.fillRoundRect(xp, yp - margin, width, height + dm, dm, dm);
			
			g.setColor(Color.black);
			Stroke stroke = g.getStroke();
			g.setStroke(new BasicStroke(2));
			g.drawRoundRect(xp, yp - margin, width, height + dm, dm, dm);
			g.setStroke(stroke);
			
			g.setColor(Color.black);
			FontMetrics fm = g.getFontMetrics();
			Rectangle rect = fm.getStringBounds(content, g).getBounds();
			g.drawString(content, xp + 5 - rect.x, yp - rect.y + (height - rect.height) / 2 + 3);
		}

		public void paintLines(Graphics2D g) {
		
		}
	}

	private static List<Object> getElements(Object obj) {
		if (obj instanceof InstFile file) {
			return List.copyOf(file.getProcedures());
		}
		
		if (obj instanceof Procedure proc) {
			return List.copyOf(proc.getInstructions());
		}

		return List.of();
	}
}
