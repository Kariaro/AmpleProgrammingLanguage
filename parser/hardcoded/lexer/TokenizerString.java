package hardcoded.lexer;

/**
 * This class is used by the Tokenizer to generate the tokens.
 * 
 * @author HardCoded
 */
final class TokenizerString implements CharSequence {
	private byte[] bytes;
	private int index;
	
	/**
	 * Create a new TokenizerString that is used by the lexer.
	 * 
	 * @param bytes
	 * @throws NullPointerException if the array was null
	 */
	public TokenizerString(byte[] bytes) {
		this.bytes = bytes.clone();
	}
	
	@Override
	public char charAt(int index) {
		return (char)Byte.toUnsignedInt(bytes[this.index + index]);
	}
	
	@Override
	public int length() {
		return bytes.length - index;
	}
	
	private int lineIndex = 1;
	private int columnIndex = 1;
	
	int getLine() { return lineIndex; }
	int getColumn() { return columnIndex; }
	int getIndex() { return index; }
	
	void move(int index) {
		if(index > 0) {
			for(int i = 0; i < index; i++) {
				char c = charAt(i);
				
				if(c == '\n') {
					lineIndex++;
					columnIndex = 1;
				} else {
					if(c == '\t') columnIndex += 3; // Notepad++, Eclipse counts tabs as 4 cols.
					columnIndex++;
				}
			}
		}
		
		this.index += index;
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		byte[] next = new byte[end - start];
		System.arraycopy(bytes, index + start, next, 0, next.length);
		return new TokenizerString(next);
	}
	
	@Override
	public String toString() {
		return new String(bytes, index, length());
	}
}
