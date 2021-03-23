package com.hardcoded.visualizer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferStrategy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TextArea extends Canvas {
	private static final long serialVersionUID = 1L;
	
	private Font font = new Font("Courier New", Font.PLAIN, 14);
	private String[] lines = new String[0];
	private int[] index = new int[0];
	private Point mouse;
	private int scroll;
	private int visible_rows = 37;
	
	public TextArea() {
		Dimension dim = new Dimension(1000, 629);
		setMinimumSize(dim);
		setPreferredSize(dim);
		setFontSize(14);
		
		MouseAdapter adapter = new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				mouse = e.getPoint();
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int next_scroll = (int)(scroll + e.getWheelRotation());
				if(next_scroll < 0) next_scroll = 0;
				scroll = next_scroll;
				
				if(scroll >= index.length) {
					scroll = index.length - 1;
				}
			}
		};
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
				sb.append(' ');
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
	
	private BufferStrategy bs;
	public void paint(Graphics gr) {
		if(getBufferStrategy() == null) {
			createBufferStrategy(2);
			bs = getBufferStrategy();
		}
		
		if(scroll >= index.length) {
			scroll = index.length - 1;
		}
		
		Graphics2D g = (Graphics2D)bs.getDrawGraphics();//(Graphics2D)gr.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setFont(font);
		
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.translate(30, 0);
		g.setColor(Color.gray);
		g.drawLine(0, 0, 0, getHeight());
		String[] array = this.lines;
		for(int j = 0; j < Math.min(scroll + visible_rows, array.length); j++) {
			String line = array[j];
			int i = j - scroll;
			
			g.setColor(Color.black);
			g.drawString(line, 0, i * font_h + font_offset);
			
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
		g.translate(-30, 0);
		
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
		
		bs.show();
	}
	
	private boolean drawRange(Graphics2D g, Range range) {
		g.setColor(range.color);
		int s = range.start;
		int e = range.end;
		
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
				int cs = s - A;
				int ce = Math.min(e - s, len);
				inside |= fillRect(g, cs * font_w, i * font_h, ce * font_w, font_h);
				break;
			}
			
			// A....s###B
			if(s >= A && e >= B) {
				int cs = s - A;
				int ce = Math.min(e - cs, len - cs);
				inside |= fillRect(g, cs * font_w, i * font_h, ce * font_w, font_h);
			}
			
			// A########B
			if(s <= A && e >= B) {
				inside |= fillRect(g, 0, i * font_h, len * font_w, font_h);
				continue;
			}
			
			// A####e...B
			if(s <= A && e <= B) {
				// The text is inside the range
				// Our thing is above the start line
				int ce = Math.min(e - A, len);
				
				inside |= fillRect(g, 0, i * font_h, ce * font_w, font_h);
				break;
			}
		}
		
		return inside;
	}
	
	
	boolean drawRect(Graphics2D g, float x, float y, float w, float h) {
		g.drawRect((int)x, (int)y, (int)w, (int)h);
		return false;
	}
	
	boolean fillRect(Graphics2D g, float x, float y, float w, float h) {
		g.fillRect((int)x, (int)(y - scroll * font_h), (int)w, (int)h);
		
		Point m = mouse;
		if(m != null) {
			int mx = m.x - 30;
			int my = (int)(m.y + scroll * font_h);
			
			return (mx >= x && my >= y && (mx < x + w) && (my < y + h));
		}
		
		return false;
	}
}
