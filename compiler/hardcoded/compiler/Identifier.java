package hardcoded.compiler;

import hardcoded.compiler.Block.Function;
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
	public Type type;
	
	public Function func() {
		return func;
	}
	
	public String name() {
		return name;
	}
	
	public IdType type() {
		return id_type;
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
		ident.type = func.returnType;
		return ident;
	}
	
	public static Identifier createVarIdent(String name, int index, Type type) { return createVarIdent(name, index, type, false); }
	public static Identifier createVarIdent(String name, int index, Type type, boolean temp) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.type = type;
		ident.temp = temp;
		ident.id_type = IdType.var;
		return ident;
	}
	
	public static Identifier createParamIdent(String name, int index, Type type) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.type = type;
		ident.id_type = IdType.param;
		return ident;
	}
	
	@Override
	public String toString() {
		return name; // + ":" + type;
	}
}
