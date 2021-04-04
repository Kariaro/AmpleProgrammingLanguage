package com.hardcoded.visualizer;

import java.awt.Color;

class Range {
	public final String tooltip;
	public final Color color;
	public final int unique;
	public final int start;
	public final int end;
	
	private Range(int start, int end, int unique, Color color,String tooltip) {
		this.tooltip = tooltip;
		this.unique = unique;
		this.color = color;
		this.start = start;
		this.end = end;
	}
	
	public static Range get(int start, int end, Color color, String tooltip) {
		return new Range(start, end, -1, color, tooltip);
	}
	
	public static Range get(int start, int end, int unique, Color color, String tooltip) {
		return new Range(start, end, unique, color, tooltip);
	}
}
