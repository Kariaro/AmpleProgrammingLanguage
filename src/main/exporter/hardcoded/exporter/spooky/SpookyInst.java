package hardcoded.exporter.spooky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SpookyInst {
	OpCode op;
	List<Object> params = new ArrayList<>();
	
	SpookyInst(OpCode op) {
		this.op = op;
	}
	
	SpookyInst(OpCode op, Object... array) {
		this.op = op;
		params.addAll(Arrays.asList(array));
	}
	
	int size() {
		if(op == OpCode.DEBUG) {
			return 0;
		}
		
		int size = 0;
		for(Object obj : params) {
			if(obj instanceof Address) {
				size += 8;
			} else if(obj instanceof Number) {
				size += 4;
			} else {
				return -1;
			}
		}
		
		return size;
	}
	
	byte[] compile() {
		if(op == OpCode.DEBUG) return new byte[0];
		ByteOutputWriter writer = new ByteOutputWriter();
		writer.write(op);
		
		for(Object obj : params) {
			if(obj instanceof Address) {
				writer.write((Address)obj);
			} else if(obj instanceof Number) {
				writer.write(((Number)obj).intValue());
			} else if(obj instanceof String) {
				writer.writeString((String)obj);
			} else throw new IllegalArgumentException("Invalid parameter inside SpookyInst '" + params + "'");
		}
		
		return writer.toByteArray();
	}
	
	
	@Override
	public String toString() {
		if(params.isEmpty()) return op + "";
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%-10s", op));
		
		for(Object obj : params) {
			sb.append(String.format("%-12s", obj));
		}
		
		sb.deleteCharAt(sb.length() - 2);
		return sb.toString().trim();
	}
}
