package hardcoded.visualization;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.*;
import java.util.List;

import javax.swing.JPanel;

import hardcoded.parser.GLRParserGenerator.IRuleList;
import hardcoded.parser.GLRParserGenerator.IState;

/**
 * Deterministic finite automaton visualization
 * https://en.wikipedia.org/wiki/Deterministic_finite_automaton
 * 
 * @author HardCoded
 */
public final class DFAVisualization extends Visualization {
	// TODO: Add a cordinate bar and a search bar..
	// TODO: Add more sorting options.
	// TODO: Make this graph usable by other people that want to see how to make graphs maybe :D
	
	private DFAPanel panel;
	
	public DFAVisualization() {
		super("DFA - Visualization", 2);
		panel = new DFAPanel();
		panel.setOpaque(true);
		
		frame.setSize(640, 460);
		frame.setContentPane(panel);
		MouseAdapter adapter = new MouseAdapter() {
			private Element selected;
			private double selectedX;
			private double selectedY;
			
			private boolean dragging = false;
			private double scrollam = 1;
			private double scroll = 1;
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				double amount = event.getPreciseWheelRotation() / 5.0;
				
				scrollam += amount;
				if(scrollam < -2) scrollam = -2;
				if(scrollam > 20) scrollam = 20;
				
				double last = scroll;
				scroll = Math.pow(1.3, scrollam - 1);
				
				{
					Point mouse = event.getPoint();
					double xx = mouse.x * (scroll - last);
					double yy = mouse.y * (scroll - last);
					panel.xpos += xx;
					panel.ypos += yy;
					
					
					if(selected == null) {
						//selectedX -= panel.xpos;
						//selectedY -= panel.ypos;
					} else {
						selectedX = mouse.x * scroll;
						selectedY = mouse.y * scroll;
						selectedX -= selected.x;
						selectedY -= selected.y;
					}
				}
				// TODO: The cursor does not stay in the same position if the user is scrolling while dragging stuff..
				
				panel.zoom = 1.0 / scroll;
				panel.repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
				if(event.getButton() == MouseEvent.BUTTON3) {
					System.out.println("Update");
					barycentric_fdgl();
					panel.repaint();
				}
				
				if(event.getButton() != MouseEvent.BUTTON1) return;
				dragging = true;
				
				Point mouse = event.getPoint();
				selected = getElement(mouse);
				selectedX = mouse.x * scroll;
				selectedY = mouse.y * scroll;
				
				if(selected != null) {
					selectedX -= selected.x;
					selectedY -= selected.y;
				} else {
					selectedX -= panel.xpos;
					selectedY -= panel.ypos;
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				selected = null;
				dragging = false;
			}
			
			@Override
			public void mouseDragged(MouseEvent event) {
				if(!dragging) return;
				
				double mx = event.getX() * scroll - selectedX;
				double my = event.getY() * scroll - selectedY;
				
				if(selected != null) {
					selected.x = mx;
					selected.y = my;
				} else {
					panel.xpos = mx;
					panel.ypos = my;
				}
				
				panel.repaint();
			}
			
			private Element getElement(Point mouse) {
				double mx = mouse.x * scroll - panel.xpos;
				double my = mouse.y * scroll - panel.ypos;
				
				for(int i = panel.elements.size() - 1; i >= 0; i--) {
					Element elm = panel.elements.get(i);
					if(elm.hasMouse(mx, my)) return elm;
				}
				
				return null;
			}
		};
		
		panel.addMouseListener(adapter);
		panel.addMouseMotionListener(adapter);
		panel.addMouseWheelListener(adapter);
	}
	
	@SuppressWarnings("unchecked")
	public void show(Object... args) {
		if(args.length != 1) throw new IllegalArgumentException("Expected one argument.");
		panel.display((List<IState>)args[0]);
		frame.setVisible(true);
		
//		new Thread(() -> {
//			while(true) {
//				try {
//					Thread.sleep(1);
//				} catch(InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//				//apply_fdg();
//				barycentric_fdgl();
//				panel.repaint();
//			}
//		}).start();
		
		new Thread(() -> {
			for(int i = 0; i < 1000000; i++) {
				apply_fdg();
				panel.repaint();
			}
			
			for(int i = 0; i < 1000; i++) {
				//barycentric_fdgl();
				panel.repaint();
			}
		}).start();
	}
	
	public void hide() {
		frame.setVisible(true);
	}
	
	private class DFAPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private List<Element> elements = new ArrayList<>();
		private Font font = new Font("Monospaced", Font.PLAIN, 18);
		
		private double zoom = 1;
		private double xpos = 0;
		private double ypos = 0;
		
		public void paint(Graphics gr) {
			Graphics2D g = (Graphics2D)gr;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setFont(font);
			
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
			
			g.scale(zoom, zoom);
			g.translate(xpos, ypos);
			
			for(Element e : elements) e.paintLines(g);
			for(Element e : elements) e.paint(g);
		}
		
		public void display(List<IState> states) {
			elements.clear();
			
			for(IState state : states) {
				Element e = new Element(DFAPanel.this, state);
				elements.add(e);
			}
			
			for(Element i : elements) {
				for(IState state : i.state.next) {
					Element k = getElement(state);
					
					int ii = i.connections.indexOf(k);
					int ik = k.connections.indexOf(i);
					if(ii < 0) i.connections.add(k);
					if(ik < 0) k.connections.add(i);
				}
			}
			
			List<IState> search = new ArrayList<>();
			IState start = states.get(0);
			search.addAll(start.next);
			
			Random random = new Random(0);
			
			// ----: Maybe implement Force-directed graph drawing
			for(Element e : elements) {
				e.x = (random.nextDouble() - 0.5) * 100;
				e.y = (random.nextDouble() - 0.5) * 100;
			}
		}
		
		public Element getElement(IState state) {
			for(Element e : elements) {
				if(e.stateName.equals(state.getName())) return e;
			}
			
			return null;
		}
	}
	
	private void apply_fdg() {
		List<Element> elements = panel.elements;
		Set<Element> visitied = new HashSet<>();
		
		double SPEED_DIVISOR = 800;
		double AREA_MULTIPLICATOR = 30000000;
		
		double speed = 0.1;
		double gravity = 1;
		double area = 10000;
		
		double maxDisplace = Math.sqrt(AREA_MULTIPLICATOR * area) / 10D;
		double k = Math.sqrt(AREA_MULTIPLICATOR * area) / (1D + elements.size());
		
		for(Element v : elements) {
			v.dirX = 0;
			v.dirY = 0;
		}
		
		for(Element v : elements) {
			//k = Math.sqrt(v.width * v.height) * 100 + 1;
			
			for(Element u : elements) {
				if(v == u) continue;
				
				double dx = v.x - u.x;
				double dy = v.y - u.y;
				double ds = Math.sqrt(dx * dx + dy * dy);
				
				if(ds > 0) {
					double rf = k * k / ds;
					v.dirX += dx / ds * rf;
					v.dirY += dy / ds * rf;
				}
			}
		}
		
		for(Element v : elements) {
			//k = Math.sqrt(v.width * v.height) + 1;
			visitied.add(v);
			
			for(Element u : v.connections) {
				if(visitied.contains(u)) continue;
				
				double dx = v.x - u.x;
				double dy = v.y - u.y;
				double ds = Math.sqrt(dx * dx + dy * dy);
				
				if(ds > 0) {
					double af = ds * ds / k;
					v.dirX -= dx / ds * af;
					v.dirY -= dy / ds * af;
					u.dirX += dx / ds * af;
					u.dirY += dy / ds * af;
				}
			}
		}
		
		for(Element v : elements) {
			//k = Math.sqrt(v.width * v.height) + 1;
			
			double d = Math.sqrt(v.x * v.x + v.y * v.y);
			double g = 0.01 * k * gravity * d;
			v.dirX -= g * v.x / d;
			v.dirY -= g * v.y / d;
			
			v.dirX *= speed / SPEED_DIVISOR;
			v.dirY *= speed / SPEED_DIVISOR;
		}
		
		for(Element v : elements) {
			double dx = v.dirX;
			double dy = v.dirY;
			double ds = Math.sqrt(dx * dx + dy * dy);
			
			if(ds > 0) {
				double ld = Math.min(maxDisplace * (speed / SPEED_DIVISOR), ds);
				v.x += dx / ds * ld;
				v.y += dy / ds * ld;
			}
		}
	}
	
	// http://cs.brown.edu/people/rtamassi/gdhandbook/chapters/force-directed.pdf
	private void barycentric_fdgl() {
		double W = 50000;
		double L = 50000;
		double M = 3000;
		double MM = 1000;
		
		List<Element> elements = panel.elements;
		
		for(int i = 1; i < 20; i++) {
			for(Element v : elements) {
				v.dirX = 0;
				v.dirY = 0;
				double vw = Math.min(1, Math.sqrt(v.width * v.height) / 1000.0);
				
				for(Element u : elements) {
					if(v == u) continue;
					
					double dx = u.x - v.x;
					double dy = u.y - v.y;
					
					double mass = Math.min(1, Math.log(u.width * u.height) / 100.0);
					double ds = (Math.sqrt(dx * dx + dy * dy) / mass) + 1;
					
					if(ds < M * vw / mass) ds = -ds;
					if(ds > MM) continue;
					
					v.dirX += dx / (ds * 1);
					v.dirY += dy / (ds * 1);
					
					
					u.dirX += dx / (ds * 1);
					u.dirY += dy / (ds * 1);
				}
			}
			
			for(Element v : elements) {
				double vw = Math.min(1, Math.sqrt(v.width * v.height) / 1000.0);
				for(Element u : v.connections) {
					if(v == u) continue;
					
					double dx = u.x - v.x;
					double dy = u.y - v.y;
					
					double mass = Math.min(1, Math.log(u.width * u.height) / 100.0);
					double ds = (Math.sqrt(dx * dx + dy * dy) / mass) + 1;
					
					if(ds < M * vw / mass) ds = -ds;
					
					v.dirX += dx / ds;
					v.dirY += dy / ds;
					
					u.dirX -= dx / ds;
					u.dirY -= dy / ds;
				}
			}
			
			for(Element v : elements) {
				double ds = Math.sqrt(v.dirX * v.dirX + v.dirY * v.dirY) + 1;
				double d2 = Math.sqrt(v.x * v.x + v.y * v.y) + 1;
				v.dirX -= 0.1 * v.x / d2;
				v.dirY -= 0.1 * v.y / d2;
				
				v.x += v.dirX / ds;
				v.y += v.dirY / ds;

				v.x = Math.min(W, Math.max(-W, v.x));
				v.y = Math.min(L, Math.max(-L, v.y));
			}
		}
		
		{
			double xm = 0;
			double ym = 0;
			for(Element e : panel.elements) {
				xm += e.x;
				ym += e.y;
			}
			
			xm /= (double)panel.elements.size();
			ym /= (double)panel.elements.size();
			
			for(Element e : panel.elements) {
				e.x -= xm;
				e.y -= ym;
			}
		}
		
		{
			//Element center = elements.get(0);
			//center.x = center.y = 0;
		}
	}
	
	private class Element {
		public IState state;
		public double x;
		public double y;
		public int height;
		public int width;
		
		private double dirX;
		private double dirY;
		
		private DFAPanel parent;
		private String stateName;
		private final String[] text;
		
		// Contains all connected elements to this one..
		private List<Element> connections;
		
		public Element(DFAPanel parent, IState state) {
			this.connections = new ArrayList<>();
			this.parent = parent;
			this.state = state;
			
			text = new String[state.size()];
			stateName = state.getName();
			
			String lastName = null;
			
			int paddingLength = 1;
			for(int i = 0; i < state.size(); i++) {
				IRuleList set = state.getRule(i);
				if(set.itemName.length() > paddingLength) paddingLength = set.itemName.length();
			}
			
			for(int i = 0; i < state.size(); i++) {
				IRuleList set = state.getRule(i);
				
				String string;
				if(!set.itemName.equals(lastName)) {
					lastName = set.itemName;
					string = lastName;
				} else {
					string = "";
				}
				
				text[i] = String.format("%-" + paddingLength + "s", string) + " > " + set;
			}
			
			paddingLength = 1;
			for(int i = 0; i < text.length; i++) {
				if(text[i].length() > paddingLength) paddingLength = text[i].length();
			}
			
			width = paddingLength * 12;
			height = text.length * 25;
		}
		
		public void paint(Graphics2D g) {
			int x = (int)this.x;
			int y = (int)this.y;
			
			g.setColor(Color.lightGray);
			g.fillRoundRect(x - 15, y - 5, width + 30, height + 10, 30, 30);
			
			g.setColor(Color.black); {
				Stroke stroke = g.getStroke();
				g.setStroke(new BasicStroke(2));
				g.drawRoundRect(x - 15, y - 5, width + 30, height + 10, 30, 30);
				g.setStroke(stroke);
			}
			
			FontMetrics fm = g.getFontMetrics(); {
				Rectangle rect = fm.getStringBounds(stateName, g).getBounds();
				g.drawString(stateName, x - rect.x + (width - rect.width) / 2, y - rect.y - rect.height / 2 - 20);
			}
			
			Rectangle rect = fm.getStringBounds(" ", g).getBounds();
			
			g.setColor(Color.white);
			for(int i = 0; i < text.length; i++) {
				g.drawString(text[i], x, y - rect.y + rect.height * i);
			}
		}
		
		public void paintLines(Graphics2D g) {
			g.setColor(Color.black);
			int cx = (int)x + width / 2;
			int cy = (int)y + height / 2;
			for(IState next : state.next) {
				Element e = parent.getElement(next);
				if(e == null) continue; // This should not happen
				
				int mx = (int)e.x + e.width / 2;
				int my = (int)e.y + e.height / 2;
				
				g.drawLine(cx, cy, mx, my);
			}
		}
		
		public boolean hasMouse(double mx, double my) {
			return (mx + 15 >= x && mx - 15 <= (x + width))
				&& (my + 10 >= y && my - 10 <= (y + height));
		}
	}
}
