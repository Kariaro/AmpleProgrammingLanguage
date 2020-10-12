package hardcoded.compiler.instruction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.expression.LowType;

public final class IRSerializer {
	private OutputStream out;
	private InputStream in;
	
	private IRSerializer() {
		
	}
	
	private void writeByte(int value) throws IOException {
		out.write(value & 0xff);
	}
	
	private void writeInt(int value) throws IOException {
		out.write(value & 0xff);
		out.write((value >> 8) & 0xff);
		out.write((value >> 16) & 0xff);
		out.write(value >>> 24);
	}
	
	private void writeVarInt(int value) throws IOException {
		do {
			int temp = (value & 0b01111111);
			value >>>= 7;
			if(value != 0) {
				temp |= 0b10000000;
			}
			
			out.write(temp);
		} while(value != 0);
	}
	
	private void writeLong(long value) throws IOException {
		writeInt((int)(value >>> 32L));
		writeInt((int)(value & 0xffffffffL));
	}
	
	private void writeLowType(LowType type) throws IOException {
		out.write(type.type().ordinal());
		out.write(type.depth());
	}
	
	private void writeString(String string) throws IOException {
		if(string == null) {
			out.write(0);
			return;
		}
		
		byte[] bytes = string.getBytes(StandardCharsets.ISO_8859_1);
		writeVarInt(bytes.length);
		out.write(bytes);
	}
	
	private void writeContext(IRContext context) throws IOException {
		writeVarInt(context.strings.size());
		for(String s : context.strings)
			writeString(s);
		
		writeString(context.programName);
		writeLong(context.creationDate);
	}
	
	private void writeInstruction(IRInstruction inst) throws IOException {
		// TODO: Implement IRSerializer.writeInstruction
		writeByte(inst.op.ordinal());
	}
	
	private void writeFunction(IRFunction func) throws IOException {
		LowType[] params = func.getParams();
		writeByte(params.length);
		for(LowType type : params)
			writeLowType(type);
		
		writeLowType(func.getType());
		writeString(func.getName());
		
		IRInstruction[] array = func.getInstructions();
		writeVarInt(array.length);
		for(IRInstruction inst : array)
			writeInstruction(inst);
	}
	
	private void writeProgram(IRProgram program) throws IOException  {
		writeContext(program.getContext());
		writeVarInt(program.list.size());
		for(IRFunction func : program.list)
			writeFunction(func);
	}
	
	public static void write(IRProgram program, OutputStream stream) throws IOException {
		IRSerializer serial = new IRSerializer();
		serial.out = stream;
		serial.writeProgram(program);
	}
	
	// ============================================ //
	
	private LowType readLowType() throws IOException {
		Atom type = Atom.values()[in.read()];
		return LowType.create(type, in.read());
	}
	
	private int readUByte() throws IOException {
		return in.read();
	}
	
//	private int readInt() throws IOException {
//		return in.read()
//			| (in.read() << 8)
//			| (in.read() << 16)
//			| (in.read() << 24);
//	}
	
	private long readLong() throws IOException {
		return (long)in.read()
			| (in.read() << 8L)
			| (in.read() << 16L)
			| (in.read() << 24L)
			| (in.read() << 32L)
			| (in.read() << 40L)
			| (in.read() << 48L)
			| (in.read() << 56L);
	}
	
	private String readString() throws IOException {
		int len = readVarInt();
		if(len == -1) return null;
		
		byte[] bytes = new byte[len];
		in.read(bytes); // Hopefully this wont fail?
		
		return new String(bytes, StandardCharsets.ISO_8859_1);
	}
	
	private int readVarInt() throws IOException {
		int numRead = 0;
		int result = 0;
		int read;
		
		do {
			read = in.read();
			int value = (read & 0b01111111);
			result |= (value << (7 * numRead));
			
			numRead++;
			if(numRead > 5) {
				throw new RuntimeException("VarInt is too big");
			}
		} while((read & 0b10000000) != 0);
		
		return result;
	}
	
	private IRContext readContext() throws IOException {
		IRContext context = new IRContext();
		
		int len = readVarInt();
		for(int i = 0; i < len; i++)
			context.strings.add(readString());
		
		context.programName = readString();
		context.creationDate = readLong();
		return context;
	}
	
	private IRInstruction readInstruction() throws IOException {
		// TODO: Implement IRSerializer.readInstruction
		IRType op = IRType.values()[readUByte()];
		
		return null;
	}
	
	private IRFunction readFunction() throws IOException {
		LowType[] params = new LowType[readUByte()];
		for(int i = 0; i < params.length; i++)
			params[i] = readLowType();
		
		LowType type = readLowType();
		String name = readString();
		
		IRFunction func = new IRFunction(type, name, params);
		
		int len = readVarInt();
		for(int i = 0; i < len; i++)
			func.list.add(readInstruction());
		
		return func;
	}
	
	private IRProgram readProgram() throws IOException {
		IRContext context = readContext();
		
		int len = readVarInt();
		List<IRFunction> list = new ArrayList<>();
		for(int i = 0; i < len; i++)
			list.add(readFunction());
		
		return new IRProgram(context, list);
	}
	
	public static IRProgram read(InputStream stream) throws IOException {
		IRSerializer serial = new IRSerializer();
		serial.in = stream;
		return serial.readProgram();
	}
	
	public static String deepPrint(String name, Object obj, int depth) throws Exception {
		if(obj == null || depth < 1) return name + ": " + Objects.toString(obj, "null");
		
		Class<?> clazz = obj.getClass();
		String ty = name + ": " + clazz.getSimpleName() + " ";
		
		// clazz.isPrimitive()
		if(clazz.isEnum() || clazz == Boolean.class
		|| clazz == AtomicInteger.class) {
			return ty + "(" + obj.toString() + ")";
		}
		if(clazz == Long.class) return name + ": Long (" + ((Long)obj).longValue() + ")";
		if(clazz == String.class) return ty + "(\"" + obj.toString() + "\")";
		if(clazz == LowType.class) return name + ": LowType (" + ((LowType)obj).type() + ", " + ((LowType)obj).depth() + ")";
		if(Number.class.isAssignableFrom(clazz)) return ty + "(" + obj.toString() + ")";
		
		if(List.class.isAssignableFrom(clazz)) {
			Collection<?> list = (Collection<?>)obj;
			StringBuilder sb = new StringBuilder();
			sb.append(clazz.getSimpleName()).append(" ").append(name).append(":\n");
			
			Object[] array = list.toArray();
			for(int i = 0; i < array.length; i++) {
				Object value = array[i];
				String string = deepPrint(Integer.toString(i), value, depth - 1);
				sb.append("\t+ ");
				if(string.indexOf('\n') != -1) {
					sb.append(string.trim().replace("\n", "\n\t| "));
				} else {
					sb.append(string);
				}
				sb.append("\n");
			}
			
			return sb.toString();
		}
		
		if(clazz.isArray()) {
			StringBuilder sb = new StringBuilder();
			sb.append(name).append(":\n");
			
			int len = Array.getLength(obj);
			for(int i = 0; i < len; i++) {
				Object value = Array.get(obj, i);
				String string = deepPrint(Integer.toString(i), value, depth - 1);
				sb.append("\t+ ");
				if(string.indexOf('\n') != -1) {
					sb.append(string.trim().replace("\n", "\n\t| "));
				} else {
					sb.append(string);
				}
				sb.append("\n");
			}
			
			return sb.toString();
		}
		
		{
			Field[] fields = clazz.getDeclaredFields();
			StringBuilder sb = new StringBuilder();
			sb.append(name).append(":\n");
			
			for(Field field : fields) {
				if(Modifier.isStatic(field.getModifiers())) continue;
				boolean acc = field.isAccessible();
				
				field.setAccessible(true);
				Object value = field.get(obj);
				field.setAccessible(acc);
				
				String string = deepPrint(field.getName(), value, depth - 1);
				sb.append("\t+ ");
				if(string.indexOf('\n') != -1) {
					sb.append(string.trim().replace("\n", "\n\t| "));
				} else {
					sb.append(string);
				}
				sb.append("\n");
			}
			
			return sb.toString();
		}
	}
}
