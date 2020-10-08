package hardcoded.exporter.spooky;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.instruction.*;
import hardcoded.compiler.instruction.IRInstruction.*;
import hardcoded.exporter.impl.CodeBlockType;
import hardcoded.exporter.impl.CodeGeneratorImpl;

public class SpookyCodeGenerator2 implements CodeGeneratorImpl {
	static class Relative {
		public final int offset;
		Relative(int offset) { this.offset = offset; }
	}
	
	private ByteOutputWriter writer;
	private int data_offset;
	
	private static final Address BASE_POINTER  = Address.create(-1,  0);
	private static final Address ZERO_CONST    = Address.create(-1, -2);
	private static final Address NEG_ONE_CONST = Address.create(-1, -3);
	private static final Address TEMP_ADDR     = Address.create(-1,  1);
	private SpookyBase base;
	
	public byte[] generate(IRProgram program) {
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
		

		writeProgramData(program, sorted);
		
		base = new SpookyBase();
		List<SpookyFunction> list = base.list;
		
		int func_id = 0;
		for(IRFunction func : sorted) {
			int id = func_id;
			if(func.length() == 0) id = -1;
			else func_id++;
			list.add(convertFunction(func, id));
		}
		
		for(SpookyFunction item : list) {
			apply_work(item);
			
			if(item.func.length() != 0) {
				SpookyBlock first = item.blocks.get(0);
				
				if(item.func.getName().equals("main")) {
					SpookyBlock block = new SpookyBlock(CodeBlockType.INST);
					block.insts.add(new SpookyInst(OpCode.CONST, item.getStackSize() + 1, BASE_POINTER));
					item.blocks.add(0, block);
				} else {
					first.insts.add(0, new SpookyInst(OpCode.CONST, item.getStackSize() - item.getNumParams(), TEMP_ADDR));
					first.insts.add(1, new SpookyInst(OpCode.ADD, TEMP_ADDR, BASE_POINTER, BASE_POINTER));
				}
				
				// Halt if outside of function
				SpookyBlock block = new SpookyBlock(CodeBlockType.INST);
				block.insts.add(new SpookyInst(OpCode.CONST, item.getStackSize() - item.getNumParams(), TEMP_ADDR));
				block.insts.add(new SpookyInst(OpCode.SUB, BASE_POINTER, TEMP_ADDR, BASE_POINTER));
				block.insts.add(new SpookyInst(OpCode.JMPADR, Address.stack(0)));
				block.insts.add(new SpookyInst(OpCode.HALT));
				item.addBlock(block);
			} else {
				item.stack_size = 0;
			}
		}
		
		
		
		int offset = 0;
		for(SpookyFunction item : list) {
			item.func_offset = offset;
			offset += getFunctionSize(item);
		}
		
		for(SpookyFunction item : list) {
			apply_work_jump(item);
		}
		
//		{
//			list.clear();
//			SpookyFunction cont = new SpookyFunction(sorted.get(0), 0); list.add(cont);
//			SpookyBlock block = new SpookyBlock(); cont.addBlock(block);
//			
//			
//			block.insts.add(new SpookyInst(OpCode.CONST, 10, BASE_POINTER));
//			block.insts.add(new SpookyInst(OpCode.MOV, NEG_ONE_CONST, Address.stack(-1)));
//			block.insts.add(new SpookyInst(OpCode.JMPADR, ZERO_CONST));
//			block.insts.add(new SpookyInst(OpCode.HALT));
//			block.insts.add(new SpookyInst(OpCode.EXTERN, "printInt"));
//			block.insts.add(new SpookyInst(OpCode.JMP, ZERO_CONST, 3));
//		}
		
		for(SpookyFunction item : list) {
			for(SpookyBlock block : item.blocks) {
				writer.writeBytes(block.compile());
			}
		}
		
		writer.write(OpCode.DATA);
		
		// StackPointer
		writer.write( 0); // STACK_POINTER
		writer.write( 0); // ZERO_CONST
		writer.write(-1); // NEG_ONE_CONST
		
		for(SpookyFunction item : list) {
			if(item.id >= 0) {
				writer.write(item.func_offset);
			}
		}
		// TODO: All functions are saved here
		
		data_offset = writer.index();
		
		// Write all strings into memory
//		IRData data = program.getIRData();
//		for(String str : data.strings) {
//			writer.write(OpCode.TEXT);
//			writer.write(str);
//		}
		
		{
			byte[] array = writer.toByteArray();
			
			try {
				FileOutputStream stream = new FileOutputStream(new File("C:/Users/Admin/Desktop/spooky/export.spook"));
				stream.write(array);
				stream.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		return writer.toByteArray();
	}
	
	private void writeProgramData(IRProgram program, List<IRFunction> list) {
		writer.write(OpCode.BINDEF);
		writer.write("HCCompiler SpookyCodeGenerator");
		writer.write(OpCode.TEXT);
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
		
		if(!block.isEmpty())
			list.addBlock(block);
		
		return list;
	}
	
	private void apply_work(SpookyFunction item) {
		for(SpookyBlock block : item.blocks) {
			apply_work(item.func.getProgram(), item, block);
		}
	}
	
	private void apply_work_jump(SpookyFunction item) {
		for(SpookyBlock block : item.blocks) {
			apply_work_jump(item.func.getProgram(), item, block);
		}
		
		// Debugging
		System.out.println("\n" + item.func + ", " + base.calculateStack(item));
		for(SpookyBlock block : item.blocks) {
			for(SpookyInst inst : block.insts) {
				System.out.println("   " + inst);
			}
		}
	}
	
	private void apply_work_jump(IRProgram program, SpookyFunction func, SpookyBlock block) {
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
	
	private Address getStringAddress(IRProgram program, int index) {
		IRData data = program.getIRData();
		
		int offset = 0;
		for(int i = 0; i < index; i++) {
			String string = data.getString(i);
			offset += 2;
			offset += string.length();
		}
		
		return Address.create(data_offset, offset);
	}
	
	private void apply_work(IRProgram program, SpookyFunction func, SpookyBlock block) {
		List<SpookyInst> list = block.insts;
		for(IRInstruction inst : block.list) {
			switch(inst.type()) {
				case mov: {
					Reg a = (Reg)inst.getParam(0);
					Param b = inst.getParam(1);
					
					Address target = generate(func, (Reg)a);
					if(b instanceof NumberReg) {
						list.add(new SpookyInst(OpCode.CONST, ((NumberReg)b).value, target));
					} else if(b instanceof Reg) {
						list.add(new SpookyInst(OpCode.MOV, generate(func, (Reg)b), target));
					} else if(b instanceof RefReg) {
						list.add(new SpookyInst(OpCode.MOV, getStringAddress(program, b.getIndex()), target));
					} else throw new IllegalArgumentException("Invalid mov parameter '" + b + "' '" + (b == null ? "NULL":b.getClass())+ "'");
					break;
				}
				
				case not:   // eq (A, 0)
				case neg: { // mul(A, -1)
					Reg a = (Reg)inst.getParam(0);
					
					Address target = generate(func, (Reg)a);
					if(inst.type() == IRType.not) {
						list.add(new SpookyInst(OpCode.EQ, target, ZERO_CONST, target));
					} else {
						list.add(new SpookyInst(OpCode.MUL, target, NEG_ONE_CONST, target));
					}
					
					break;
				}
				
				case neq:	// eq(eq(A, B), 0)
				case gt:	// eq(lte(A, B), 0)
				case gte:	// eq(lt(A, B), 0)
				
				case eq:	// eq(A, B)
				case lt:	// lt(A, B)
				case lte:	// lte(A, B)
				
				case sub: case add:
				case mul: case div:
				case mod: {
					Reg a = (Reg)inst.getParam(0);
					Reg b = (Reg)inst.getParam(1);
					Param c = inst.getParam(2);
					
					Address target = generate(func, a);
					Address op1 = generate(func, b);
					OpCode op = OpCode.convert(inst.type());
					
					if(c instanceof NumberReg) {
						list.add(new SpookyInst(OpCode.CONST, ((NumberReg)c).value, TEMP_ADDR));
						if(op == null) {
							op = OpCode.convertSpecial(inst.type());
							list.add(new SpookyInst(op, TEMP_ADDR, op1, TEMP_ADDR));
						} else {
							list.add(new SpookyInst(op, op1, TEMP_ADDR, target));
						}
					} else if(c instanceof Reg) {
						if(op == null) {
							list.add(new SpookyInst(op, op1, generate(func, (Reg)c), TEMP_ADDR));
							list.add(new SpookyInst(OpCode.EQ, ZERO_CONST, TEMP_ADDR, target));
						} else {
							list.add(new SpookyInst(op, op1, generate(func, (Reg)c), target));
						}
					} else throw new IllegalArgumentException("Invalid parameter '" + c + "' '" + (c == null ? "NULL":c.getClass())+ "'");
					break;
				}
				
				case br: {
					// TODO: Make the label point to the correct memory index
					LabelParam label = (LabelParam)inst.getParam(0);
					list.add(new SpookyInst(OpCode.JMP, ZERO_CONST, label));
					break;
				}
				
				case brz: case bnz: {
					Param a = inst.getParam(0);
					LabelParam label = (LabelParam)inst.getParam(1);
					
					if(a instanceof NumberReg) {
						long value = ((NumberReg)a).value;
						
						if(inst.type() == IRType.brz) {
							if(value == 0) list.add(new SpookyInst(OpCode.JMP, ZERO_CONST, label));
						} else {
							if(value != 0) list.add(new SpookyInst(OpCode.JMP, ZERO_CONST, label));
						}
						
						break;
					} else if(a instanceof Reg) {
						if(inst.type() == IRType.brz) {
							list.add(new SpookyInst(OpCode.JMP, generate(func, (Reg)a), label));
						} else {
							list.add(new SpookyInst(OpCode.EQ, generate(func, (Reg)a), ZERO_CONST, TEMP_ADDR));
							list.add(new SpookyInst(OpCode.JMP, TEMP_ADDR, label));
						}
					} else throw new IllegalArgumentException("Invalid brach parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
					break;
				}
				
				case ret: {
					Param a = inst.getParam(0);
					
					// TODO: Read from stack
					int jump = func.getNumParams() + 1;
					if(a instanceof NumberReg) {
						list.add(new SpookyInst(OpCode.CONST, jump, TEMP_ADDR));
						list.add(new SpookyInst(OpCode.SUB, BASE_POINTER, TEMP_ADDR, BASE_POINTER));
						list.add(new SpookyInst(OpCode.CONST, ((NumberReg)a).value, TEMP_ADDR));
						list.add(new SpookyInst(OpCode.JMPADR, Address.stack(0)));
					} else if(a instanceof Reg) {
						list.add(new SpookyInst(OpCode.CONST, jump, TEMP_ADDR));
						list.add(new SpookyInst(OpCode.SUB, BASE_POINTER, TEMP_ADDR, BASE_POINTER));
						list.add(new SpookyInst(OpCode.JMPADR, Address.stack(0)));
					} else throw new IllegalArgumentException("Invalid return parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
					
					break;
				}
				
				case call: { // TODO: Call
					Param a = inst.getParam(0);
					FunctionLabel label = (FunctionLabel)inst.getParam(1);
					SpookyFunction called = base.getFunction(label);
					
					boolean isExternal = called.id < 0;
					// First we need to allocate enough memory on stack.
					// called.func.getNumParams();
					
					// Get the size of this functions stack.
					int stackOffset = called.getNumParams() + 2;
					if(isExternal) {
						// stackOffset = called.getNumParams() + 2;
					}
					
					// Allocate 
					
					for(int i = 2; i < inst.params.size(); i++) {
						Param c = inst.getParam(i);
						
						// We write the address tack to the back of our function
						Address target = Address.stack(-i + stackOffset);
						
						if(c instanceof NumberReg) {
							list.add(new SpookyInst(OpCode.CONST, ((NumberReg)c).value, target));
						} else if(c instanceof Reg) {
							list.add(new SpookyInst(OpCode.MOV, generate(func, (Reg)c), target));
						} else {
							throw new IllegalArgumentException("Invalid call parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
						}
					}
					

					if(isExternal) {
						int val = called.getNumParams() + 1;
						list.add(new SpookyInst(OpCode.CONST, val, TEMP_ADDR));
						list.add(new SpookyInst(OpCode.ADD, BASE_POINTER, TEMP_ADDR, BASE_POINTER));
						list.add(new SpookyInst(OpCode.EXTERN, label.ident.name()));
						list.add(new SpookyInst(OpCode.CONST, val, TEMP_ADDR));
						list.add(new SpookyInst(OpCode.SUB, BASE_POINTER, TEMP_ADDR, BASE_POINTER));
					} else {
						list.add(new SpookyInst(OpCode.CONST, new Relative(2), Address.stack(0)));
						list.add(new SpookyInst(OpCode.JMPADR, Address.functionPointer(called.id)));
					}
					
					if(a instanceof Reg) {
						list.add(new SpookyInst(OpCode.MOV, TEMP_ADDR, generate(func, (Reg)a)));
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
	
	private Address generate(SpookyFunction func, Reg reg) {
		if(reg.isTemporary) {
			return Address.stack(func.getStackSize() - reg.getIndex());
		}
		
		System.out.println(reg + ":" + reg.getIndex());
		// ReturnAddr, Result, Params, Stack
		return Address.stack(-reg.getIndex() - 1);
	}
}
