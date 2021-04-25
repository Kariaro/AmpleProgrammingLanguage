package com.hardcoded.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * A number utility class
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public final class NumberUtils {
	private NumberUtils() {
		
	}
	
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##########", DecimalFormatSymbols.getInstance(Locale.US));
	
	public static String toString(double value) {
		return NUMBER_FORMAT.format(value);
	}
}
