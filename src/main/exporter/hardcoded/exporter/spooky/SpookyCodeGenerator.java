package hardcoded.exporter.spooky;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.impl.ICodeGenerator;
import hardcoded.compiler.instruction.*;
import hardcoded.compiler.instruction.Param.*;
import hardcoded.utils.error.CodeGenException;

public class SpookyCodeGenerator implements ICodeGenerator {
	static class Relative {
		public final int offset;
		Relative(int offset) { this.offset = offset; }
	}
	
	private ByteOutputWriter writer;
	private static final Address BASE_POINTER = Address.global(0);
	private static final Address TEMP         = Address.global(1);
	private static final Address ZERO         = Address.data(0);
	private static final Address MINUS_ONE    = Address.data(1);
	private SpookyBase base;
	
	// A program always start with the stack pointer and a temporary memory cell
	// Return values are always written to [ TEMP ]
	// [ SP ] [ TEMP ] 
	
	// The structure of a function always has access to the caller pointer, parameters and stack.
	// [ ip_pointer ] [ main_args ] [ main_stack ]
	
	
	// When another function is called it will write the function structure again
	// [ ip_pointer ] [ main_args ] [ main_stack ] [ ip_pointer ] [ func_args ] [ func_stack ]
	
	public void reset() {
		
	}
	
	public byte[] getBytecode(IRProgram program) {
		System.out.println("\nInside the spooky code generator");
		
		writer = new ByteOutputWriter();
		
		List<IRFunction> sorted = new ArrayList<>();
		for(IRFunction func : program.getFunctions()) {
			if(func.getName().equals("main")) {
				sorted.add(0, func);
			} else {
				sorted.add(func);
			}
		}
		
		base = new SpookyBase(program);
		List<SpookyFunction> list = base.list;
		writeProgramData();
		
		int func_id = 0;
		for(IRFunction func : sorted) {
			int id = func_id;
			if(func.length() == 0) id = -1;
			else func_id++;
			list.add(convertFunction(func, id));
		}
		
		for(SpookyFunction item : list) {
			for(SpookyBlock block : item.blocks) {
				apply_work(item, block);
			}
			
			if(!item.isExtern()) {
				int jump = item.getUsage();
				
				// Allocate stack at the begining
				SpookyBlock first = new SpookyBlock();
				first.insts.add(0, new SpookyInst(OpCode.CONST, jump, TEMP));
				first.insts.add(1, new SpookyInst(OpCode.ADD, BASE_POINTER, TEMP, BASE_POINTER));
				item.blocks.add(0, first);
				
				// Deallocate stack at the end
				SpookyBlock block = new SpookyBlock();
				block.insts.add(new SpookyInst(OpCode.CONST, jump, TEMP));
				block.insts.add(new SpookyInst(OpCode.SUB, BASE_POINTER, TEMP, BASE_POINTER));
				block.insts.add(new SpookyInst(OpCode.JMPADR, Address.stack(0)));
				item.addBlock(block);
			}
		}
		
		// Write all functions and data into the output buffer
		writeProgramBytes(list);
		
		{
			byte[] array = writer.toByteArray();
			
			try {
				File file = new File(System.getProperty("user.home") + "/Desktop/spooky/export.spook");
				if(!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				
				FileOutputStream stream = new FileOutputStream(file);
				stream.write(array);
				stream.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return writer.toByteArray();
	}
	
	@Override
	public byte[] getAssembler(IRProgram program) throws CodeGenException {
		return new byte[0];
	}
	
	private void writeProgramBytes(List<SpookyFunction> list) {
		int offset = 0;
		for(SpookyFunction item : list) {
			item.func_offset = offset;
			offset += getFunctionSize(item);
		}
		
		for(SpookyFunction item : list) {
			for(SpookyBlock block : item.blocks) {
				apply_work_jump(item, block);
			}
		}
		
		// Compile all functions and write
		for(SpookyFunction item : list) {
			for(SpookyBlock block : item.blocks) {
				writer.writeBytes(block.compile());
			}
		}
		
		// Add data segment tag
		writer.write(OpCode.DATA);
		
		// Runtime constants for operations
		writer.write( 0); // ZERO
		writer.write(-1); // MINUS_ONE
		
		// Write all function offsets to the function pointer array
		for(SpookyFunction item : list) {
			if(item.id >= 0) {
				writer.write(item.func_offset);
			}
		}
		
		// Write all strings to the data segment
		IRContext data = base.program.getContext();
		for(String str : data.getStrings()) {
			byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
			writer.writeInt(bytes.length);
			
			for(byte b : bytes) {
				writer.writeInt(b & 0xff);
			}
		}
		
		// Debugging the output
		for(SpookyFunction item : list) {
			System.out.println("\n" + item.func + " using " + item.getUsage() + " stack");
			for(SpookyBlock block : item.blocks) {
				for(SpookyInst inst : block.insts) {
					System.out.println("   " + inst);
				}
			}
		}
		
		// TODO: Reserving som extra data incase of out of bounds.
		for(int i = 0; i < 30; i++) {
			writer.write(0);
		}
	}
	
	private void writeProgramData() {
		writer.write(OpCode.BINDEF);
		writer.write("HCCompiler SpookyCodeGenerator");
		writer.write(OpCode.TEXT);
		
		// Create a entry function for the program
		SpookyFunction main = new SpookyFunction(null, -1);
		SpookyBlock block = new SpookyBlock();
		
		// Setup correct return pointer and jump to main
		block.insts.add(new SpookyInst(OpCode.CONST, 2, BASE_POINTER));
		block.insts.add(new SpookyInst(OpCode.CONST, 3, Address.global(2)));
		block.insts.add(new SpookyInst(OpCode.JMPADR, Address.functionPointer(0)));
		block.insts.add(new SpookyInst(OpCode.HALT));
		
		main.addBlock(block);
		base.list.add(0, main);
	}
	
	private SpookyFunction convertFunction(IRFunction func, int id) {
		SpookyFunction list = new SpookyFunction(func, id);
		SpookyBlock block = new SpookyBlock();
		
		for(IRInstruction a : func.getInstructions()) {
			switch(a.type()) {
				case call:
				case bnz:
				case brz:
				case br: {
					if(!block.isEmpty()) list.addBlock(block);
					list.addJumpBlock(a);
					block = new SpookyBlock();
					break;
				}
				
				case label: {
					if(!block.isEmpty()) list.addBlock(block);
					list.addLabel(a);
					block = new SpookyBlock();
					break;
				}
				
				default: {
					block.add(a);
				}
			}
		}
		
		if(!block.isEmpty()) {
			list.addBlock(block);
		}
		
		return list;
	}
	
	private void apply_work_jump(SpookyFunction func, SpookyBlock block) {
		for(SpookyInst inst : block.insts) {
			for(int i = 0; i < inst.params.size(); i++) {
				Object obj = inst.params.get(i);
				
				if(obj instanceof LabelParam) {
					if(inst.op == OpCode.JMP) {
						inst.params.set(i, base.getLabelIndex(func, (LabelParam)obj));
					} else {
						inst.params.set(i, Address.create(0, base.getLabelIndex(func, (LabelParam)obj)));
					}
				}
				
				if(obj instanceof Relative) {
					inst.params.set(i, base.getNextInstruction(func, inst) + ((Relative)obj).offset);
				}
			}
		}
	}
	
	private int getFunctionSize(SpookyFunction item) {
		int size = 0;
		
		for(SpookyBlock block : item.blocks) {
			size += block.insts.size();
		}
		
		return size;
	}
	
	private Address getStringAddress(int index) {
		IRContext data = base.program.getContext();
		
		int offset = 2;
		for(SpookyFunction func : base.list) {
			if(!func.isExtern()) offset ++;
		}
		
		String str = data.getString(index);
		for(String s : data.getStrings()) {
			if(s.equals(str)) return Address.data(offset);
			offset += 1 + s.length();
		}
		
		throw new NullPointerException("String not found '" + str + "' index = " + index);
	}
	
	private void apply_work(SpookyFunction func, SpookyBlock block) {
		List<SpookyInst> list = block.insts;
		for(IRInstruction inst : block.list) {
			switch(inst.type()) {
				case mov: {
					RegParam a = (RegParam)inst.getParam(0);
					Param b = inst.getParam(1);
					
					Address target = generate(func, (RegParam)a);
					if(b instanceof NumParam) {
						list.add(new SpookyInst(OpCode.CONST, ((NumParam)b).getValue(), target));
					} else if(b instanceof RegParam) {
						list.add(new SpookyInst(OpCode.MOV, generate(func, (RegParam)b), target));
					} else if(b instanceof RefParam) {
						list.add(new SpookyInst(OpCode.CONST, getStringAddress(b.getIndex()).offset, target));
					} else throw new IllegalArgumentException("Invalid mov parameter '" + b + "' '" + (b == null ? "NULL":b.getClass())+ "'");
					break;
				}
				
				case not:   // eq (A, 0)
				case neg: { // mul(B, A, -1)
					RegParam a = (RegParam)inst.getParam(0);
					RegParam b = (RegParam)inst.getParam(1);
					
					Address target = generate(func, a);
					Address source = generate(func, b);
					
					if(inst.type() == IRType.not) {
						list.add(new SpookyInst(OpCode.EQ, source, ZERO, target));
					} else {
						list.add(new SpookyInst(OpCode.MUL, source, MINUS_ONE, target));
					}
					
					break;
				}
				
				case neq:	// eq(eq(A, B), 0)
				case gt:	// lt(B, A)
				case gte:	// lte(B, A)
				
				case eq:	// eq(A, B)
				case lt:	// lt(A, B)
				case lte:	// lte(A, B)
				
				case sub: case add:
				case mul: case div:
				case mod: {
					RegParam a = (RegParam)inst.getParam(0);
					RegParam b = (RegParam)inst.getParam(1);
					Param c = inst.getParam(2);
					
					Address target = generate(func, a);
					Address op1 = generate(func, b);
					OpCode op = OpCode.convert(inst.type());
					
					if(c instanceof NumParam) {
						list.add(new SpookyInst(OpCode.CONST, ((NumParam)c).getValue(), TEMP));
						if(op == null) {
							op = OpCode.convertSpecial(inst.type());
							list.add(new SpookyInst(op, TEMP, op1, TEMP));
						} else {
							list.add(new SpookyInst(op, op1, TEMP, target));
						}
					} else if(c instanceof RegParam) {
						if(op == null) {
							list.add(new SpookyInst(op, op1, generate(func, (RegParam)c), TEMP));
							list.add(new SpookyInst(OpCode.EQ, ZERO, TEMP, target));
						} else {
							list.add(new SpookyInst(op, op1, generate(func, (RegParam)c), target));
						}
					} else throw new IllegalArgumentException("Invalid parameter '" + c + "' '" + (c == null ? "NULL":c.getClass())+ "'");
					break;
				}
				
				case br: {
					list.add(new SpookyInst(OpCode.JMP, ZERO, (LabelParam)inst.getParam(0)));
					break;
				}
				
				case brz: case bnz: {
					Param a = inst.getParam(0);
					LabelParam label = (LabelParam)inst.getParam(1);
					
					if(a instanceof NumParam) {
						long value = ((NumParam)a).getValue();
						
						if(inst.type() == IRType.brz) {
							if(value == 0) list.add(new SpookyInst(OpCode.JMP, ZERO, label));
						} else {
							if(value != 0) list.add(new SpookyInst(OpCode.JMP, ZERO, label));
						}
						
						break;
					} else if(a instanceof RegParam) {
						if(inst.type() == IRType.brz) {
							list.add(new SpookyInst(OpCode.JMP, generate(func, (RegParam)a), label));
						} else {
							list.add(new SpookyInst(OpCode.EQ, generate(func, (RegParam)a), ZERO, TEMP));
							list.add(new SpookyInst(OpCode.JMP, TEMP, label));
						}
					} else throw new IllegalArgumentException("Invalid brach parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
					break;
				}
				
				case read: {
					RegParam a = (RegParam)inst.getParam(0);
					RegParam b = (RegParam)inst.getParam(1);
					
					Address target = generate(func, a);
					Address source = generate(func, b);
					
					// Set temp to base_pointer
					// We want index to be [source:0]
					list.add(new SpookyInst(OpCode.MOV, source, TEMP));
					list.add(new SpookyInst(OpCode.MOV, Address.create(1, 0), target));
					break;
				}
				
				case ret: {
					Param a = inst.getParam(0);
					
					int jump = func.getUsage();
					if(a instanceof NumParam) {
						list.add(new SpookyInst(OpCode.CONST, jump, TEMP));
						list.add(new SpookyInst(OpCode.SUB, BASE_POINTER, TEMP, BASE_POINTER));
						list.add(new SpookyInst(OpCode.CONST, ((NumParam)a).getValue(), TEMP));
						list.add(new SpookyInst(OpCode.JMPADR, Address.stack(0)));
					} else if(a instanceof RegParam) {
						list.add(new SpookyInst(OpCode.CONST, jump, TEMP));
						list.add(new SpookyInst(OpCode.SUB, BASE_POINTER, TEMP, BASE_POINTER));
						list.add(new SpookyInst(OpCode.JMPADR, Address.stack(0)));
					} else throw new IllegalArgumentException("Invalid return parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
					
					break;
				}
				
				case call: {
					Param a = inst.getParam(0);
					FunctionLabel label = (FunctionLabel)inst.getParam(1);
					SpookyFunction called = base.getFunction(label);
					
					for(int i = 2; i < inst.getNumParams(); i++) {
						Param c = inst.getParam(i);
						
						// We write the address tack to the back of our function
						Address target = Address.stack(i - 1);
						
						if(c instanceof NumParam) {
							list.add(new SpookyInst(OpCode.CONST, ((NumParam)c).getValue(), target));
						} else if(c instanceof RegParam) {
							list.add(new SpookyInst(OpCode.MOV, generate(func, (RegParam)c), target));
						} else if(c instanceof RefParam) {
							list.add(new SpookyInst(OpCode.CONST, generate(func, (RefParam)c).offset, target));
						} else {
							throw new IllegalArgumentException("Invalid call parameter '" + c + "' '" + (c == null ? "NULL":c.getClass())+ "'");
						}
					}
					
					
					if(called.isExtern()) {
						int val = called.getNumParams() + 1;
						list.add(new SpookyInst(OpCode.CONST, val, TEMP));
						list.add(new SpookyInst(OpCode.ADD, BASE_POINTER, TEMP, BASE_POINTER));
						list.add(new SpookyInst(OpCode.EXTERN, label.getName()));
						list.add(new SpookyInst(OpCode.CONST, val, TEMP));
						list.add(new SpookyInst(OpCode.SUB, BASE_POINTER, TEMP, BASE_POINTER));
						list.add(new SpookyInst(OpCode.MOV, Address.stack(0), TEMP));
					} else {
						list.add(new SpookyInst(OpCode.CONST, new Relative(2), Address.stack(0)));
						list.add(new SpookyInst(OpCode.JMPADR, Address.functionPointer(called.id)));
					}
					
					if(a instanceof RegParam) {
						list.add(new SpookyInst(OpCode.MOV, TEMP, generate(func, (RegParam)a)));
					} else {
						// throw new IllegalArgumentException("Invalid call parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
					}
					
					break;
				}
				
				default: {
					list.add(new SpookyInst(OpCode.DEBUG, inst.toString()));
				}
			}
		}
	}
	
	private Address generate(SpookyFunction func, RegParam reg) {
		if(reg.isTemporary()) {
			return Address.stack(-func.getUsage() + func.getNumParams() + reg.getIndex() + 1);
		}
		
		return Address.stack(-func.getUsage() + reg.getIndex() + 1);
	}
	
	private Address generate(SpookyFunction func, RefParam reg) {
		Address addr = getStringAddress(reg.getIndex());
		System.out.println(reg + " == " + addr);
		return  addr;
	}
}
