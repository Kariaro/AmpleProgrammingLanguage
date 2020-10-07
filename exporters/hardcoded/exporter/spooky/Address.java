package hardcoded.exporter.spooky;

class Address {
	int baseAddr;
	int offset;
	private Address(int baseAddr, int offset) {
		this.baseAddr = baseAddr;
		this.offset = offset;
	}
	
	public String toString() {
		return "Address{" + baseAddr + ":" + offset + "}";
	}
	
	static Address create(int baseAddr, int offset) {
		return new Address(baseAddr, offset);
	}
}
