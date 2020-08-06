package hardcoded.grammar;

import java.io.IOException;
import java.io.Reader;

public class BNFGrammarReader implements GrammarReaderImpl {
	BNFGrammarReader() {}
	
	@Override
	public Grammar load(Reader reader) throws IOException {
		throw new UnsupportedOperationException("Grammar is not implemented");
	}
	
}
