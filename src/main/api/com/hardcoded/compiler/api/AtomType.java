package com.hardcoded.compiler.api;

public enum AtomType {
//	i64, i32, i16, i8,
//	u64, u32, u16, u8,
//	f64, f32,
	number,
	
	// String constant
	string,
	
	// Reference to another variable
	ref,
}
