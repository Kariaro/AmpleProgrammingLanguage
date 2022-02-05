package me.hardcoded.compiler.parser.type;

public class Reference {
	public static final int IMPORT = 1 << 5;
	
	public static final int VARIABLE = 0,
							LABEL = 1,
							FUNCTION = 2;
	
	private int id;
	private String name;
	private int flags;
	private int usages;
	
	public Reference(String name, int id, int flags) {
		this.name = name;
		this.id = id;
		this.flags = flags;
	}
	
	public String getName() {
		return name;
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
		
		return type + "_" + id + (isImported() ? "_import" : "");
	}
	
	@Override
	public String toString() {
		if (id < 0) {
			return name;
		}
		
		return name + ":" + toSimpleString();
	}
}
