package me.hardcoded.exporter.spooky;

import me.hardcoded.compiler.instruction.IRType;

enum OpCode {
	BINDEF  (0x00),
	TEXT    (0x01),
	DATA    (0x02),
	MOV     (0x03),
	CONST   (0x04),
	ADD     (0x05),
	MUL     (0x06),
	SUB     (0x07),
	DIV     (0x08),
	MOD     (0x0E),
	LT      (0x09),
	EQ      (0x0D),
	JMP     (0x0A),
	EXTERN  (0x0B),
	HALT    (0x0C),
	LEQ     (0x0F),
	JMPADR  (0x10),
	
	
	DEBUG	(0xDEEDBEEF),
	;
	
	final int code;
	private OpCode(int code) {
		this.code = code;
	}
	
	static OpCode convert(IRType type) {
		switch(type) {
			case mov: return MOV;
			case mul: return MUL;
			case add: return ADD;
			case sub: return SUB;
			case div: return DIV;
			case mod: return MOD;
			
			case lte: return LEQ;
			case eq: return EQ;
			case lt: return LT;
			
			default: return null;
		}
	}
	
	static OpCode convertSpecial(IRType type) {
		switch(type) {
			case neq: return EQ;
			case gt: return LT;
			case gte: return LEQ;
			default: return null;
		}
	}
}