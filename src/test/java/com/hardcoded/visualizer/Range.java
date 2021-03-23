package com.hardcoded.visualizer;

import java.awt.Color;

class Range {
	public final String tooltip;
	public final Color color;
	public final int start;
	public final int end;
	private Range(Color color, int start, int end, String tooltip) {
		this.color = color;
		this.start = start;
		this.end = end;
		this.tooltip = tooltip;
	}
	
	public static Range get(int start, int end, Color color, String tooltip) {
		return new Range(color, start, end, tooltip);
	}
}
