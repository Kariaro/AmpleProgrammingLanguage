package com.hardcoded.compiler.impl.context;

import com.hardcoded.compiler.lexer.Token;

/**
 * A reference container interface
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public interface IRefContainer {
	Reference getReference();
	void setReference(Reference ref);
	Token getRefToken();
}
