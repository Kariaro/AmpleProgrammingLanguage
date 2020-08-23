package hardcoded.visualization;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import hardcoded.tree.ParseTree;
import hardcoded.tree.ParseTree.PNode;

/**
 * @author HardCoded
 */
public final class PTVisualization extends Visualization {
	private PTPanel panel;
	
	public PTVisualization() {
		super("ParseTree - Visualization", 2);
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
					
					selectedX = mouse.x * scroll - panel.xpos;
					selectedY = mouse.y * scroll - panel.ypos;
				}
				
				panel.zoom = 1.0 / scroll;
				panel.repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
				if(event.getButton() != MouseEvent.BUTTON1) return;
				selectedX = event.getX() * scroll - panel.xpos;
				selectedY = event.getY() * scroll - panel.ypos;
			}
			
			@Override
			public void mouseDragged(MouseEvent event) {
				panel.xpos = event.getX() * scroll - selectedX;
				panel.ypos = event.getY() * scroll - selectedY;
				panel.repaint();
			}
		};
		
		panel.addMouseListener(adapter);
		panel.addMouseMotionListener(adapter);
		panel.addMouseWheelListener(adapter);
	}
	
	public void show(Object... args) {
		if(args.length != 1) throw new IllegalArgumentException("Expected one argument.");
		panel.display((ParseTree)args[0]);
		frame.setVisible(true);
	}
	
	public void hide() {
		frame.setVisible(true);
	}
	
	private class PTPanel extends JPanel {
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
		
		@SuppressWarnings("deprecation")
		public void display(ParseTree parseTree) {
			elements.clear();
			
			ParseTree tree = new ParseTree(parseTree);
			tree.group();
			
			PNode node = new PNode("ENTRY");
			node.nodes.addAll(tree.nodes);
			
			Element entry = new Element(null, node);
			elements.add(entry);
			entry.move(-(entry.x + (entry.offset - entry.width) / 2), -entry.y);
		}
	}
	
	private class Element {
		public double x;
		public double y;
		public int height;
		public int width;
		public double offset;
		
		private List<Element> elements;
		private String content;
		
		private Element(Element parent, PNode node) {
			this.elements = new ArrayList<>();
			this.content = node.value;
			
			width = content.length() * 12 + 10;
			height = 20;
			
			offset = width + 1;
			
			if(!node.nodes.isEmpty()) {
				offset = 0;
				
				for(PNode n : node.nodes) {
					Element e = new Element(this, n);
					e.move(offset, 140);
					
					offset += e.offset;
					elements.add(e);
				}
				
				if(width > offset) {
					double diff = (width - offset) / 2.0;
					for(Element e : elements) e.move(diff, 0);
					
					offset = width;
				}
			}
		}
		
		public void move(double x, double y) {
			this.x += x;
			this.y += y;
			for(Element e : elements) e.move(x, y);
		}
		
		public void paint(Graphics2D g) {
			int x = (int)this.x;
			int y = (int)this.y;
			
			int xp = x;
			
			if(!elements.isEmpty()) {
				xp += (offset - width) / 2;
			}
			
			if((xp + width + panel.xpos < 0)
			|| (xp - width + panel.xpos > panel.getWidth() / panel.zoom)
			|| (y - 5 + (height + 10) + panel.ypos < 0)
			|| (y - 5 - (height + 10) + panel.ypos > panel.getHeight() / panel.zoom)) {
				
			} else {
				g.setColor(Color.lightGray);
				g.fillRoundRect(xp, y - 5, width, height + 10, 10, 10);
				
				g.setColor(Color.black);
				Stroke stroke = g.getStroke();
				g.setStroke(new BasicStroke(2));
				g.drawRoundRect(xp, y - 5, width, height + 10, 10, 10);
				g.setStroke(stroke);
				
				g.setColor(Color.black);
				FontMetrics fm = g.getFontMetrics(); {
					Rectangle rect = fm.getStringBounds(content, g).getBounds();
					g.drawString(content, x - rect.x + ((int)offset - rect.width) / 2, y - rect.y + (height - rect.height) / 2);
				}
			}
			
			for(Element elm : elements) {
				elm.paint(g);
			}
		}
		
		public void paintLines(Graphics2D g) {
			int x0 = (int)(x + offset / 2D);
			int y0 = (int)y;
			
			g.setColor(Color.black);
			for(Element elm : elements) {
				int x1 = (int)(elm.x + elm.offset / 2D);
				int y1 = (int)elm.y;
				
				g.drawLine(x0, y0, x1, y1);
				elm.paintLines(g);
			}
		}
	}
}
