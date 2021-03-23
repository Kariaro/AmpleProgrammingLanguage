package com.hardcoded.visualizer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class TextArea extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Font font = new Font("Courier New", Font.PLAIN, 14);
	private String[] lines = new String[0];
	private int[] index = new int[0];
	private Point mouse;
	private int scroll;
	private int visible_rows = 37;
	private int tab_size = 4;
	
	public TextArea() {
		Dimension dim = new Dimension(1000, 629);
		setMinimumSize(dim);
		setPreferredSize(dim);
		setBackground(Color.white);
		setFontSize(14);
		setDoubleBuffered(true);
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				mouse = e.getPoint();
				repaint();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				mouse = null;
				repaint();
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int next_scroll = (int)(scroll + e.getWheelRotation());
				if(next_scroll < 0) next_scroll = 0;
				scroll = next_scroll;
				
				if(scroll >= index.length) {
					scroll = index.length - 1;
				}
				
				repaint();
			}
		};
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		addMouseWheelListener(adapter);
	}
	
	public void setText(String text) {
		setText(text.getBytes(StandardCharsets.ISO_8859_1));
	}
	
	public void setText(byte[] bytes) {
		ranges.clear();
		IntList intList = new IntList();
		List<String> list = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		int start = 0;
		for(int i = 0; i < bytes.length; i++) {
			char c = (char)((int)bytes[i] & 0xff);
			
			if(c == '\r') continue;
			if(c == '\n') {
				list.add(sb.toString());
				sb.delete(0, sb.length());
				intList.add(start);
				start = i + 1;
				continue;
			}
			
			if(c == '\t') {
				//sb.append('\u00bb');
				//sb.append(' ');
				sb.append('\t');
			} else {
				sb.append(c);
			}
		}
		
		if(!sb.isEmpty()) {
			list.add(sb.toString());
			intList.add(start);
		}
		
		lines = list.toArray(String[]::new);
		index = intList.toArray();
	}
	
	private float font_offset;
	private float font_w;
	private float font_h;
	public void setFontSize(float size) {
		font = font.deriveFont(size);
		FontMetrics fm = getFontMetrics(font);
		font_offset = fm.getAscent();
		font_w = fm.stringWidth(" ");
		font_h = fm.getHeight();
	}
	
	private List<Range> ranges = new ArrayList<>();
	public void addRange(Range range) {
		ranges.add(range);
	}
	
	public void paintComponent(Graphics gr) {
		if(scroll >= index.length) scroll = index.length - 1;
		if(scroll < 0) scroll = 0;
		
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setFont(font);
		
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.translate(33, 0);
		g.setColor(Color.gray);
		g.drawLine(0, 0, 0, getHeight());
		String[] array = this.lines;
		for(int j = 0; j < Math.min(scroll + visible_rows, array.length); j++) {
			String line = array[j];
			int i = j - scroll;
			
			g.setColor(Color.black);
			drawString(g, line, 0, i * font_h + font_offset);
			
			g.setColor(Color.lightGray);
			//g.drawRect(0, (int)(i * font_h), (int)(line.length() * font_w), (int)font_h);
		}
		
		Range inside_range = null;
		Range[] range_array = ranges.toArray(Range[]::new);
		for(int i = 0; i < range_array.length; i++) {
			Range range = range_array[i];
			boolean inside = drawRange(g, range);
			
			if(inside) {
				inside_range = range;
			}
		}
		g.translate(-33, 0);
		
		if(inside_range != null) {
			Point m = mouse;
			if(m != null) {
				String tip = inside_range.tooltip;
				String ran = String.format("(start: %d, end: %d)", inside_range.start, inside_range.end);
				int mx = m.x + 15;
				int my = m.y - 6;
				int ln = (int)(Math.max(tip.length(), ran.length()) * font_w);
				
				g.setColor(Color.gray);
				g.fillRect(mx, my, ln + 2, 18 + 16);
				g.setColor(Color.white);
				g.fillRect(mx + 1, my + 1, ln, 32);
				g.setColor(Color.black);
				g.drawString(tip, mx + 1, my + font_offset);
				g.drawString(ran, mx + 1, my + font_offset + font_h);
			}
		}
		
		g.setColor(Color.black);
		int[] index = this.index;
		for(int i = scroll; i < Math.min(scroll + visible_rows, index.length); i++) {
			String line = String.valueOf(index[i]);
			g.drawString(line, 0, (i - scroll) * font_h + font_offset);
		}
		
		//gr.drawImage(bi, 0, 0, null);
		//g.dispose();
	}
	
	private boolean drawRange(Graphics2D g, Range range) {
		g.setColor(range.color);
		int s = range.start;
		int e = range.end;
		if(s == e) return false;
		
		// ....###
		// #######
		// ###....
		
		String[] lines = this.lines.clone();
		int[] index = this.index.clone();
		
		boolean inside = false;
		for(int i = scroll; i < Math.min(scroll + visible_rows, index.length); i++) {
			int A = index[i];
			int len = lines[i].length();
			int B = (i + 1 < index.length ? index[i + 1]:(A + len));
			
			// A....s##eB
			if(s >= A && e <= B) {
				inside |= fillStringRect(g, lines[i], i * font_h, s - A, e - A);
				break;
			}
			
			// A....s###B
			if(s >= A && s < B && e >= B) {
				inside |= fillStringRect(g, lines[i], i * font_h, s - A, len);
				continue;
			}
			
			// A########B
			if(s < A && e >= B) {
				inside |= fillStringRect(g, lines[i], i * font_h, 0, len);
				continue;
			}
			
			// A####e...B
			if(s <= A && e > A && e <= B) {
				inside |= fillStringRect(g, lines[i], i * font_h, 0, e - A);
				break;
			}
		}
		
		return inside;
	}
	
	private void drawString(Graphics2D g, String s, float x, float y) {
		byte[] bytes = s.getBytes();
		//g.drawString(s, x, y);
		
		int ox = 0;
		for(int i = 0; i < bytes.length; i++) {
			char c = (char)(((int)bytes[i] & 0xff));
			
			if(c == '\t') {
				ox += (tab_size - (ox % tab_size));
				continue;
			}
			
			g.drawString("" + c, (ox++) * font_w, y);
		}
	}
	
	boolean drawRect(Graphics2D g, float x, float y, float w, float h) {
		g.drawRect((int)x, (int)y, (int)w, (int)h);
		return false;
	}
	
	boolean fillRect(Graphics2D g, float x, float y, float w, float h) {
		g.fillRect((int)x, (int)(y - scroll * font_h), (int)w, (int)h);
		
		Point m = mouse;
		if(m != null) {
			int mx = m.x - 33;
			int my = (int)(m.y + scroll * font_h);
			
			return (mx >= x && my >= y && (mx < x + w) && (my < y + h));
		}
		
		return false;
	}
	
	boolean fillStringRect(Graphics2D g, String line, float y, int s, int e) {
		int oxs = 0;
		int oxe = 0;
		int ox = 0;
		for(int i = 0; i < e; i++) {
			if(i == s) oxs = ox;
			
			if(line.charAt(i) == '\t') {
				ox += (tab_size - (ox % tab_size));
				continue;
			}
			
			ox++;
		}
		
		oxe = ox;
		
		return fillRect(g, (oxs) * font_w, y, (oxe - oxs) * font_w, font_h);
	}
}
