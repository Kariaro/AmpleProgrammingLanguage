package hardcoded.compiler;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.types.Type;

public class Identifier {
	public enum IdType {
		clazz,
		funct,
		param,
		var
	}
	
	private IdType id_type;
	private String name;
	private int index;
	private boolean temp;
	public Function func;
	private AtomType low_type;
	private Type high_type;
	
	public Function func() {
		return func;
	}
	
	public String name() {
		return name;
	}
	
	public IdType idtype() {
		return id_type;
	}
	
	public Type highType() {
		return high_type;
	}
	
	public AtomType atomType() {
		if(low_type != null) return low_type;
		return high_type == null ? null:high_type.atomType();
	}
	
	public int index() {
		return index;
	}
	
	public boolean temp() {
		return temp;
	}
	
	public boolean hasType() {
		return id_type == IdType.var || id_type == IdType.param || id_type == IdType.funct;
	}
	
	public static Identifier createFuncIdent(String name, int index, Function func) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.func = func;
		ident.id_type = IdType.funct;
		ident.high_type = func.returnType;
		ident.low_type = ident.high_type.atomType();
		return ident;
	}
	
	public static Identifier createVarIdent(String name, int index, AtomType type) { return createVarIdent(name, index, type, false); }
	public static Identifier createVarIdent(String name, int index, AtomType type, boolean temp) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.low_type = type;
		ident.temp = temp;
		ident.id_type = IdType.var;
		return ident;
	}
	
	public static Identifier createParamIdent(String name, int index, AtomType type) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.low_type = type;
		ident.id_type = IdType.param;
		return ident;
	}
	
	public Identifier clone() {
		Identifier a = new Identifier();
		a.name = name;
		a.index = index;
		a.id_type = id_type;
		a.low_type = low_type;
		a.high_type = high_type;
		a.temp = temp;
		a.func = func;
		return a;
	}
	
	@Override
	public String toString() {
		return name + ":" + atomType();
	}
}
