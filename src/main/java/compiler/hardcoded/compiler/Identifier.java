package hardcoded.compiler;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.types.HighType;

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
	private boolean isGenerated;
	
	private Function function;
	private LowType lowType;

	@Deprecated
	private HighType highType;
	
	public Function func() {
		return function;
	}
	
	public String name() {
		return name;
	}
	
	public IdType id_type() {
		return id_type;
	}
	
	public HighType getHighType() {
		return highType;
	}
	
	public LowType getLowType() {
		if(lowType != null) return lowType;
		return highType == null ? null:highType.type();
	}
	
	public int index() {
		return index;
	}
	
	public boolean isGenerated() {
		return isGenerated;
	}
	
	public boolean hasType() {
		return id_type == IdType.var || id_type == IdType.param || id_type == IdType.funct;
	}
	
	public static Identifier createFuncIdent(String name, int index, Function func) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.function = func;
		ident.id_type = IdType.funct;
		ident.highType = func.returnType;
		ident.lowType = ident.highType.type();
		return ident;
	}
	
	public static Identifier createVarIdent(String name, int index, LowType type) { return createVarIdent(name, index, type, false); }
	public static Identifier createVarIdent(String name, int index, LowType type, boolean temp) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.lowType = type;
		ident.isGenerated = temp;
		ident.id_type = IdType.var;
		return ident;
	}
	
	public static Identifier createParamIdent(String name, int index, HighType type) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.highType = type;
		ident.lowType = type.type();
		ident.id_type = IdType.param;
		return ident;
	}
	
	public Identifier clone() {
		Identifier a = new Identifier();
		a.name = name;
		a.index = index;
		a.id_type = id_type;
		a.lowType = lowType;
		a.highType = highType;
		a.isGenerated = isGenerated;
		a.function = function;
		return a;
	}
	
	@Override
	public String toString() {
		return name + ":" + getLowType();
	}
}
