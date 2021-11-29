package hardcoded.exporter.x86;

import java.util.*;

import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.instruction.IRFunction;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.compiler.instruction.Param;
import hardcoded.compiler.instruction.Param.*;

class AsmFunction {
	private AssemblyCodeGenerator parent;
	private int[] param_offset;
	private String[] param_names;
	private int[] stack_offset;
	private int stack_size;
	private int param_size;
	
	public AsmFunction(AssemblyCodeGenerator parent, IRFunction func) {
		this.parent = parent;
		param_offset = new int[func.getNumParams()];
		param_names = func.getParamNames();
		
		for(int i = 0, offset = 16; i < func.getNumParams(); i++) {
			if(i < 4) {
				param_offset[i] = -8 * (i + 1);
			} else {
				offset += 8;
				param_offset[i] = offset;
				param_size += 8;
			}
		}
		
		int extra_size = Math.min(4, func.getNumParams()) * 8;
		
		{
			int size = 0;
			Map<Integer, RegParam> map = new HashMap<>();
			for(IRInstruction inst : func.getInstructions()) {
				List<Param> list = inst.getParams();
				
				for(Param param : list) {
					if(param instanceof RegParam) {
						RegParam regParam = (RegParam)param;
						int regIndex = regParam.getIndex();
						if(regParam.isTemporary()) {
							if(!map.containsKey(regIndex)) {
								map.put(regIndex, regParam);
								size += regParam.getSize().size();
							}
						}
					}
				}
			}
			
			stack_offset = new int[map.size()];
			// TODO: This should be easier to do. Save the stack and param's inside the IRFunction directly...
			for(int i = 0, offset = 0; i < map.size(); i++) {
				RegParam param = map.get(i);
				offset += param.getSize().size();
				stack_offset[i] = -(offset + extra_size);
			}
			
			stack_size = extra_size + size;
		}
	}
	
	public int get_stack_size() {
		return stack_size;
	}
	
	public int get_param_size() {
		return param_size;
	}
	
	public int get_param_offset(int i) {
		return param_offset[i];
	}
	
	public String get_param_name(int i) {
		return param_names[i];
	}
	
	public int get_stack_offset(Param param) {
		if(param instanceof RegParam) {
			RegParam regParam = (RegParam)param;
			if(regParam.isTemporary()) {
				return stack_offset[regParam.getIndex()];
			} else {
				return param_offset[regParam.getIndex()];
			}
		}
		
		throw new UnsupportedOperationException();
	}

	
	public AsmOpr get_asm_opr(Param param) {
		return get_asm_opr(param.getSize(), param);
	}
	
	public AsmOpr get_asm_opr(LowType size, Param param) {
		if(param instanceof NumParam) {
			return new OprBuilder().imm(((NumParam)param).getValue());
		} else if(param instanceof RegParam) {
			return new OprBuilder().reg(RegisterX86.RBP).add().num(get_stack_offset(param)).ptr(size.size() * 8);
		} else if(param instanceof RefParam) {
			RefParam refParam = (RefParam)param;
			
			switch(refParam.label) {
				case ".data.strings" -> {
					int memory_index = parent.string_index_ordinal.get(refParam.index);
					return new OprBuilder().imm(memory_index);
				}
			}
		}
		
		throw new UnsupportedOperationException(param.getClass() + ", " + param);
	}

	public Map<String, List<Integer>> test_map = new HashMap<>();
	public Map<Integer, String> test_label_map = new HashMap<>();
	public void add_label(int index, LabelParam param) {
		List<Integer> list = test_map.computeIfAbsent(param.getName(), (i) -> new ArrayList<>());
		list.add(index);
	}

	public void set_label(LabelParam param, int index) {
		test_label_map.put(index, param.getName());
	}
}
