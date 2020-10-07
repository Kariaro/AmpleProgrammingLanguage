package hardcoded.exporter.spooky;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hardcoded.compiler.instruction.IRInstruction.Param;
import hardcoded.exporter.spooky.SpookyCodeGenerator.Relative;
import hardcoded.utils.StringUtils;

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
		if(op == OpCode.DEBUG)
			return 0;
		
		int size = 0;
		for(Object obj : params) {
			if(obj instanceof Address) {
				size += 8;
			} else if(obj instanceof Number) {
				size += 4;
			} else return -1;
		}
		
		return size;
	}
	
	int calculate_size() {
		if(op == OpCode.DEBUG)
			return 0;
		
		int size = 1;
		for(Object obj : params) {
			if(obj instanceof Address) {
				size += 8;
			} else if(obj instanceof Number) {
				size += 4;
			} else if(obj instanceof String) {
				String str = (String)obj;
				byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
				size += (bytes.length) + 1;
			} else if(obj instanceof Param) {
				if(op == OpCode.JMP) {
					size += 4; // Number
				} else {
					size += 8; // Address
				}
			} else if(obj instanceof Relative) {
				size += 4;
			}
		}
		
		return size;
	}
	
	byte[] compile() {
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
		return op + " " + StringUtils.join(", ", params);
	}
}
