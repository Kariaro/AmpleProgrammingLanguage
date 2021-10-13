package hardcoded.vm;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import hardcoded.CompilerMain;
import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.instruction.*;
import hardcoded.compiler.instruction.IRInstruction.*;
import hardcoded.compiler.numbers.Value;

public class AmpleVm {
	public static void run(IRProgram program) throws VmException { run(program, new StdAmpleBufferStream(), null); }
	public static void run(IRProgram program, AmpleBufferStream stream, AmpleBufferCallback callback) throws VmException {
		AmpleVm vm = new AmpleVm(program, stream, callback);
		vm.start();
		
		if(callback != null) {
			callback.bufferChanged(vm.buffer, AmpleBufferCallback.BUFFER_CLOSED);
		}
	}
	
	private final Memory memory = new Memory();
	private final Map<String, VmFunction> functions;
	private final Map<String, Integer> pointers;
	private final int consts_offset;
	private final VmFunction entry;
	
	private final AmpleBufferCallback callback;
	private final AmpleBufferStream buffer;
	
	private AmpleVm(IRProgram program, AmpleBufferStream stream, AmpleBufferCallback callback) {
		this.functions = new HashMap<>();
		this.pointers = new HashMap<>();
		this.callback = callback;
		this.buffer = stream;
		
		for(IRFunction func : program.getFunctions()) {
			functions.put(func.getName(), new VmFunction(func));
		}
		
		IRContext context = program.getContext();
		int offset = 0;
		int index = 0;
		for(String s : context.getStrings()) {
			pointers.put(".data.strings[" + (index++) + "]", offset);
			
			byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
			for(int i = 0; i < bytes.length; i++) {
				memory.write(offset + i, bytes[i]);
			}
			memory.write(offset + bytes.length, 0);
			
			offset += bytes.length + 1;
		}
		
		consts_offset = offset;
		entry = functions.get("main");
		if(entry == null)
			throw new VmException("Cound not find start function 'main'");
	}
	
	private void start() {
		run(entry, consts_offset, 0);
	}
	
	private void run(VmFunction func, int offset, int ip) {
		while(true) {
			if(ip >= func.getNumInstructions()) return;
			IRInstruction inst = func.getInstruction(ip++);
			
			
			switch(inst.type()) {
				case data:
				case nop:
				case label: break;
				
				case mov: {
					RegParam target = (RegParam)inst.getParam(0);
					Value v = read(func, offset, inst.getParam(1));
					write(func, offset, target, v);
					break;
				}
				
				case add:
				case sub:
				case div:
				case mul:
				case mod:
				case shl:
				case shr:
				case or:
				case and:
				case xor:
				case eq:
				case neq:
				case gt:
				case gte:
				case lt:
				case lte: {
					RegParam target = (RegParam)inst.getParam(0);
					
					Value a = read(func, offset, inst.getParam(1));
					Value b = read(func, offset, inst.getParam(2));
					
					switch(inst.type()) {
						case add: a = a.add(b); break;
						case sub: a = a.sub(b); break;
						case div: a = a.div(b); break;
						case mul: a = a.mul(b); break;
						case mod: a = a.mod(b); break;
						case shl: a = a.shl(b); break;
						case shr: a = a.shr(b); break;
						case or:  a = a.or(b); break;
						case and: a = a.and(b); break;
						case xor: a = a.xor(b); break;
						case eq:  a = a.eq(b); break;
						case neq: a = a.neq(b); break;
						case gt:  a = a.gt(b); break;
						case gte: a = a.gte(b); break;
						case lt:  a = a.lt(b); break;
						case lte: a = a.lte(b); break;
						default:
					}

					write(func, offset, target, a);
					break;
				}
				
				case not:
				case nor:
				case neg: {
					RegParam target = (RegParam)inst.getParam(0);
					
					Value a = read(func, offset, inst.getParam(1));
					switch(inst.type()) {
						case neg: a = a.neg(); break;
						case not: a = a.not(); break;
						case nor: a = a.nor(); break;
						default:
					}

					write(func, offset, target, a);
					break;
				}
				
				case read: {
					RegParam target = (RegParam)inst.getParam(0);
					
					Value a = read(func, offset, inst.getParam(1));
					a = memory.read((int)a.longValue(), inst.getSize());
					write(func, offset, target, a);
					break;
				}
				
				case write: {
					Value a = read(func, offset, inst.getParam(0));
					Value b = read(func, offset, inst.getParam(1));
					memory.write((int)a.longValue(), b, inst.getParam(1).getSize());
					
					if(CompilerMain.isDeveloper() || true) {
						if(a.longValue() >= 0xb8000 && a.longValue() < 0xc0000) {
							buffer.write((int)(a.longValue() - 0xb8000), (char)b.longValue());
							
							if(callback != null) {
								callback.bufferChanged(buffer, AmpleBufferCallback.BUFFER_CHANGED);
							}
						}
					}
					
					break;
				}
				
				case ret: {
					memory.write(offset, read(func, offset, inst.getParam(0)), inst.getParam(0).getSize());
					return;
				}
				
				case bnz: {
					Value a = read(func, offset, inst.getParam(0));
					if(a.doubleValue() != 0) ip = func.getLabel(inst.getParam(1).getName());
					break;
				}
				
				case brz: {
					Value a = read(func, offset, inst.getParam(0));
					if(a.doubleValue() == 0) ip = func.getLabel(inst.getParam(1).getName());
					break;
				}
				
				case br: {
					ip = func.getLabel(inst.getParam(0).getName());
					break;
				}
				
				case call: {
					VmFunction next = functions.get(inst.getParam(1).getName());
					boolean hasReturn = inst.getParam(0) != IRInstruction.NONE;
					
					for(int i = 2; i < inst.getNumParams(); i++) {
						Value v = read(func, offset, inst.getParam(i));
						memory.write(offset + func.bodySize + next.getArgs(i - 2), v.convert(next.getParamSize(i - 2)), next.getParamSize(i - 2));
					}
					
					run(next, offset + func.bodySize, 0);
					
					if(hasReturn) {
						RegParam reg = (RegParam)inst.getParam(0);
						memory.write(offset + func.getRegister(reg), memory.read(offset + func.bodySize, next.getType()), reg.getSize());
					}
					
					break;
				}
				
			}
		}
	}
	
	private Value read(VmFunction func, int offset, Param param) {
		if(param instanceof RegParam) {
			RegParam reg = (RegParam)param;
			return memory.read(offset + func.getRegister(reg), param.getSize());
		} else if(param instanceof NumParam) {
			NumParam reg = (NumParam)param;
			LowType type = reg.getSize();
			if(type.isPointer()) return Value.get(reg.getValue(), Atom.i64);
			return Value.get(reg.getValue(), type.type());
		} else if(param instanceof RefParam) {
			RefParam reg = (RefParam)param;
			return Value.dword(pointers.get(reg.toString()));
		} else if(param instanceof DebugParam) {
			return Value.dword(0); // T O D O: Illegal
		}
		
		throw new VmException("Not found: " + param.getClass());
	}
	
	private void write(VmFunction func, int offset, RegParam reg, Value value) {
		memory.write(offset + func.getRegister(reg), value.convert(reg.getSize()), reg.getSize());
	}
}
