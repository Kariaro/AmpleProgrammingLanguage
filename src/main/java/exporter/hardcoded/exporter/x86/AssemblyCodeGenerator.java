package hardcoded.exporter.x86;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import hardcoded.assembly.x86.*;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.instruction.*;
import hardcoded.compiler.instruction.Param.*;
import hardcoded.exporter.impl.CodeGeneratorImpl;
import hardcoded.utils.StringUtils;

public class AssemblyCodeGenerator implements CodeGeneratorImpl {
	public AssemblyCodeGenerator() {
		
	}
	
	public void reset() {
		
	}
	
	protected Map<String, Integer> string_ordinal;
	protected Map<Integer, Integer> string_index_ordinal;
	protected Map<IRFunction, Integer> function_ordinal;
	protected Map<String, IRFunction> function_string_ordinal;
	protected Map<Integer, Integer> function_index_ordinal;
	protected int function_header_offset = 0;
	protected int function_trunc_offset = 0;
	protected int string_header_offset = 0;
	protected int code_offset = 0;
	
	public byte[] generate(IRProgram program) {
		try {
			byte[] result = generate2(program);
			
			System.out.println(StringUtils.printHexString("", result));
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
	
	public byte[] generate2(IRProgram program) throws Exception {
		System.out.println("\nInside the asm code generator");
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(0x100000);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		function_ordinal = new HashMap<>();
		function_string_ordinal = new HashMap<>();
		function_index_ordinal = new HashMap<>();
		string_ordinal = new HashMap<>();
		string_index_ordinal = new HashMap<>();
		
		// The code should first create a string region
		string_header_offset = byteBuffer.position();
		for(String str : program.getContext().getStrings()) {
			int ordinal = string_ordinal.size();
			int ordinal_index = byteBuffer.position();
			string_ordinal.put(str, ordinal);
			string_index_ordinal.put(ordinal, ordinal_index);
			
			// Write string
			byteBuffer.put(str.getBytes());
			byteBuffer.put((byte)0);
			byteBuffer.position((byteBuffer.position() + 3) & (~3));
		}
		
		// Align the stream
		byteBuffer.position((byteBuffer.position() + 15) & (~15));
		
		// The code should then create function region
		function_header_offset = byteBuffer.position();
		for(IRFunction func : program.getFunctions()) {
			int ordinal = function_ordinal.size();
			function_ordinal.put(func, ordinal);
			function_string_ordinal.put(func.getName(), func);
			
			// Zero initialize the function
			byteBuffer.putLong(0x00000000_000000E9L);
			
			// Align the stream
			byteBuffer.position((byteBuffer.position() + 7) & (~7));
		}
		
		// Align the stream
		byteBuffer.position((byteBuffer.position() + 15) & (~15));
		
		// The code that gives relative jump instructions
		
		// The code should then compile functions
		code_offset = byteBuffer.position();
		
		System.out.printf("String   header: 0x%08x\n", string_header_offset);
		System.out.printf("Function header: 0x%08x\n", function_header_offset);
		System.out.printf("Code     offset: 0x%08x\n\n", code_offset);
		
		// Start with looping trough each function
		for(IRFunction func : program.getFunctions()) {
			int function_offset = byteBuffer.position();
			// Write the function index to the function ordinal list
			int inst_offset = function_header_offset + (function_ordinal.get(func) * 8);
			byteBuffer.putInt(inst_offset + 1, function_offset - inst_offset - 5);
			
			// Start compiling the function fully
			byte[] compiled_code = compileFunction(func, program, function_offset);
			byteBuffer.put(compiled_code);
			byteBuffer.position((byteBuffer.position() + 15) & (~15));
		}
		
		byte[] result_array = new byte[byteBuffer.position()];
		byteBuffer.rewind();
		byteBuffer.get(result_array);
		return result_array;
	}
	
	private byte[] compileFunction(IRFunction func, IRProgram program, int memory_offset) {
		System.out.printf("\nCompiling function: %s\n", func.getName());
		
		// A function always has parameters
		// These parameters should be saved as [rbp - parameter_location]
		// Know that [rbp - 0] is the return address of the function
		AsmFunction asmFunc = new AsmFunction(this, func);
		
		if(func.getNumParams() > 0) {
			System.out.printf("Parameters:\n");
			for(int i = 0; i < func.getNumParams(); i++) {
				System.out.printf("    [rsp + 0x%x] @%s;\n", asmFunc.get_param_offset(i), asmFunc.get_param_name(i));
			}
		}
		
		int stack_size = asmFunc.get_stack_size();
		
		System.out.printf("Stack size: %d\n", stack_size);
		System.out.printf("IRcode:\n-------------------------\n%s\n-------------------------\n", IRUtils.printPretty(func));
		
		// Split all ir instructions into different groups.
		// The groups should be split when there is a branch instruction.
		// Start the function by adding the stack space initialization.
		
		List<AsmInst> list = new ArrayList<>();
		list.addAll(List.of(
			Assembly.getInstruction("push rbp"),
			Assembly.getInstruction("mov rbp, rsp"),
			Assembly.getInstruction("sub rsp, 0x%x".formatted(stack_size))
		));
		
		{
			String[] regs = { "RCX", "RDX", "R8", "R9" };
			int len = Math.min(4, func.getNumParams());
			for(int i = 0; i < len; i++) {
				list.add(Assembly.getInstruction("mov qword[rbp + %d], %s".formatted(asmFunc.get_param_offset(i), regs[i])));
			}
		}
		
		for(IRInstruction inst : func.getInstructions()) {
			// For all instructions when you find a computation convert it into the correct assembly.
			switch(inst.type()) {
				case mov -> {
					RegParam op0 = (RegParam)inst.getParam(0);
					Param op1 = inst.getParam(1);
					
					RegisterX86 reg = pickRegister(RegisterX86.RDX, op0.getSize());
					AsmOpr reg_opr = new OprBuilder().reg(reg).get();
					
					if(op1 instanceof NumParam
					|| op1 instanceof RefParam) {
						// Move the content of reg into op0
						list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(op0), asmFunc.get_asm_opr(inst.getSize(), op1)));
					} else {
						// Move the content of op1 into reg
						list.add(Assembly.getInstruction(AsmMnm.MOV, reg_opr, asmFunc.get_asm_opr(inst.getSize(), op1)));
						
						// Move the content of reg into op0
						list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(op0), reg_opr));
					}
				}
				
				case read -> {
					RegParam op0 = (RegParam)inst.getParam(0);
					Param op1 = inst.getParam(1);
					
					AsmOpr rbx_opr = new OprBuilder().reg(RegisterX86.RBX).get();
					AsmOpr rbx_ptr_opr = new OprBuilder().reg(RegisterX86.RBX).ptr(inst.getSize().size() * 8);
					AsmOpr rbx_dta_opr = new OprBuilder().reg(pickRegister(RegisterX86.RBX, inst.getSize())).get();
					// op1 holds a value and is a pointer
					
					// Write op1 to RBX
					list.add(Assembly.getInstruction(AsmMnm.MOV, rbx_opr, asmFunc.get_asm_opr(op1)));
					// Write [RBX] to op0
					list.add(Assembly.getInstruction(AsmMnm.MOV, rbx_dta_opr, rbx_ptr_opr));
					list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(op0), rbx_dta_opr));
				}
				
				case write -> {
					RegParam op0 = (RegParam)inst.getParam(0);
					Param op1 = inst.getParam(1);
					
					AsmOpr rbx_opr = new OprBuilder().reg(RegisterX86.RBX).get();
					
					LowType container_size = inst.getSize().nextLowerPointer();
					AsmOpr rbx_ptr_opr = new OprBuilder().reg(RegisterX86.RBX).ptr(container_size.size() * 8);
					AsmOpr rdx_opr = new OprBuilder().reg(pickRegister(RegisterX86.RDX, container_size)).get();
					
					// op1 holds a value and is a pointer
					
					// Write op0 to RBX
					list.add(Assembly.getInstruction(AsmMnm.MOV, rbx_opr, asmFunc.get_asm_opr(op0)));
					// Write op1 to RDX with the correct size
					list.add(Assembly.getInstruction(AsmMnm.MOV, rdx_opr, asmFunc.get_asm_opr(container_size, op1)));
					list.add(Assembly.getInstruction(AsmMnm.MOV, rbx_ptr_opr, rdx_opr));
				}
				
				case add, sub, and, or, xor -> {
					RegParam op0 = (RegParam)inst.getParam(0);
					RegParam op1 = (RegParam)inst.getParam(1);
					Param op2 = inst.getParam(2);
					
					RegisterX86 reg = pickRegister(RegisterX86.RDX, op0.getSize());
					AsmOpr rdx_opr = new OprBuilder().reg(reg).get();
					AsmMnm asm_mnm = convert_to_asm(inst.type());
					
					// Move the content of op1 into reg
					list.add(Assembly.getInstruction(AsmMnm.MOV, rdx_opr, asmFunc.get_asm_opr(inst.getSize(), op1)));
					list.add(Assembly.getInstruction(asm_mnm, rdx_opr, asmFunc.get_asm_opr(inst.getSize(), op2)));
					
					// Move the content of reg into op0
					list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(op0), rdx_opr));
				}
				
				case label -> {
					asmFunc.set_label((LabelParam)inst.getParam(0), list.size());
				}
				
				case brz -> {
					RegParam param = (RegParam)inst.getParam(0);
					LabelParam label = (LabelParam)inst.getParam(1);

					RegisterX86 reg = pickRegister(RegisterX86.RAX, param.getSize());
					AsmOpr rax_opr = new OprBuilder().reg(reg).get();
					list.add(Assembly.getInstruction(AsmMnm.MOV, rax_opr, asmFunc.get_asm_opr(param.getSize(), param)));
					list.add(Assembly.getInstruction(AsmMnm.TEST, rax_opr, rax_opr));
					list.add(Assembly.getInstruction("jz 0x10000000"));
					asmFunc.add_label(list.size() - 1, label);
				}
				
				case bnz -> {
					RegParam param = (RegParam)inst.getParam(0);
					LabelParam label = (LabelParam)inst.getParam(1);
					
					RegisterX86 reg = pickRegister(RegisterX86.RAX, param.getSize());
					AsmOpr rax_opr = new OprBuilder().reg(reg).get();
					list.add(Assembly.getInstruction(AsmMnm.MOV, rax_opr, asmFunc.get_asm_opr(param.getSize(), param)));
					list.add(Assembly.getInstruction(AsmMnm.TEST, rax_opr, rax_opr));
					list.add(Assembly.getInstruction("jnz 0x10000000"));
					asmFunc.add_label(list.size() - 1, label);
				}
				
				case br -> {
					LabelParam label = (LabelParam)inst.getParam(0);
					list.add(Assembly.getInstruction("jmp 0x10000000"));
					asmFunc.add_label(list.size() - 1, label);
				}
				
				case eq, neq, lt, lte, gt, gte -> {
					RegParam op0 = (RegParam)inst.getParam(0);
					RegParam op1 = (RegParam)inst.getParam(1);
					Param op2 = inst.getParam(2);
					
					AsmOpr rbx_opr = new OprBuilder().reg(pickRegister(RegisterX86.RBX, inst.getSize())).get();
					AsmOpr rax_opr = new OprBuilder().reg(pickRegister(RegisterX86.RAX, inst.getSize())).get();
					AsmOpr al_opr = new OprBuilder().reg(RegisterX86.AL).get();
					AsmMnm asm_mnm = convert_to_asm(inst.type());
					
					list.add(Assembly.getInstruction(AsmMnm.MOV, rbx_opr, asmFunc.get_asm_opr(op1)));
					list.add(Assembly.getInstruction("xor rax, rax"));
					list.add(Assembly.getInstruction(AsmMnm.CMP, rbx_opr, asmFunc.get_asm_opr(inst.getSize(), op2)));
					list.add(Assembly.getInstruction(asm_mnm, al_opr));
					list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(op0), rax_opr));
				}
				
				case mul -> {
					RegParam op0 = (RegParam)inst.getParam(0);
					RegParam op1 = (RegParam)inst.getParam(1);
					Param op2 = inst.getParam(2);
					
					// Clear rax and rdx, rbx
					list.add(Assembly.getInstruction("xor rax, rax"));
					list.add(Assembly.getInstruction("xor rbx, rbx"));

					AsmOpr rax_opr = new OprBuilder().reg(pickRegister(RegisterX86.RAX, inst.getSize())).get();
					AsmOpr rbx_opr = new OprBuilder().reg(pickRegister(RegisterX86.RBX, inst.getSize())).get();
					
					// Write op1 to rax
					list.add(Assembly.getInstruction(AsmMnm.MOV, rax_opr, asmFunc.get_asm_opr(op1)));
					// Write op2 to rbx
					list.add(Assembly.getInstruction(AsmMnm.MOV, rbx_opr, asmFunc.get_asm_opr(op2)));
					// Multiply and store in EDX, EAX
					list.add(Assembly.getInstruction(AsmMnm.MUL, rbx_opr));
					// Store in output register
					list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(op0), rax_opr));
				}
				
				case div, mod -> {
					RegParam op0 = (RegParam)inst.getParam(0);
					RegParam op1 = (RegParam)inst.getParam(1);
					Param op2 = inst.getParam(2);
					
					// Clear rax and rdx, rbx
					list.add(Assembly.getInstruction("xor rax, rax"));
					list.add(Assembly.getInstruction("xor rdx, rdx"));
					list.add(Assembly.getInstruction("xor rbx, rbx"));

					AsmOpr rax_opr = new OprBuilder().reg(pickRegister(RegisterX86.RAX, inst.getSize())).get();
					AsmOpr rbx_opr = new OprBuilder().reg(pickRegister(RegisterX86.RBX, inst.getSize())).get();
					AsmOpr rdx_opr = new OprBuilder().reg(pickRegister(RegisterX86.RDX, inst.getSize())).get();
					
					// Write op1 to rax
					list.add(Assembly.getInstruction(AsmMnm.MOV, rax_opr, asmFunc.get_asm_opr(op1)));
					// Write op2 to rbx
					list.add(Assembly.getInstruction(AsmMnm.MOV, rbx_opr, asmFunc.get_asm_opr(op2)));
					// Divide and store in EDX, EAX
					list.add(Assembly.getInstruction(AsmMnm.DIV, rbx_opr));
					// Store in output register
					switch(inst.type()) {
						case div -> {
							list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(op0), rax_opr));
						}
						case mod -> {
							list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(op0), rdx_opr));
						}
					}
				}
				
				case ret -> {
					AsmOpr rax_opr = new OprBuilder().reg(pickRegister(RegisterX86.RAX, inst.getSize())).get();
					list.add(Assembly.getInstruction("xor rax, rax"));
					list.add(Assembly.getInstruction(AsmMnm.MOV, rax_opr, asmFunc.get_asm_opr(inst.getParam(0))));
					list.add(Assembly.getInstruction("pop rbp"));
					list.add(Assembly.getInstruction("retn"));
				}
				
				case call -> {
					// push rbp
					// mov rbp, rsp
					// sub rsp, <stack_size>
					
					// Parameters
					// [Stack:0x0] Return address
					// [Stack:0x8] param_1
					//    ....
					// [Stack:0x10] stack 
					
					FunctionLabel op1 = (FunctionLabel)inst.getParam(1);
					IRFunction ir_func = function_string_ordinal.get(op1.getName());
					int ref_func = function_ordinal.get(ir_func);

					RegisterX86[] regs = { RegisterX86.RCX, RegisterX86.RDX, RegisterX86.R8, RegisterX86.R9 };
					int pushed = 0;
					for(int i = ir_func.getNumParams() - 1, j = 0; i >= 0; i--, j++) {
						Param param = inst.getParam(i + 2);
						
						LowType target_size = ir_func.getParams()[i];
						if(j < 4) {
							RegisterX86 reg = pickRegister(regs[j], target_size);
							list.add(Assembly.getInstruction(AsmMnm.MOV, new OprBuilder().reg(reg).get(), asmFunc.get_asm_opr(param)));
							continue;
						}
						
						AsmOpr rax_low_opr = new OprBuilder().reg(pickRegister(RegisterX86.RAX, target_size)).get();
						AsmOpr rax_opr;
						
						if(target_size.size() == 1
						|| target_size.size() == 4) {
							rax_opr = new OprBuilder().reg(pickRegister(RegisterX86.RAX, target_size.size() << 1)).get();
							pushed += target_size.size() << 1;
						} else {
							rax_opr = new OprBuilder().reg(pickRegister(RegisterX86.RAX, target_size)).get();
							pushed += target_size.size();
						}
						
						list.add(Assembly.getInstruction("xor rax, rax"));
						list.add(Assembly.getInstruction(AsmMnm.MOV, rax_low_opr, asmFunc.get_asm_opr(param)));
						list.add(Assembly.getInstruction(AsmMnm.PUSH, rax_opr));
					}
					
					list.add(Assembly.getInstruction(AsmMnm.CALL, new OprBuilder().imm(ref_func)));
					if(pushed != 0) {
						list.add(Assembly.getInstruction("add esp, %d".formatted(pushed)));
					}

					Param op0 = inst.getParam(0);
					
					if(op0 != Param.NONE) {
						// Return value is always in ax
						AsmOpr rax_opr = new OprBuilder().reg(pickRegister(RegisterX86.RAX, inst.getSize())).get();
						list.add(Assembly.getInstruction(AsmMnm.MOV, asmFunc.get_asm_opr(inst.getParam(0)), rax_opr));
					}
				}
				
				default -> {
					System.out.println("missing: " + inst);
					list.add(Assembly.getInstruction("nop"));
				}
			}
		}
		
		if(list.isEmpty() || list.get(list.size() - 1).getMnemonic() != AsmMnm.RETN) {
			list.add(Assembly.getInstruction("pop rbp"));
			list.add(Assembly.getInstruction("retn"));
		}
		
		Map<Integer, Integer> jump_indexes = new HashMap<>();
		Map<Integer, Integer> label_indexes = new HashMap<>();

		ByteBuffer buffer = ByteBuffer.allocate(0x10000);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		for(int i = 0; i < list.size(); i++) {
			System.out.printf("inst: %3d: %s\n", i, list.get(i));
		}
		
		int longest = 0;
		for(int i = 0; i < list.size(); i++) {
			AsmInst inst = list.get(i);
			String labelStr = asmFunc.test_label_map.get(i);
			if(labelStr != null) {
				label_indexes.put(i, buffer.position());
			}
			
			int func_ref = 0;
			switch(inst.getMnemonic()) {
				case CALL -> {
					func_ref = (int)inst.getOperand(0).getImmediate(0);
					list.set(i, Assembly.getInstruction(AsmMnm.CALL, new OprBuilder().imm(0x10000000)));
					buffer.put((byte)0xE8);
					buffer.putInt(0);
				}
				default -> {
					int[] opcode = Assembly.compile(inst);
					if(opcode == null) {
						System.out.printf("Could not compile: [index: %d][%s]\n", i, inst);
					}
					
					for(int opcodeByte : opcode)
						buffer.put((byte)opcodeByte);
					
				}
			}
			
			switch(inst.getMnemonic()) {
				case JZ, JNZ, JMP -> {
					jump_indexes.put(i, buffer.position());
				}
				case CALL -> {
					int inst_offset = buffer.position() + memory_offset;
					buffer.putInt(buffer.position() - 4, (function_header_offset + func_ref * 8) - inst_offset);
				}
			}
			
			longest = buffer.position();
		}
		
		for(Integer line : label_indexes.keySet()) {
			String label_name = asmFunc.test_label_map.get(line);
			int label_offset = label_indexes.get(line);
			
			List<Integer> overwrite_lines = asmFunc.test_map.get(label_name);
			for(Integer overwrite : overwrite_lines) {
				int jump_offset = jump_indexes.get(overwrite);
				
				buffer.position(jump_offset - 4);
				buffer.putInt(label_offset - jump_offset);
			}
		}
		
		System.out.println(asmFunc.test_map);
		
		byte[] result_array = new byte[longest];
		System.arraycopy(buffer.array(), 0, result_array, 0, result_array.length);
		return result_array;
	}
	
	private AsmMnm convert_to_asm(IRType type) {
		switch(type) {
			case add -> { return AsmMnm.ADD; }
			case sub -> { return AsmMnm.SUB; }
			case and -> { return AsmMnm.AND; }
			case xor -> { return AsmMnm.XOR; }
			case or -> { return AsmMnm.OR; }

			case eq -> { return AsmMnm.SETE; }
			case neq -> { return AsmMnm.SETNE; }
			case gt -> { return AsmMnm.SETG; }
			case gte -> { return AsmMnm.SETGE; }
			case lt -> { return AsmMnm.SETL; }
			case lte -> { return AsmMnm.SETLE; }
		}
		
		throw new UnsupportedOperationException();
	}
	
	private RegisterX86 pickRegister(RegisterX86 reg, LowType type) {
		switch(type.size() * 8) {
			case 8: return RegisterX86.get(RegisterType.r8, reg.index);
			case 16: return RegisterX86.get(RegisterType.r16, reg.index);
			case 32: return RegisterX86.get(RegisterType.r32, reg.index);
			case 64: return RegisterX86.get(RegisterType.r64, reg.index);
		}
		
		return null;
	}
	
	private RegisterX86 pickRegister(RegisterX86 reg, int size) {
		switch(size * 8) {
			case 8: return RegisterX86.get(RegisterType.r8, reg.index);
			case 16: return RegisterX86.get(RegisterType.r16, reg.index);
			case 32: return RegisterX86.get(RegisterType.r32, reg.index);
			case 64: return RegisterX86.get(RegisterType.r64, reg.index);
		}
		
		return null;
	}
}
