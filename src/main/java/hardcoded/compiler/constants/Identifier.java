package hardcoded.compiler.constants;

import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.statement.Function;
import hardcoded.compiler.types.HighType;

public class Identifier {
	public enum IdType {
		clazz,
		funct,
		param,
		var
	}
	
	private final IdType id;
	private final String name;
	private final int index;
	private boolean generated;
	private Function function;
	private LowType lowType;
	private HighType highType;
	
	public Identifier(IdType id, String name, int index) {
		this.id = id;
		this.name = name;
		this.index = index;
	}
	
	public Function getFunction() {
		return function;
	}
	
	public String getName() {
		return name;
	}
	
	public IdType getIdType() {
		return id;
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
		return generated;
	}
	
	public boolean hasType() {
		return id == IdType.var || id == IdType.param || id == IdType.funct;
	}
	
	// Functions
	public static Identifier createFuncIdent(String name, int index, Function func) {
		Identifier ident = new Identifier(IdType.funct, name, index);
		ident.function = func;
		ident.highType = func.getReturnType();
		return ident;
	}
	
	// Parameters
	public static Identifier createParamIdent(String name, int index, HighType type) {
		Identifier ident = new Identifier(IdType.param, name, index);
		ident.highType = type;
		return ident;
	}
	
	// Variables
	public static Identifier createLocalVariable(String name, int index, HighType type) {
		Identifier ident = new Identifier(IdType.var, name, index);
		ident.highType = type;
		ident.generated = false;
		return ident;
	}
	
	public static Identifier createTempLocalVariable(String name, int index, LowType type) {
		Identifier ident = new Identifier(IdType.var, name, index);
		ident.lowType = type;
		ident.generated = true;
		return ident;
	}

	@Override
	public Identifier clone() {
		Identifier a = new Identifier(id, name, index);
		a.lowType = lowType;
		a.highType = highType;
		a.generated = generated;
		a.function = function;
		return a;
	}

	@Override
	public String toString() {
		return name;
	}
}
