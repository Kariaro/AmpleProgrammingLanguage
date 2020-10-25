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

public class HcVm {
	// Main memory
	private final Memory memory = new Memory();
	private final Map<String, VmFunction> functions;
	private final Map<String, Integer> pointers;
	private final int consts_offset;
	private final VmFunction entry;
	private final StringBuilder stdout = new StringBuilder();
	
	public static void run(IRProgram program) {
		if(CompilerMain.isDeveloper()) {
			System.out.println("----------------------------");
		}
		
		new HcVm(program).run();
	}
	
	private HcVm(IRProgram program) {
		this.functions = new HashMap<>();
		this.pointers = new HashMap<>();
		
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
	}
	
	private void run() {
		run(entry, consts_offset, 0);
		System.out.println(stdout.toString().trim());
	}
	
	private void run(VmFunction func, int offset, int ip) {
		if(func.getNumInstructions() < 1) {
			switch(func.getName()) {
				case "print": {
					char c = (char)(memory.read(offset, Atom.i32).longValue() & 0xff);
					//System.out.println("Stdout: '" + StringUtils.escapeString("" + c) + "'");
					stdout.append(c);
					return;
				}
				case "printInt": {
					stdout.append((int)(memory.read(offset, Atom.i32).longValue()));
					return;
				}
			}
			return;
		}
		
		while(true) {
//			for(int i = offset; i < offset + func.bodySize; i++) {
//				System.out.print(StringUtils.escapeString(Character.toString((char)memory.read(i))));
//			}
//			System.out.println();
			
			if(ip >= func.getNumInstructions()) return;
			IRInstruction inst = func.getInstruction(ip++);
			//System.out.printf("%4d : %s\n", ip, inst);
			
			
			
			switch(inst.type()) {	
				default: {
					//System.out.println("[Missing] type: " + inst);
				}
				case nop:
				case label: break;
				
				case mov: {
					Reg target = (Reg)inst.getParam(0);
					Value v = read(func, offset, inst.getParam(1));
					//System.out.println("mov: " + target + " = " + v);
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
					Reg target = (Reg)inst.getParam(0);
					
					Value a = read(func, offset, inst.getParam(1));
					Value b = read(func, offset, inst.getParam(2));
					//System.out.println(inst.type() + "| " + a + " <op> " + b);
					
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
					
//					Value read = read(func, offset, target);
//					if(!read.toString().equals(a.toString())) throw new AssertionError("'" + read + "' != '" + a + "'");
					break;
				}
				
				case not:
				case nor:
				case neg: {
					Reg target = (Reg)inst.getParam(0);
					
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
					Reg target = (Reg)inst.getParam(0);
					
					Value a = read(func, offset, inst.getParam(1));
					a = memory.read((int)a.longValue(), inst.getSize());
					//System.out.println("mov: " + target + " = " + a);
					write(func, offset, target, a);
					break;
				}
				
				case write: {
					Value a = read(func, offset, inst.getParam(0));
					Value b = read(func, offset, inst.getParam(1));
					memory.write((int)a.longValue(), b, inst.getParam(1).getSize());
					
					if(CompilerMain.isDeveloper() || true) {
						if(a.longValue() >= 0xb8000 && a.longValue() < 0xc0000) {
							stdout.ensureCapacity(0x8000);
							stdout.setLength(0x8000);
							stdout.setCharAt((int)(a.longValue() - 0xb8000), (char)b.longValue());
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
					//System.out.println("Calling -> " + next + " / " + next.bodySize);
					boolean hasReturn = inst.getParam(0) != IRInstruction.NONE;
					
					for(int i = 2; i < inst.getNumParams(); i++) {
						Value v = read(func, offset, inst.getParam(i));
						memory.write(offset + func.bodySize + next.getArgs(i - 2), v.convert(next.getParamSize(i - 2)), next.getParamSize(i - 2));
					}
					
					run(next, offset + func.bodySize, 0);
					
					if(hasReturn) {
						Reg reg = (Reg)inst.getParam(0);
						memory.write(offset + func.getRegister(reg), memory.read(offset + func.bodySize, next.getType()), reg.getSize());
					}
					
					break;
				}
				
			}
		}
	}
	
	private Value read(VmFunction func, int offset, Param param) {
		if(param instanceof Reg) {
			Reg reg = (Reg)param;
			return memory.read(offset + func.getRegister(reg), param.getSize());
		} else if(param instanceof NumberReg) {
			NumberReg reg = (NumberReg)param;
			return Value.get(reg.getValue(), get(reg.getSize()));
		} else if(param instanceof RefReg) {
			RefReg reg = (RefReg)param;
			return Value.dword(pointers.get(reg.toString()));
		} else if(param instanceof DebugParam) {
			return Value.dword(0); // TODO: Illegal
		}
		
		throw new NullPointerException("Not found: " + param.getClass());
	}
	
	private void write(VmFunction func, int offset, Reg reg, Value value) {
		memory.write(offset + func.getRegister(reg), value.convert(reg.getSize()), reg.getSize());
	}
	
	private Atom get(LowType type) {
		if(type.isPointer()) return Atom.i64;
		return type.type();
	}
}
