package hc.parser;

// Instruction
public enum Expression {
	NOP, STRING, BYTE, WORD, DWORD, IDENT, /* atoms */
	COR, CAND, LOOP,                       /* comparisons */
	APO, SPO,                              /* pointer */
	CALL,                                  /* call */
	COPY,                                  /* copy */
	COMMA,                                 /* comma */
	RET                                    /* return */
}
