package me.hardcoded.compiler.parser.type;

import me.hardcoded.utils.DebugUtils;

public class Reference {
	public static final int IMPORT = 1 << 5,
							EXPORT = 1 << 6;
	
	public static final int VARIABLE = 0,
							LABEL = 1,
							FUNCTION = 2;
	
	private final int id;
	private final String name;
	private ValueType valueType;
	private int flags;
	private int usages;
	
	public Reference(String name, ValueType valueType, int id, int flags, int usages) {
		this.name = name;
		this.id = id;
		this.valueType = valueType;
		this.flags = flags;
		this.usages = usages;
	}
	
	public Reference(String name, ValueType valueType, int id, int flags) {
		this(name, valueType, id, flags, 0);
	}
	
	public String getName() {
		return name;
	}
	
	public ValueType getValueType() {
		return valueType;
	}
	
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	
	public int getUsages() {
		return usages;
	}
	
	public void incUsages() {
		usages++;
	}
	
	public void decUsages() {
		usages--;
	}
	
	public int getId() {
		return id;
	}
	
	public void setType(int type) {
		flags = (flags & (~0x1f)) | type;
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
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public String toSimpleString() {
		String type = switch (flags & 0x1f) {
			case VARIABLE -> "var";
			case LABEL -> "lab";
			case FUNCTION -> "fun";
			default -> "unk";
		};
		
		return type + "_" + id + (isExported() ? "_export" : "") + (isImported() ? "_import" : "");
	}
	
	@Override
	public String toString() {
		if (id < 0) {
			return name;
		}
		
		if (DebugUtils.DEBUG_REFERENCE_INFORMATION) {
			if (valueType != null) {
				return valueType + " " + name + ":" + toSimpleString();
			}
			
			return name + ":" + toSimpleString();
		}
		
		return name;
	}
}
