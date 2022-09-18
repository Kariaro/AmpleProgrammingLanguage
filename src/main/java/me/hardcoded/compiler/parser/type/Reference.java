package me.hardcoded.compiler.parser.type;

import java.util.Objects;

public class Reference {
	public static final int IMPORT = 1 << 5,
		EXPORT = 1 << 6;
	
	public static final int VARIABLE = 0,
		LABEL = 1,
		FUNCTION = 2,
		NAMESPACE = 3;
	
	public static final int MODIFIERS = IMPORT | EXPORT;
	
	private final int id;
	private final String name;
	private final Namespace namespace;
	private ValueType valueType;
	private int flags;
	private int usages;
	private String mangledName;
	
	public Reference(String name, Namespace namespace, ValueType valueType, int id, int flags) {
		this(name, namespace, valueType, id, flags, 0);
	}
	
	public Reference(String name, Namespace namespace, ValueType valueType, int id, int flags, int usages) {
		this.name = name;
		this.namespace = Objects.requireNonNull(namespace);
		this.id = id;
		this.valueType = Objects.requireNonNull(valueType);
		this.flags = flags;
		this.usages = usages;
	}
	
	public String getName() {
		return name;
	}
	
	public String getMangledName() {
		return mangledName;
	}
	
	public Namespace getNamespace() {
		return namespace;
	}
	
	public ValueType getValueType() {
		return valueType;
	}
	
	public int getUsages() {
		return usages;
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return flags & 0x1f;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public boolean isVariable() {
		return getType() == VARIABLE;
	}
	
	public boolean isFunction() {
		return getType() == FUNCTION;
	}
	
	public boolean isLabel() {
		return getType() == LABEL;
	}
	
	public boolean isImported() {
		return (flags & IMPORT) != 0;
	}
	
	public boolean isExported() {
		return (flags & EXPORT) != 0;
	}
	
	public void setExported(boolean enable) {
		flags = (flags & ~EXPORT) | (enable ? EXPORT : 0);
	}
	
	public void setImported(boolean enable) {
		flags = (flags & ~IMPORT) | (enable ? IMPORT : 0);
	}
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public void setModifiers(int modifiers) {
		this.flags = (flags & ~MODIFIERS) | (modifiers & MODIFIERS);
	}
	
	public void setType(int type) {
		flags = (flags & (~0x1f)) | type;
	}
	
	public void setMangledName(String mangledName) {
		this.mangledName = mangledName;
	}
	
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	
	public void incUsages() {
		usages++;
	}
	
	public void decUsages() {
		usages--;
	}
	
	public String toSimpleString() {
		String type = switch (flags & 0x1f) {
			case VARIABLE -> "var";
			case LABEL -> "lab";
			case FUNCTION -> "fun";
			case NAMESPACE -> "ns";
			default -> "unk";
		};
		
		return type + "_" + id + (isExported() ? "_export" : "") + (isImported() ? "_import" : "");
	}
	
	/**
	 * @return the namespace and name combined
	 */
	public String getPath() {
		if (namespace.isRoot()) {
			return name;
		}
		
		return namespace.getPath() + "::" + name;
	}
	
	@Override
	public String toString() {
		if (id < 0) {
			return name;
		}
		
		String mangledPart = (mangledName != null ? " " + mangledName : "");
		return valueType + " " + getPath() + ":" + toSimpleString() + mangledPart;
	}
}
