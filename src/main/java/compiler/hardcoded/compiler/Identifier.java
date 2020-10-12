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
	private LowType low_type;

	@Deprecated
	private HighType high_type;
	
	public Function func() {
		return function;
	}
	
	public String name() {
		return name;
	}
	
	public IdType id_type() {
		return id_type;
	}
	
	@Deprecated
	public HighType high_type() {
		return high_type;
	}
	
	public LowType low_type() {
		if(low_type != null) return low_type;
		return high_type == null ? null:high_type.type();
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
		ident.high_type = func.returnType;
		ident.low_type = ident.high_type.type();
		return ident;
	}
	
	public static Identifier createVarIdent(String name, int index, LowType type) { return createVarIdent(name, index, type, false); }
	public static Identifier createVarIdent(String name, int index, LowType type, boolean temp) {
		Identifier ident = new Identifier();
		ident.name = name;
		ident.index = index;
		ident.low_type = type;
		ident.isGenerated = temp;
		ident.id_type = IdType.var;
		return ident;
	}
	
	public static Identifier createParamIdent(String name, int index, LowType type) {
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
		a.isGenerated = isGenerated;
		a.function = function;
		return a;
	}
	
	@Override
	public String toString() {
		return name + ":" + low_type();
	}
}
