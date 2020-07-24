package hc.token;

@Deprecated
enum BracketType {
	CURLYBRACKET,
	SQUAREBRACKET,
	ROUNDBRACKET,
	;
	
	public static boolean isBracket(String value) {
		if(value == null) return false;
		
		switch(value) {
			case "{": case "}":
			case "[": case "]":
			case "(": case ")": return true;
			default: return false;
		}
	}
	
	public static boolean isOpenBracket(String value) {
		if(!isBracket(value)) return false;
		
		switch(value) {
			case "{": case "[": case "(": return true;
			default: return false;
		}
	}
	
	public static boolean isClosedBracket(String value) {
		if(!isBracket(value)) return false;
		
		switch(value) {
			case "}": case "]": case ")": return true;
			default: return false;
		}
	}
	
	public static BracketType getBracketType(String value) {
		if(!isBracket(value)) return null;
		
		switch(value) {
			case "{": case "}": return CURLYBRACKET;
			case "[": case "]": return SQUAREBRACKET;
			case "(": case ")": return ROUNDBRACKET;
			default: return null;
		}
	}
}
