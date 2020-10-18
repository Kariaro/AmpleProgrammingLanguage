package hardcoded.compiler.instruction;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.instruction.IRInstruction.*;
import hardcoded.utils.StringUtils;

/**
 * TODO: Generated strings should be put into categories like
 *<PRE>
 *Each compiler made label should be written with this pattern
 *
 *First byte:
 *....    | Type   if, for, while, switch, cor, cand
 *    ....| Id     specific id that tells what type of label it is.
 *=========
 *
 *Second byte:
 *VarInt index.
 *
 *Each user made label is written with this pattern
 *
 *1111    | Type
 *    ....| Reserved
 *=========
 *VarInt string_index
 *
 *</PRE>
 * 
 */
public final class IRSerializer {
	private static final int MAGIC = 0x52494C48; // 'HLIR'
	
	private List<String> strings = new ArrayList<>();
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
	
	private void writeBytes(byte[] array) throws IOException {
		out.write(array);
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
	
	private void writeVarLong(long value) throws IOException {
		do {
			long temp = (value & 0b01111111);
			value >>>= 7;
			if(value != 0) {
				temp |= 0b10000000;
			}
			
			out.write((int)temp);
		} while(value != 0);
	}
	
	private void writeLong(long value) throws IOException {
		writeInt((int)(value & 0xffffffffL));
		writeInt((int)(value >>> 32L));
	}
	
	private void writeLowType(LowType type) throws IOException {
		writeByte(Converter.getLowType(type));
	}
	
	private void writeString(String string) throws IOException {
		if(string == null) {
			out.write(0);
			return;
		}
		
		int index = strings.indexOf(string);
		if(index < 0) {
			index = strings.size();
			strings.add(string);
		}
		
		writeVarInt(index + 1);
	}
	
	private void writeContext(IRContext context) throws IOException {
		writeVarInt(context.strings.size());
		for(String s : context.strings)
			writeString(s);
		
		writeString(context.programName);
		writeLong(context.creationDate);
	}
	
	private void writeLabelParam(LabelParam param) throws IOException {
		if(param.isTemporary()) {
			int[] parts = IO.getBytesFromLabelName(param.getName());
			writeByte(parts[0]);
			writeVarInt(parts[1]);
		} else {
			writeString(param.getName());
		}
	}
	
	private void writeParam(Param param) throws IOException {
		if(param == IRInstruction.NONE) {
			// TODO: Only allowed in call PARAMS
			writeByte(0);
		} else if(param instanceof Reg) {
			writeByte(1);
			writeLowType(param.getSize());
			writeVarInt(param.getIndex());
			writeString(param.getName());
		} else if(param instanceof RefReg) {
			writeByte(2);
			writeVarInt(param.getIndex());
			writeString(param.getName());
		} else if(param instanceof NumberReg) {
			writeByte(3);
			writeLowType(param.getSize());
			writeVarLong(((NumberReg)param).getValue());
		} else if(param instanceof DataParam) {
			writeByte(4);
			writeVarInt(param.getIndex());
			writeString("" + ((DataParam)param).getValue());
		} else if(param instanceof FunctionLabel) {
			writeByte(5);
			writeLowType(param.getSize());
			writeString(param.getName());
		} else if(param instanceof LabelParam) {
			writeByte(6);
			writeLabelParam((LabelParam)param);
		} else if(param instanceof DebugParam) {
			writeByte(7);
			writeString("" + ((DebugParam)param).getValue());
		} else {
			throw new NullPointerException("Invalid parameter '" + param + "'");
		}
	}
	
	private void writeInstruction(IRInstruction inst) throws IOException {
		writeByte(inst.op.ordinal());
		
		switch(inst.op) {
			case br:
			case label: {
				writeLabelParam((LabelParam)inst.getParam(0));
				return;
			}
			default:
		}
		
		if(inst.op.args < 0)
			writeByte(inst.getNumParams());
		for(Param param : inst.getParams())
			writeParam(param);
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
	
	private static byte[] processWrite(IRSerializer serial, IRProgram program) throws IOException {
		byte[] code;
		{
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			serial.out = bs;
			serial.writeProgram(program);
			code = bs.toByteArray();
		}
		
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		serial.out = bs;
		
		serial.writeVarInt(serial.strings.size());
		
		for(String str : serial.strings) {
			byte[] array = str.getBytes(StandardCharsets.ISO_8859_1);
			serial.writeVarInt(array.length);
			serial.writeBytes(array);
		}
		
		serial.writeBytes(code);
		return bs.toByteArray();
	}
	
	public static void write(IRProgram program, OutputStream out) throws IOException {
		IRSerializer serial = new IRSerializer();
		byte[] buffer = processWrite(serial, program);
		// buffer = encodeGzip(buffer);
		
		serial.out = out;
		serial.writeInt(MAGIC);
		serial.writeBytes(buffer);
	}
	
	private static byte[] encodeGzip(byte[] input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		GZIPOutputStream stream = new GZIPOutputStream(output);
		stream.write(input);
		stream.close();
		return output.toByteArray();
	}
	
	private static ByteArrayInputStream decodeGzip(InputStream stream) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		GZIPInputStream gzip = new GZIPInputStream(stream);
		
		byte[] buffer = new byte[4096];
		int readBytes = 0;
		while((readBytes = gzip.read(buffer)) != -1) {
			bs.write(buffer, 0, readBytes);
		}
		
		return new ByteArrayInputStream(bs.toByteArray());
	}
	
	// ================================================================================================ //
	// ================================================================================================ //
	
	private LowType readLowType() throws IOException {
		return Converter.getLowType(readUByte());
	}
	
	private int readUByte() throws IOException {
		return in.read();
	}
	
	private int readInt() throws IOException {
		return in.read()
			| (in.read() << 8)
			| (in.read() << 16)
			| (in.read() << 24);
	}
	
	private long readLong() throws IOException {
		return((long)readInt() <<  0L)
			| ((long)readInt() << 32L);
	}
	
	private byte[] readBytes(int length) throws IOException {
		byte[] array = new byte[length];
		in.read(array);
		return array;
	}
	
	private String readString() throws IOException {
		int index = readVarInt();
		if(index == 0) return null;
		return strings.get(index - 1);
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
	
	private long readVarLong() throws IOException {
		int numRead = 0;
		long result = 0;
		int read;
		
		do {
			read = in.read();
			long value = (read & 0b01111111);
			result |= (value << (7 * numRead));
			
			numRead++;
			if(numRead > 10) {
				throw new RuntimeException("VarLong is too big");
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
	
	private LabelParam readLabelParam() throws IOException {
		int head = readUByte();
		int type = head >> 4;
		
		if(type == 0xf) {
			// USER LABEL
			return new LabelParam(readString(), false, null);
		} else {
			String name = "_" + IO.getStringFromType(head)
						+ "_" + readVarInt();
			
			return new LabelParam(name, true, null);
		}
	}
	
	private Param readParam() throws IOException {
		int type = readUByte();
		
		switch(type) {
			case 0: return IRInstruction.NONE;
			case 1: {
				LowType size = readLowType();
				int index = readVarInt();
				String name = readString();
				return new Reg(name, size, index);
			}
			case 2: {
				int index = readVarInt();
				String name = readString();
				return new RefReg(name, index);
			}
			case 3: {
				LowType size = readLowType();
				long value = readVarLong();
				return new NumberReg(value, size);
			}
			case 4: {
				int index = readVarInt();
				String str = readString();
				return new DataParam(str, index);
			}
			case 5: {
				LowType size = readLowType();
				String name = readString();
				return new FunctionLabel(name, size);
			}
			case 6: {
				return readLabelParam();
			}
			case 7: return new DebugParam(readString());
			default: {
				throw new NullPointerException("Invalid parameter.");
			}
		}
	}
	
	private IRInstruction readInstruction() throws IOException {
		IRType op = IRType.values()[readUByte()];
		
		// Quick
		switch(op) {
			case br:
			case label: {
				IRInstruction inst = new IRInstruction(op);
				inst.params.add(readLabelParam());
				return inst;
			}
			default:
		}
		
		int num_params = op.args;
		if(num_params < 0) {
			num_params = readVarInt();
		}
		
		IRInstruction inst = new IRInstruction(op);
		for(int i = 0; i < num_params; i++)
			inst.params.add(readParam());
		
		return inst;
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
	
	private static IRProgram processRead(IRSerializer serial, InputStream stream) throws IOException {
		serial.in = stream; //decodeGzip(stream);
		
		int num_strings = serial.readVarInt();
		for(int i = 0; i < num_strings; i++) {
			byte[] array = serial.readBytes(serial.readVarInt());
			serial.strings.add(new String(array, StandardCharsets.ISO_8859_1));
		}
		
		return serial.readProgram();
	}
	
	public static IRProgram read(InputStream stream) throws IOException {
		IRSerializer serial = new IRSerializer();
		serial.in = stream;
		
		int magic = serial.readInt();
		if(magic != MAGIC) {
			throw new IOException("File magic was wrong. Expected 'HCIR' but got '" + (
				(char)(magic & 0xff) + "" +
				(char)((magic >> 8) & 0xff) + "" +
				(char)((magic >> 16) & 0xff) + "" +
				(char)((magic >> 24) & 0xff)
			) + "'");
		}
		
		return processRead(serial, stream);
	}
	
	// ================================================================================================ //
	// ================================================================================================ //
	
	public static String deepPrint(String name, Object obj, int depth) throws Exception {
		if(obj == null || depth < 1) return name + ": " + Objects.toString(obj, "null");
		
		Class<?> clazz = obj.getClass();
		String ty = name + ": " + clazz.getSimpleName() + " ";
		
		// clazz.isPrimitive()
		if(clazz.isEnum() || clazz == Boolean.class || clazz == AtomicInteger.class) return ty + "(" + obj.toString() + ")";
		if(clazz == String.class) return ty + "(\"" + StringUtils.escapeString(obj.toString()) + "\")";
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
				if(string.indexOf('\n') != -1) sb.append(string.trim().replace("\n", "\n\t| "));
				else sb.append(string);
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
				if(string.indexOf('\n') != -1) sb.append(string.trim().replace("\n", "\n\t| "));
				else sb.append(string);
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
				if(string.indexOf('\n') != -1) sb.append(string.trim().replace("\n", "\n\t| "));
				else sb.append(string);
				sb.append("\n");
			}
			
			return sb.toString();
		}
	}
}

class Converter {
	static final int IF_TYPE		= 0x0;
	static final int FOR_TYPE		= 0x1;
	static final int WHILE_TYPE		= 0x2;
	static final int SWITCH_TYPE	= 0x3;
	static final int COR_TYPE		= 0x4;
	static final int CAND_TYPE		= 0x5;
	// RESERVED
	static final int USER_TYPE		= 0xf;
	
	static int[] getByteFromLabelName(String name) {
		// Remove dash
		name = name.substring(1);
		
		// [ type, number ]
		String[] parts = name.split("_");
		
		String part = parts[0];
		int index = Integer.valueOf(parts[1]);
		int header = -1;
		
		switch(part) {
			case "if.end":		header = (IF_TYPE << 4) | 0; break;
			case "if.else":		header = (IF_TYPE << 4) | 1; break;
			
			case "for.next":	header = (FOR_TYPE << 4) | 0; break;
			case "for.loop":	header = (FOR_TYPE << 4) | 1; break;
			case "for.end":		header = (FOR_TYPE << 4) | 2; break;
			
			case "while.next":	header = (WHILE_TYPE << 4) | 0; break;
			case "while.loop":	header = (WHILE_TYPE << 4) | 1; break;
			case "while.end":	header = (WHILE_TYPE << 4) | 2; break;
			
			// switch
			
			case "cor.end":		header = (COR_TYPE << 4) | 0; break;
			case "cand.end":	header = (CAND_TYPE << 4) | 0; break;
		}
		
		if(header < 0) {
			throw new IllegalArgumentException("The compiler label '" + part + "' is undefined");
		}
		
		return new int[] { header, index };
	}
	
	static String getLabelTypeName(int type) {
		switch(type) {
			case IF_TYPE: return "if";
			case FOR_TYPE: return "for";
			case WHILE_TYPE: return "while";
			case SWITCH_TYPE: return "switch";
			case COR_TYPE: return "cor";
			case CAND_TYPE: return "cand";
			
			// Reserved
			case USER_TYPE: return "";
		}
		
		throw new IllegalArgumentException("The type '" + type + "' is undefined");
	}
	
	static String getTypeString(int type, int id) {
		String typeName = getLabelTypeName(type);
		switch(type) {
			case IF_TYPE: {
				switch(id) {
					case 0: return typeName + ".end";
					case 1: return typeName + ".else";
				}
				break;
			}
			case WHILE_TYPE:
			case FOR_TYPE: {
				switch(id) {
					case 0: return typeName + ".next";
					case 1: return typeName + ".loop";
					case 2: return typeName + ".end";
				}
				break;
			}
			case SWITCH_TYPE: {
				// Not implemented yet
				break;
			}
			
			case CAND_TYPE:
			case COR_TYPE: {
				if(id == 0) return typeName + ".end";
				break;
			}
		}
		
		throw new IllegalArgumentException("The type '" + getLabelTypeName(type) + "' does not have the id '" + id + "'");
	}
	
	static byte getLowType(LowType type) {
		// ..          : type   I, U, F, <object>
		//    .        : is_pointer
		//      ...    : size   0, 8, 16, 32, 64, 128, 256, 512
		//          .. : reserved
		
		int serial = 0;
		
		if(type.isNumber()) {
			if(type.isFloating()) {
				serial = 0b10_000000;
			} else if(!type.isSigned()) {
				serial = 0b01_000000;
			}
		} else {
			serial = 0b11_000000;
		}
		
		if(type.isPointer()) {
			serial |= 0b1_00000;
		}
		
		{
			switch(type.size()) {
				case 0:  serial |= 0b000_00; break;
				case 1:  serial |= 0b001_00; break;
				case 2:  serial |= 0b010_00; break;
				case 4:  serial |= 0b011_00; break;
				case 8:  serial |= 0b100_00; break;
				case 16: serial |= 0b101_00; break;
				case 32: serial |= 0b110_00; break;
				case 64: serial |= 0b111_00; break;
			}
			
			// Last two bytes are reserved
		}
		
		return (byte)serial;
	}
	
	static LowType getLowType(int field) {
		// ..          : type   I, U, F, <object>
		//    .        : is_pointer
		//      ...    : size   0, 8, 16, 32, 64, 128, 256, 512
		//          .. : reserved
		
		int type_field = (field & 0b11_000000) >>> 6;
		int size_field = (field & 0b111_00) >>> 2;
		int size = ((int)Math.pow(2, size_field)) / 2;
		boolean isPointer = (field & 0b1_00000) != 0;
		
		if(type_field == 3) {
			// Custom one..
			return LowType.create(Atom.unf, isPointer ? 1:0);
		}
		
		boolean isFloating = (type_field == 2);
		boolean isUnsigned = (type_field == 1);
		
		return LowType.create(
			Atom.get(size, !isUnsigned, isFloating),
			isPointer ? 1:0
		);
	}
}

class IO {
	private static Map<String, Integer> map;
	private static final int IF_LABEL		= 0x0 << 4,
							 FOR_LABEL		= 0x1 << 4,
							 WHILE_LABEL	= 0x2 << 4,
							 SWITCH_LABEL	= 0x3 << 4,
							 COR_LABEL		= 0x4 << 4,
							 CAND_LABEL		= 0x5 << 4,
							 /* Reserved */
							 USER_LABEL		= 0xf << 4;
	
	static {
		Map<String, Integer> m = new HashMap<>();
		map = Collections.unmodifiableMap(m);
		m.put("if.end", IF_LABEL | 0);
		m.put("if.else", IF_LABEL | 1);
		
		m.put("for.next", FOR_LABEL | 0);
		m.put("for.loop", FOR_LABEL | 1);
		m.put("for.end", FOR_LABEL | 2);
		
		m.put("while.next", WHILE_LABEL | 0);
		m.put("while.loop", WHILE_LABEL | 1);
		m.put("while.end", WHILE_LABEL | 2);
		
		// Switch_label
		
		m.put("cor.end", COR_LABEL | 0);
		m.put("cand.end", CAND_LABEL | 0);
	}
	
	static int[] getBytesFromLabelName(String name) {
		// Remove dash
		name = name.substring(1);
		
		// [ type, number ]
		String[] parts = name.split("_");
		
		return new int[] {
			map.get(parts[0]),
			Integer.valueOf(parts[1])
		};
	}
	
	static String getStringFromType(int read) {
		for(String key : map.keySet()) {
			if(map.get(key) == read) return key;
		}
		
		throw new NullPointerException("Could not find label '" + read + "' /");
	}
}