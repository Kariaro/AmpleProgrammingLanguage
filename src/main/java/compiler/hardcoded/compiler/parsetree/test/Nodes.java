package hardcoded.compiler.parsetree.test;

import hardcoded.compiler.context.Lang;

@SuppressWarnings("preview")
interface Nodes {
	record modifier() {
		
	}
	
	record type() {
		
	}
	
	record func(modifier[] modifiers, type return_type, String name, param[] parameters, stat body) {
		
	}
	
	record expr() {
		
	}
	
	record stat() {
		
	}
	
	record param(type a, String name) { }
	
	static modifier[] readModifiers(Lang reader) { return new modifier[0]; }
	static param[] readParameters(Lang reader) { return new param[0]; }
	
	static type readType(Lang reader) { return null; }
	static stat readStat(Lang reader) { return null; }
}
