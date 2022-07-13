package me.hardcoded.visualization;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorCache {
	private final Map<Integer, Color> colorCache;
	
	ColorCache() {
		this.colorCache = new HashMap<>();
	}
	
	public Color getColor(int rgb) {
		return colorCache.computeIfAbsent(rgb, v -> new Color(v, false));
	}
	
	public Color getColor(int r, int g, int b, int a) {
		return getColor(toRgba(r, g, b, a), true);
	}
	
	public Color getColor(float r, float g, float b, float a) {
		return getColor(toRgba(r, g, b, a), true);
	}
	
	public Color getColor(int rgba, boolean hasAlpha) {
		return colorCache.computeIfAbsent(rgba, v -> new Color(v, hasAlpha));
	}
	
	public void clear() {
		colorCache.clear();
	}
	
	private int toRgba(float r, float g, float b, float a) {
		return toRgba((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255));
	}
	
	private int toRgba(int r, int g, int b, int a) {
		return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	}
}
