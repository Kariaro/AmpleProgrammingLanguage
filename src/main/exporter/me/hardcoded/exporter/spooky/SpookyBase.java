package me.hardcoded.exporter.spooky;

import java.util.*;

import me.hardcoded.compiler.instruction.IRInstruction;
import me.hardcoded.compiler.instruction.IRProgram;
import me.hardcoded.compiler.instruction.Param;
import me.hardcoded.compiler.instruction.Param.FunctionLabel;
import me.hardcoded.compiler.instruction.Param.LabelParam;
import me.hardcoded.compiler.instruction.Param.RegParam;

class SpookyBase {
	final List<SpookyFunction> list = new ArrayList<>();
	final IRProgram program;
	
	public SpookyBase(IRProgram program) {
		this.program = program;
	}
	
	public SpookyFunction getFunction(FunctionLabel label) {
		for(SpookyFunction item : list) {
			if(item.func == null) continue;
			
			if(item.func.getName().equals(label.getName())) {
				return item;
			}
		}
		
		throw new NullPointerException("Function '" + label + "' was not found");
	}
	
	public boolean isExternal(FunctionLabel label) {
		return getFunction(label).id < 0;
	}
	
	public int getLabelIndex(SpookyFunction func, LabelParam label) {
		if(label instanceof FunctionLabel) {
			return getFunction((FunctionLabel)label).id;
		}
		
		int index = func.func_offset;
		for(SpookyBlock block : func.blocks) {
			if(block.isLabelBlock()) {
				if(block.getDataName().equals(label.toString())) {
					return index;
				}
			} else {
				index += block.insts.size();
			}
		}
		
		throw new IllegalArgumentException("Cound not find the label '" + label + "'");
	}
	
	public int getNextInstruction(SpookyFunction func, SpookyInst inst) {
		int index = 0;
		
		for(SpookyFunction item : list) {
			for(SpookyBlock block : item.blocks) {
				for(SpookyInst i : block.insts) {
					if(i == inst) return index;
					index++;
				}
			}
		}
		
		throw new IllegalArgumentException("Cound not find the instruction '" + inst + "'");
	}

	public int calculateStack(SpookyFunction func) {
		Set<Integer> set = new HashSet<>();
		
		for(SpookyBlock block : func.blocks) {
			for(IRInstruction ir : block.list) {
				for(Param param : ir.getParams()) {
					if(param instanceof RegParam) {
						
						if(!set.contains(param.getIndex())) {
							set.add(param.getIndex());
						}
					}
				}
			}
		}
		
		// [ RetAddr, Params, Stack ]
		return set.size();
	}
}
