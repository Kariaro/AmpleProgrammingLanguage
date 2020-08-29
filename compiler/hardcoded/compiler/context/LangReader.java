package hardcoded.compiler.context;

/**
 * This is a reader for the language.
 * @author HardCoded
 */
public final class LangReader {
	private Sym sym;
	public LangReader(Sym sym) {
		this.sym = sym;
	}
	
	// TODO: Add more context
	
	public LangReader next() {
		sym.next();
		return this;
	}
	
	public LangReader prev() {
		sym.prev();
		return this;
	}
	
	public String group() {
		return sym.group();
	}
	
	public String value() {
		return sym.value();
	}
	
	public boolean groupEquals(String string) {
		return sym.groupEquals(string);
	}
	
	public boolean valueEquals(String string) {
		return sym.valueEquals(string);
	}
}
