package hardcoded.visualization;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.*;
import java.util.List;

import javax.swing.JPanel;

import hardcoded.parser.LR0_ParserGenerator.IRuleList;
import hardcoded.parser.LR0_ParserGenerator.IState;

/**
 * Deterministic finite automaton visualization
 * https://en.wikipedia.org/wiki/Deterministic_finite_automaton
 * 
 * @author HardCoded
 */
public final class DFAVisualization extends Visualization {
	private DFAPanel panel;
	
	public DFAVisualization() {
		super("DFA - Visualization", 2);
		panel = new DFAPanel();
		panel.setOpaque(true);
		
		frame.setSize(640, 460);
		frame.setContentPane(panel);
		MouseAdapter adapter = new MouseAdapter() {
			private Element selected;
			private Point selectedXY;
			
			private double scrollam = 1;
			private double scroll = 1;
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				double amount = event.getPreciseWheelRotation() / 5.0;
				
				scrollam += amount;
				if(scrollam < 1) scrollam = 1;
				if(scrollam > 20) scrollam = 20;
				
				double last = scroll;
				scroll = Math.pow(1.3, scrollam - 1);
				
				{
					Point mouse = event.getPoint();
					
					// TODO: Allow dragging and zooming.
					
					double xx = mouse.x * (scroll - last);
					double yy = mouse.y * (scroll - last);
					panel.xpos += xx;
					panel.ypos += yy;
				}
				
				
				panel.zoom = 1.0 / scroll;
				panel.repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
				if(event.getButton() == MouseEvent.BUTTON1) {
					selected = getElement(event.getPoint());
					selectedXY = event.getPoint();
					
					if(selected != null) {
						selectedXY.x -= selected.x / scroll;
						selectedXY.y -= selected.y / scroll;
					} else {
						selectedXY.x -= panel.xpos / scroll;
						selectedXY.y -= panel.ypos / scroll;
					}
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent event) {
				if(selected != null) {
					selected.x = (int)((event.getX() - selectedXY.x) * scroll);
					selected.y = (int)((event.getY() - selectedXY.y) * scroll);
					panel.repaint();
				} else {
					// Drag the panel around
					panel.xpos = ((event.getX() - selectedXY.x) * scroll);
					panel.ypos = ((event.getY() - selectedXY.y) * scroll);
					panel.repaint();
				}
			}
			
			private Element getElement(Point mouse) {
				double mx = mouse.x * scroll;
				double my = mouse.y * scroll;
				
				synchronized(panel.elements) {
					for(int i = panel.elements.size() - 1; i >= 0; i--) {
						Element elm = panel.elements.get(i);
						if(elm.hasMouse(mx - panel.xpos, my - panel.ypos)) return elm;
					}
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
		
		@Override
		public void paint(Graphics gr) {
			Graphics2D g = (Graphics2D)gr;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setFont(font);
			
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
			
			g.scale(zoom, zoom);
			g.translate(xpos, ypos);
			
			synchronized(elements) {
				for(Element e : elements) e.paintLines(g);
				for(Element e : elements) e.paint(g);
			}
		}
		
		public void display(List<IState> states) {
			System.out.println("Display: " + states.size());
			
			synchronized(elements) {
				elements.clear();
				
				for(IState state : states) {
					Element e = new Element(DFAPanel.this, state);
					elements.add(e);
				}
				
				List<IState> search = new ArrayList<>();
				IState start = states.get(0);
				search.addAll(start.next);
				
				Random random = new Random(0);
				IState last = null;
				
				Set<IState> searched = new HashSet<>();
				while(!search.isEmpty()) {
					IState state = search.get(0);
					search.remove(0);
					
					if(!searched.contains(state)) {
						{
							Element elm = getElement(state);
							
							if(last != null) {
								Element ell = getElement(last);
								double dsty = (elm.height + ell.height) / 2.0 + 50;
								double dstx = (elm.width + ell.width) / 2.0 + 50;
								double dst = Math.sqrt(dstx * dstx + dsty * dsty);
								
								double angle = random.nextDouble() * Math.PI * 2;
								double cos = Math.cos(angle);
								double sin = Math.sin(angle);
								
								double xx = dst * cos;
								double yy = dst * sin;
								elm.x = ell.x + (int)xx;
								elm.y = ell.y + (int)yy;
							}
						}
						
						
						searched.add(state);
						search.addAll(state.next);
						
						last = state;
					}
				}
			}
		}
		
		public Element getElement(IState state) {
			synchronized(elements) {
				for(Element e : elements) {
					if(e.stateName.equals(state.getName())) return e;
				}
			}
			
			return null;
		}
	}
	
	private class Element {
		//public List<IRuleList> list;
		public IState state;
		public int x;
		public int y;
		public int height;
		public int width;
		
		private DFAPanel parent;
		private String stateName;
		private final String[] text;
		
		public Element(DFAPanel parent, IState state) {
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
			int cx = x + width / 2;
			int cy = y + height / 2;
			for(IState next : state.next) {
				Element e = parent.getElement(next);
				if(e == null) continue; // This should not happen
				
				int mx = e.x + e.width / 2;
				int my = e.y + e.height / 2;
				
				g.drawLine(cx, cy, mx, my);
			}
		}
		
		public boolean hasMouse(double mx, double my) {
			return (mx >= x && mx <= (x + width))
				&& (my >= y && my <= (y + height));
		}
	}
}
