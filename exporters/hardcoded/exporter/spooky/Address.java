package hardcoded.exporter.spooky;

class Address {
	int baseAddr;
	int offset;
	private Address(int baseAddr, int offset) {
		this.baseAddr = baseAddr;
		this.offset = offset;
	}
	
	public String toString() {
		if(baseAddr == -1) {
			if(offset >=  0) return "[" + offset + "]";
			if(offset == -1) return "0";
			if(offset == -2) return "-1";
			return "Data[" + (offset) + "]";
		}
		
		if(baseAddr == 0) {
			return "[SP " + (offset < 0 ? ("- " + (-offset)):("+ " + offset)) + "]";
		}
		return "Address[" + baseAddr + ":" + offset + "]";
	}
	
	static Address create(int baseAddr, int offset) {
		return new Address(baseAddr, offset);
	}
	
	static Address stack(int offset) {
		return new Address(0, offset);
	}
	
	static Address funcStack(int offset) {
		return new Address(0, offset);
	}
	
	static Address data(int offset) {
		return new Address(-1, -(offset + 1));
	}
	
	static Address global(int offset) {
		return new Address(-1, offset);
	}
	
	static Address functionPointer(int id) {
		return new Address(-1, -(id + 4));
	}
}
