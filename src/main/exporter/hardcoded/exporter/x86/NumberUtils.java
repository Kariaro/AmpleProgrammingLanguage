package hardcoded.exporter.x86;

final class NumberUtils {
	public static int getBitsSize(long value) {
		value = (value < 0 ? - (value + 1):value);
		
		// The eight was added because 0xff should be treated as 16 bit
		// otherwise it would encode -1 as a 8 bit value and that is wrong.
		if((value & 0xffffffff80000000L) != 0) return 64; else
		if((value &         0xffff8000L) != 0) return 32; else
		if((value &             0xff80L) != 0) return 16;
		else return 8;
	}
}
