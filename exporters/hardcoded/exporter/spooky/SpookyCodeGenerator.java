package hardcoded.exporter.spooky;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.instruction.*;
import hardcoded.compiler.instruction.IRInstruction.*;
import hardcoded.exporter.impl.CodeBlockType;
import hardcoded.exporter.impl.CodeGeneratorImpl;

public class SpookyCodeGenerator implements CodeGeneratorImpl {
	static class Relative {
		public final int offset;
		Relative(int offset) { this.offset = offset; }
	}
	
	private ByteOutputWriter writer;
	private int data_offset;
	
	private Address zero_number = Address.create(-1, 18);
	private Address neg_one_num = Address.create(-1, 17);
	private Address temp_number = Address.create(-1, 16);
	private Address return_addr = Address.create(-1, 15); // 
	private Address return_valu = Address.create(-1, 1); // 
	
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
		
		List<SpookyContainer> list = new ArrayList<>();
		for(IRFunction func : sorted) {
			list.add(convertFunction(func, list.size()));
		}
		
		for(SpookyContainer item : list) {
			apply_work(item);
			
			if(item.func.length() != 0) {
				if(item.func.getName().equals("main")) {
					SpookyBlock block = new SpookyBlock(CodeBlockType.INST);
					block.insts.add(new SpookyInst(OpCode.HALT));
					item.addBlock(block);
				} else {
					SpookyBlock block = new SpookyBlock(CodeBlockType.JUMP);
					block.insts.add(new SpookyInst(OpCode.JMPADR, return_addr));
					item.addBlock(block);
				}
			}
		}
		
		
		
		int offset = 0;
		for(SpookyContainer item : list) {
			item.func_offset = offset;
			offset += getFunctionSize(item);
		}
		
		for(SpookyContainer item : list) {
			apply_work_jump(list, item);
		}
		
		for(SpookyContainer item : list) {
			for(SpookyBlock block : item.blocks) {
				writer.writeBytes(block.compile());
			}
		}
		
		writer.write(OpCode.DATA);
		
		for(int i = 0; i < 18; i++) writer.write(i); // Reserved
		
		writer.write( 0); // Always zero
		writer.write(-1); // Negative one
		writer.write( 0); // Temp value
		
		data_offset = writer.index();
		
		// Write all strings into memory
		IRData data = program.getIRData();
		for(String str : data.strings) {
			writer.write(OpCode.TEXT);
			writer.write(str);
		}
		
		return writer.toByteArray();
	}
	
	private void writeProgramData(IRProgram program, List<IRFunction> list) {
		writer.write(OpCode.BINDEF);
		writer.write("HCCompiler SpookyCodeGenerator");
		writer.write(OpCode.TEXT);
	}
	
	private int calculateDataSize(IRProgram program, IRFunction func) {
		int size = 3 + func.getNumParams() * 4;
		IRData data = program.getIRData();
		for(String str : data.strings) {
			size += 1 + (str.getBytes(StandardCharsets.ISO_8859_1).length);
		}
		
		return size;
	}
	
	private SpookyContainer convertFunction(IRFunction func, int id) {
		SpookyContainer list = new SpookyContainer(func, id);
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
	
	private void apply_work(SpookyContainer item) {
		for(SpookyBlock block : item.blocks) {
			apply_work(item.func.getProgram(), item, block);
		}
	}
	
	private void apply_work_jump(List<SpookyContainer> full, SpookyContainer item) {
		for(SpookyBlock block : item.blocks) {
			apply_work_jump(item.func.getProgram(), full, item, block);
		}
		
		// Debugging
		System.out.println("\n" + item.func);
		for(SpookyBlock block : item.blocks) {
			for(SpookyInst inst : block.insts) {
				System.out.println("   " + inst);
			}
		}
	}
	
	private int findLabel(List<SpookyContainer> full, SpookyContainer base, LabelParam label) {
		int index = base.func_offset;
		
		if(label instanceof FunctionLabel) {
			FunctionLabel flabel = (FunctionLabel)label;
			
			for(SpookyContainer item : full) {
				if(item.func.getName().equals(flabel.getName())) {
					return item.func_offset;
				}
			}
		} else {
			for(SpookyBlock block : base.blocks) {
				if(block.isLabelBlock()) {
					if(block.getDataName().equals(label.toString()))
						return index;
				} else {
					index += block.insts.size();
				}
			}
		}
		
		throw new IllegalArgumentException("Cound not find the label '" + label + "'");
	}
	
	private int findNextInst(List<SpookyContainer> full, SpookyContainer base, SpookyInst inst) {
		int index = 0;
		
		for(SpookyContainer item : full) {
			for(SpookyBlock block : item.blocks) {
				for(SpookyInst i : block.insts) {
					if(i == inst) return index;
					index++;
				}
			}
		}
		
		throw new IllegalArgumentException("Cound not find the instruction '" + inst + "'");
	}
	
	private void apply_work_jump(IRProgram program, List<SpookyContainer> full, SpookyContainer base, SpookyBlock block) {
		for(SpookyInst inst : block.insts) {
			for(int i = 0; i < inst.params.size(); i++) {
				Object obj = inst.params.get(i);
				
				if(obj instanceof LabelParam) {
					if(inst.op == OpCode.JMP) {
						inst.params.set(i, findLabel(full, base, (LabelParam)obj));
					} else {
						inst.params.set(i, Address.create(0, findLabel(full, base, (LabelParam)obj)));
					}
				}
				
				if(obj instanceof Relative) {
					inst.params.set(i, findNextInst(full, base, inst) + ((Relative)obj).offset);
				}
			}
		}
	}
	
	private int getStackSize(SpookyContainer item) {
		int size = item.func.getNumParams();
		
		for(SpookyBlock block : item.blocks) {
			for(IRInstruction inst : block.list) {
				for(Param p : inst.params) {
					if(!(p instanceof Reg)) continue;
					Reg reg = (Reg)p;
					
					if(reg.isGenerated) {
						size += 4;
					}
				}
			}
		}
		
		return size;
	}
	
	private int getFunctionSize(SpookyContainer item) {
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
	
	private void apply_work(IRProgram program, SpookyContainer base, SpookyBlock block) {
		List<SpookyInst> list = block.insts;
		for(IRInstruction inst : block.list) {
			switch(inst.type()) {
				case mov: {
					Reg a = (Reg)inst.getParam(0);
					Param b = inst.getParam(1);
					
					Address target = generate(base, (Reg)a);
					if(b instanceof NumberReg) {
						list.add(new SpookyInst(OpCode.CONST, ((NumberReg)b).value, target));
					} else if(b instanceof Reg) {
						list.add(new SpookyInst(OpCode.MOV, generate(base, (Reg)b), target));
					} else if(b instanceof RefReg) {
						list.add(new SpookyInst(OpCode.MOV, getStringAddress(program, b.getIndex()), target));
					} else throw new IllegalArgumentException("Invalid mov parameter '" + b + "' '" + (b == null ? "NULL":b.getClass())+ "'");
					break;
				}
				
				case not:   // eq (A, 0)
				case neg: { // mul(A, -1)
					Reg a = (Reg)inst.getParam(0);
					
					Address target = generate(base, (Reg)a);
					if(inst.type() == IRType.not) {
						list.add(new SpookyInst(OpCode.EQ, target, zero_number, target));
					} else {
						list.add(new SpookyInst(OpCode.MUL, target, neg_one_num, target));
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
					
					Address target = generate(base, a);
					Address op1 = generate(base, b);
					OpCode op = OpCode.convert(inst.type());
					
					if(c instanceof NumberReg) {
						list.add(new SpookyInst(OpCode.CONST, ((NumberReg)c).value, temp_number));
						if(op == null) {
							op = OpCode.convertSpecial(inst.type());
							list.add(new SpookyInst(op, op1, temp_number, temp_number));
							list.add(new SpookyInst(OpCode.EQ, zero_number, temp_number, target));
						} else {
							list.add(new SpookyInst(op, op1, temp_number, target));
						}
					} else if(c instanceof Reg) {
						if(op == null) {
							list.add(new SpookyInst(op, op1, generate(base, (Reg)c), temp_number));
							list.add(new SpookyInst(OpCode.EQ, zero_number, temp_number, target));
						} else {
							list.add(new SpookyInst(op, op1, generate(base, (Reg)c), target));
						}
					} else throw new IllegalArgumentException("Invalid parameter '" + c + "' '" + (c == null ? "NULL":c.getClass())+ "'");
					break;
				}
				
				case br: {
					// TODO: Make the label point to the correct memory index
					LabelParam label = (LabelParam)inst.getParam(0);
					list.add(new SpookyInst(OpCode.JMP, zero_number, label));
					break;
				}
				
				case brz: case bnz: {
					Param a = inst.getParam(0);
					LabelParam label = (LabelParam)inst.getParam(1);
					
					if(a instanceof NumberReg) {
						long value = ((NumberReg)a).value;
						
						if(inst.type() == IRType.brz) {
							if(value == 0) list.add(new SpookyInst(OpCode.JMP, zero_number, label));
						} else {
							if(value != 0) list.add(new SpookyInst(OpCode.JMP, zero_number, label));
						}
						
						break;
					} else if(a instanceof Reg) {
						if(inst.type() == IRType.brz) {
							list.add(new SpookyInst(OpCode.JMP, generate(base, (Reg)a), label));
						} else {
							list.add(new SpookyInst(OpCode.EQ, generate(base, (Reg)a), zero_number, temp_number));
							list.add(new SpookyInst(OpCode.JMP, temp_number, label));
						}
					} else throw new IllegalArgumentException("Invalid brach parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
					break;
				}
				
				case ret: {
					Param a = inst.getParam(0);
					
					if(a instanceof NumberReg) {
						list.add(new SpookyInst(OpCode.CONST, ((NumberReg)a).value, return_valu));
						list.add(new SpookyInst(OpCode.JMPADR, return_addr));
					} else if(a instanceof Reg) {
						list.add(new SpookyInst(OpCode.MOV, generate(base, (Reg)a), return_valu));
						list.add(new SpookyInst(OpCode.JMPADR, return_addr));
					} else throw new IllegalArgumentException("Invalid return parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
					
					break;
				}
				
				case call: {
					Param a = inst.getParam(0);
					FunctionLabel label = (FunctionLabel)inst.getParam(1);
					
					// [returnvärde] [returnaddress] [arg1] [arg2] ..
					for(int i = 2; i < inst.params.size(); i++) {
						Param c = inst.getParam(i);
						
						Address target = Address.create(-1, i);
						if(c instanceof NumberReg) {
							list.add(new SpookyInst(OpCode.CONST, ((NumberReg)c).value, target));
						} else if(c instanceof Reg) {
							list.add(new SpookyInst(OpCode.MOV, generate(base, (Reg)c), target));
						} else {
							throw new IllegalArgumentException("Invalid call parameter '" + a + "' '" + (a == null ? "NULL":a.getClass())+ "'");
						}
					}
					
					if(isExternal(base, label)) {
						list.add(new SpookyInst(OpCode.EXTERN, label.ident.name()));
					} else {
						list.add(new SpookyInst(OpCode.CONST, new Relative(2), return_addr));
						list.add(new SpookyInst(OpCode.JMP, zero_number, label));
					}
					
					if(a instanceof Reg) {
						list.add(new SpookyInst(OpCode.MOV, return_valu, generate(base, (Reg)a)));
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
		
//		if(block.insts.isEmpty()) {
//			System.out.println(block.getDataName());
//		}
		
//		for(SpookyInst inst : block.insts) {
//			System.out.println("   " + inst);
//		}
	}
	
	private boolean isExternal(SpookyContainer base, FunctionLabel label) {
		IRProgram program = base.func.getProgram();
		
		for(IRFunction func : program.getFunctions()) {
			if(func.getName().equals(label.getName())) {
				return func.length() == 0;
			}
		}
		
		return false;
	}
	
	private Address generate(SpookyContainer base, Reg param) {
		if(param.isGenerated) {
			return Address.create(0, 16 + base.id * 16 + param.getIndex());
		}
		
		return Address.create(-1, param.getIndex() + 2);
	}
}
