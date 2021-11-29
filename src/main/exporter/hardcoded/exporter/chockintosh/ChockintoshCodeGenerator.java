package hardcoded.exporter.chockintosh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hardcoded.compiler.impl.ICodeGenerator;
import hardcoded.compiler.instruction.*;
import hardcoded.compiler.instruction.Param.*;
import hardcoded.utils.error.CodeGenException;

public class ChockintoshCodeGenerator implements ICodeGenerator {
	private static final Logger LOGGER = LogManager.getLogger(ChockintoshCodeGenerator.class);
	
	@Override
	public byte[] getBytecode(IRProgram program) throws CodeGenException {
		List<IRFunction> functions = program.getFunctions();
		
		IRFunction main = null;
		for(IRFunction func : functions) {
			switch(func.getName()) {
				case "main" -> {
					main = func;
				}
				case "_set_output", "_read_input", "_halt" -> {
					// Do nothing.
				}
				default -> {
					throw new CodeGenException("Chockintosh does not support multiple.");
				}
			}
		}
		
		if(main == null) {
			throw new CodeGenException("main function was not defined.");
		}
		
		if(main.getNumParams() != 0) {
			throw new CodeGenException("Chockintosh does not support main function parameters.");
		}
		
//		LOGGER.info(main);
//		LOGGER.info("");
//		LOGGER.info("Compiling function: {}", main.getName());
//		LOGGER.info("IRCode:");
//		LOGGER.info("-------------------------");
//		LOGGER.info("\n{}", IRPrintUtils.printPretty(main));
//		LOGGER.info("-------------------------");
		
		byte[] code = new byte[0];
		try {
			code = compileFunction(main);
		} catch(CodeGenException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
//		LOGGER.info("Printing code: ({} bytes)", code.length);
//		LOGGER.info("\n{}", printCode(code));
		
		return code;
	}
	
	@Override
	public byte[] getAssembler(IRProgram program) throws CodeGenException {
		return printCode(getBytecode(program)).getBytes();
	}
	
	private String printCode(byte[] code) {
		List<String> assembler = new ArrayList<>();
		List<Integer> jumps = new ArrayList<>();
		Set<Integer> unique_jumps = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < code.length; i++) {
			byte inst = code[i];
			
			OpCode op = OpCode.get(inst & 15);
			int reg = (inst >>> 4) & 15;
			
			if(op != null) {
				switch(op) {
					case ADD, SUB, XOR, OR, AND, BSP, BSM, HALT -> {
						assembler.add(op.toString());
					}
					
					case LDL, SIF, GOTO -> {
						assembler.add("");
						byte value = code[++i];
						assembler.add("%s %d".formatted(op, Byte.toUnsignedInt(value)));
						if(op == OpCode.SIF || op == OpCode.GOTO) {
							int dst = Byte.toUnsignedInt(value);
							if(unique_jumps.add(dst)) {
								jumps.add(dst);
							}
						}
					}
					
					default -> {
						assembler.add("%s %s".formatted(op, Reg.getName(reg)));
					}
				}
			} else {
				assembler.add("<null> (%s)".formatted(toBinary(8, inst)));
			}
		}
		
		jumps.sort(null);
		
		for(int i = 0, len = jumps.size(); i < len; i++) {
			int index = jumps.get(i);
			assembler.add(index + i, "// %d:".formatted(index));
		}
		
		for(String str : assembler) {
			if(!str.isEmpty()) {
				sb.append(str).append("\n");
			}
		}
		
		return sb.toString().trim();
	}
	
	private String toBinary(int bits, long value) {
		return ("%" + bits + "s").formatted(Long.toBinaryString(value)).replace(' ', '0');
	}

	@Override
	public void reset() {
		
	}
	
	private byte[] compileFunction(IRFunction func) throws IOException, CodeGenException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();

		int label_index = 0;
		List<JumpLabel> label_list = new ArrayList<>();
		List<Jump> jump_list = new ArrayList<>();
		
		for(IRInstruction inst : func.getInstructions()) {
			switch(inst.type()) {
				case add, sub, and, xor, or, shr, shl -> { 
					RegParam dst = (RegParam)inst.getParam(0);
					RegParam op0 = (RegParam)inst.getParam(1);
					Param op1 = inst.getParam(2);
					
					bs.write(createWide(OpCode.LDL, op0.getIndex())); // w = <reg>
					bs.write(createInst(OpCode.SDR, Reg.RAMA));       // ram_addr = <reg>
					bs.write(createInst(OpCode.LDR, Reg.RAM));        // w = *(<reg>)
					bs.write(createInst(OpCode.SDR, Reg.AL2));        // AL2 = *(<reg>)
					
					if(op1 instanceof NumParam numParam) {
						bs.write(createWide(OpCode.LDL, numParam.getValue())); // w = <num>
					} else if(op1 instanceof RegParam regParam) {
						bs.write(createWide(OpCode.LDL, regParam.getIndex())); // w = <reg>
						bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <reg>
						bs.write(createInst(OpCode.LDR, Reg.RAM));             // w = *(<reg>)
					}
					
					// w = w <op> AL2
					switch(inst.type()) {
						case add -> bs.write(createInst(OpCode.ADD));
						case sub -> bs.write(createInst(OpCode.SUB));
						case xor -> bs.write(createInst(OpCode.XOR));
						case and -> bs.write(createInst(OpCode.AND));
						case or -> bs.write(createInst(OpCode.OR));
						case shr -> bs.write(createInst(OpCode.BSP));
						case shl -> bs.write(createInst(OpCode.BSM));
					}
					
					if(inst.type() == IRType.shr || inst.type() == IRType.shl) {
						if(op1 instanceof NumParam numParam) {
							if(numParam.getValue() != 1) {
								throw new CodeGenException("Shift does not allow shifts with values other than 1.");
							}
						} else {
							throw new CodeGenException("Shift does not allow non number shifts.");
						}
					}
					
					bs.write(createInst(OpCode.SDR, Reg.AL2));        // AL2 = (w <op> AL2)
					bs.write(createWide(OpCode.LDL, dst.getIndex())); // w = <dst>
					bs.write(createInst(OpCode.SDR, Reg.RAMA));       // ram_addr = <dst>
					bs.write(createWide(OpCode.LDL, 0));              // w = 0
					bs.write(createInst(OpCode.OR));                  // w = (w <op> AL2)
					bs.write(createInst(OpCode.SDR, Reg.RAM));        // *(<dst>) = (w <op> AL2)
					// w = w + AL2
				}
				case neg -> {
					RegParam dst = (RegParam)inst.getParam(0);
					Param op0 = inst.getParam(1);
					
					if(op0 instanceof NumParam numParam) {
						bs.write(createWide(OpCode.LDL, numParam.getValue())); // w = <num>
					} else if(op0 instanceof RegParam regParam) {
						bs.write(createWide(OpCode.LDL, regParam.getIndex())); // w = <reg>
						bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <reg>
						bs.write(createInst(OpCode.LDR, Reg.RAM));             // w = *(<reg>)
					}
					
					bs.write(createInst(OpCode.SDR, Reg.AL2));        // AL2 = w
					bs.write(createWide(OpCode.LDL, 0));              // w = 0
					bs.write(createInst(OpCode.SUB));                 // w = -AL2
					bs.write(createInst(OpCode.SDR, Reg.AL2));        // AL2 = w
					
					bs.write(createWide(OpCode.LDL, dst.getIndex())); // w = <dst>
					bs.write(createInst(OpCode.SDR, Reg.RAMA));       // ram_addr = <dst>
					bs.write(createWide(OpCode.LDL, 0));              // w = 0
					bs.write(createInst(OpCode.OR));                  // w = -AL2
					bs.write(createInst(OpCode.SDR, Reg.RAM));        // *(<dst>) = -AL2
					// w = -AL2
				}
				case nor -> {
					RegParam dst = (RegParam)inst.getParam(0);
					Param op0 = inst.getParam(2);
					
					if(op0 instanceof NumParam numParam) {
						bs.write(createWide(OpCode.LDL, numParam.getValue())); // w = <num>
					} else if(op0 instanceof RegParam regParam) {
						bs.write(createWide(OpCode.LDL, regParam.getIndex())); // w = <reg>
						bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <reg>
						bs.write(createInst(OpCode.LDR, Reg.RAM));             // w = *(<reg>)
					}
					
					bs.write(createInst(OpCode.NOT));                 // w = ~w
					bs.write(createInst(OpCode.SDR, Reg.AL2));        // AL2 = ~w
					bs.write(createWide(OpCode.LDL, dst.getIndex())); // w = <dst>
					bs.write(createInst(OpCode.SDR, Reg.RAMA));       // ram_addr = <dst>
					bs.write(createWide(OpCode.LDL, 0));              // w = 0
					bs.write(createInst(OpCode.OR));                  // w = ~w
					bs.write(createInst(OpCode.SDR, Reg.RAM));        // *(<dst>) = ~w
					// w = ~AL2
				}
				case read, write, mov -> { // mem[R0] = mem[R1]
					RegParam dst = (RegParam)inst.getParam(0);
					Param op0 = inst.getParam(1);
					
					if(op0 instanceof NumParam numParam) {
						bs.write(createWide(OpCode.LDL, dst.getIndex()));      // w = <dst>
						bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <dst>
						bs.write(createWide(OpCode.LDL, numParam.getValue())); // w = <num>
						bs.write(createInst(OpCode.SDR, Reg.RAM));             // *(<dst>) = <num>
						
					} else if(op0 instanceof RegParam regParam) {
						bs.write(createWide(OpCode.LDL, regParam.getIndex())); // w = <reg>
						bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <reg>
						bs.write(createInst(OpCode.LDR, Reg.RAM));             // w = *(<reg>)
						bs.write(createInst(OpCode.SDR, Reg.AL2));             // AL2 = w
						bs.write(createWide(OpCode.LDL, dst.getIndex()));      // w = <dst>
						bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <dst>
						bs.write(createWide(OpCode.LDL, 0));                   // w = 0
						bs.write(createInst(OpCode.OR));                       // w = (<op0>)
						bs.write(createInst(OpCode.SDR, Reg.RAM));             // *(<dst>) = <op0>
					}
				}
				case call -> {
					FunctionLabel op1 = (FunctionLabel)inst.getParam(1);
					
					switch(op1.getName()) {
						case "_read_input" -> {
							RegParam dst = (RegParam)inst.getParam(0);

							bs.write(createInst(OpCode.LDR, Reg.INP));        // w = <inp>
							bs.write(createInst(OpCode.SDR, Reg.AL2));        // AL2 = <inp>
							bs.write(createWide(OpCode.LDL, dst.getIndex())); // w = <dst>
							bs.write(createInst(OpCode.SDR, Reg.RAMA));       // ram_addr = <dst>
							bs.write(createWide(OpCode.LDL, 0));              // w = 0
							bs.write(createInst(OpCode.OR));                  // w = AL2
							bs.write(createInst(OpCode.SDR, Reg.RAM));        // *(<dst>) = <inp>
						}
						case "_set_output" -> {
							Param op0 = inst.getParam(2);
							
							if(op0 instanceof NumParam numParam) {
								bs.write(createWide(OpCode.LDL, numParam.getValue())); // w = <num>
							} else if(op0 instanceof RegParam regParam) {
								bs.write(createWide(OpCode.LDL, regParam.getIndex())); // w = <reg>
								bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <reg>
								bs.write(createInst(OpCode.LDR, Reg.RAM));             // w = *(<reg>)
							}
							bs.write(createInst(OpCode.SDR, Reg.OUT)); // <out> = w
						}
						case "_halt" -> {
							bs.write(createInst(OpCode.HALT));
						}
					}
				}
				case ret -> {
					bs.write(createInst(OpCode.HALT));
				}
				case label -> {
					label_list.add(new JumpLabel(bs.size(), ((LabelParam)inst.getParam(0)).getName()));
				}
				case br -> {
					bs.write(createWide(OpCode.GOTO, 0));
					jump_list.add(new Jump(bs.size() - 1, ((LabelParam)inst.getParam(0)).getName()));
				}
				case brz -> {
					Param op0 = inst.getParam(0);
					
					if(op0 instanceof NumParam numParam) {
						bs.write(createWide(OpCode.LDL, numParam.getValue())); // w = <num>
					} else if(op0 instanceof RegParam regParam) {
						bs.write(createWide(OpCode.LDL, regParam.getIndex())); // w = <reg>
						bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <reg>
						bs.write(createInst(OpCode.LDR, Reg.RAM));             // w = *(<reg>)
					}
					
					String label_1 = "#__label_%d".formatted(label_index++);
					String label_2 = "#__label_%d".formatted(label_index++);
					bs.write(createWide(OpCode.SIF, 0));
					jump_list.add(new Jump(bs.size() - 1, label_1));
					bs.write(createWide(OpCode.GOTO, 0));
					jump_list.add(new Jump(bs.size() - 1, label_2));
					label_list.add(new JumpLabel(bs.size(), label_1));
					bs.write(createWide(OpCode.GOTO, 0));
					jump_list.add(new Jump(bs.size() - 1, ((LabelParam)inst.getParam(1)).getName()));
					label_list.add(new JumpLabel(bs.size(), label_2));
				}
				case bnz -> {
					Param op0 = inst.getParam(0);
					
					if(op0 instanceof NumParam numParam) {
						bs.write(createWide(OpCode.LDL, numParam.getValue())); // w = <num>
					} else if(op0 instanceof RegParam regParam) {
						bs.write(createWide(OpCode.LDL, regParam.getIndex())); // w = <reg>
						bs.write(createInst(OpCode.SDR, Reg.RAMA));            // ram_addr = <reg>
						bs.write(createInst(OpCode.LDR, Reg.RAM));             // w = *(<reg>)
					}
					
					bs.write(createWide(OpCode.SIF, 0));
					jump_list.add(new Jump(bs.size() - 1, ((LabelParam)inst.getParam(1)).getName()));
				}
				
				case mod, mul, div -> {
					throw new CodeGenException("To complex instructions.");
				}
				
				default -> {
					LOGGER.info("Missing instruction: {}", inst);
				}
			}
		}
		
		byte[] code = bs.toByteArray();
		
		for(Jump jump : jump_list) {
			JumpLabel dest = null;
			for(JumpLabel label : label_list) {
				if(label.label.equals(jump.label)) {
					dest = label;
					break;
				}
			}
			
			code[jump.index] = (byte)dest.index;
		}
		
		return code;
	}
	
	private byte createInst(OpCode op) {
		return (byte)(op.code & 255);
	}
	
	private byte createInst(OpCode op, Reg register) {
		switch(op) {
			case LDL, GOTO, SIF -> {
				throw new CodeGenException("Multibyte instruction cannot use 1 byte.");
			}
		}
		
		return (byte)((((register.index & 15) << 4) | op.code) & 255);
	}
	
	private byte[] createWide(OpCode op, long value) {
		return createWide(op, 0, value);
	}
	
	private byte[] createWide(OpCode op, int index, long value) {
		return new byte[] {
			(byte)((((index & 15) << 4) | op.code) & 255),
			(byte)(value)
		};
	}
	
	private enum Reg {
		FLG(0),
		IFF(1),
		RAMA(2),
		RAM(3),
		INP(4),
		OUT(5),
		AL2(6),
		PGC(7),
		STACK(8);
		
		private int index;
		private Reg(int index) {
			this.index = index;
		}
		
		private static String getName(int reg) {
			for(Reg r : values()) {
				if(r.index == reg) {
					return r.toString();
				}
			}
			
			return "unknown(%d)".formatted(reg);
		}
	}
	
	private static class Jump {
		public int index;
		public String label;
		
		public Jump(int index, String label) {
			this.index = index;
			this.label = label;
		}
	}
	
	private static class JumpLabel {
		public int index;
		public String label;
		
		public JumpLabel(int index, String label) {
			this.index = index;
			this.label = label;
		}
	}
}
