package com.hardcoded.compiler.llcode;

import java.util.*;

import com.hardcoded.compiler.api.Instruction;
import com.hardcoded.compiler.api.Instruction.Type;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.instruction.*;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * A code optimizer
 * 
 * @author HardCoded
 * @since 0.2.0
 */
class AmpleCodeOptimizer {
	private static final Log LOGGER = Log.getLogger();
	
	public AmpleCodeOptimizer() {
		
	}
	
	
	public ImCode process(Options options, ImCode code) {
		ImCode copy = new ImCode();
		
		for(InstList list : code.list()) {
			InstList list_optimized = optimize(list.clone());
			
			//System.out.println("---------------------------");
			//LOGGER.debug("Before (%d) after (%d)", list.size(), list_optimized.size());
			
			System.out.printf(":================: %08x\n", list.hashCode());
			
			for(Inst inst : list_optimized.list()) {
				Instruction.Type type = inst.getType();
				if(type == Instruction.Type.MARKER) continue;
				if(type == Instruction.Type.LABEL) {
					System.out.printf("  %s\n", inst.getParam(0));
				} else {
					System.out.printf("    %s\n", inst);
				}
			}
			
			System.out.println("---------------------------");
			
			copy.push(list_optimized);
		}
		
		System.out.println("===============================================================================");
		System.out.println("===============================================================================");
		System.out.println("===============================================================================");
		
		{
			for(InstList list : copy.list()) {
				List<Block> split = propagateSystem(list);
				System.out.println("--------------");
				for(Block block : split) {
					System.out.printf("    :-------------- %s\n", block.states);
					InstList block_list = block.list;
					
					for(Inst inst : block_list.list()) {
						Instruction.Type type = inst.getType();
						if(type == Instruction.Type.MARKER) continue;
//						if(type == Instruction.Type.LABEL) {
//							System.out.printf("    :  %s\n", inst.getParam(0));
//						} else {
//							//System.out.printf("    :    %s\n", inst);
//						}
						String padding = type == Instruction.Type.LABEL ? "":"  ";
						System.out.printf("    :  %s%-30s\t%s\n", padding, inst.toPrettyString(), inst);
					}
				}
			}
		}
		
		for(InstList list : code.list()) {
			for(Inst inst : list.list()) {
				AmpleInstVerifier.verify(inst);
			}
		}
		
		return copy;
	}
	
	private InstList optimize(InstList list) {
		int last_size = 0;
		while(last_size != list.size()) {
			last_size = list.size();
			
			list = optimize_label(list);
			list = optimize_ret_br(list);
			list = optimize_jump(list);
			list = optimize_define(list);
			
			LOGGER.debug("Size changed from: %d to %d", last_size, list.size());
		}
		
		return list;
	}
	
	/**
	 * Unused label optimization
	 * 
	 * <p>Remove all {@code label} instructions that are not used.
	 * 
	 * @param list
	 */
	private InstList optimize_label(InstList list) {
		Set<Reference> set = new HashSet<>();
		Set<Integer> labels = new HashSet<>();
		
		for(int i = 0; i < list.size(); i++) {
			Inst inst = list.get(i);
			
			if(inst.getType() == Type.LABEL) labels.add(i);
			if(inst.getType() == Type.BNZ
			|| inst.getType() == Type.BRZ
			|| inst.getType() == Type.BR) {
				InstParam last = inst.getParam(inst.getNumParam() - 1);
				set.add(last.getReference());
			}
		}
		
		InstList next = InstList.get();
		for(Inst inst : list.list()) {
			if(inst.getType() == Type.LABEL) {
				InstParam label = inst.getParam(0);
				if(set.contains(label.getReference())) {
					next.add(inst);
				}
			} else {
				next.add(inst);
			}
		}
		
		return next;
	}
	
	/**
	 * Unused variable optimization
	 * 
	 * @param list
	 */
	private InstList optimize_define(InstList list) {
		Map<Reference, Set<Integer>> map = new HashMap<>();
		
		for(int i = 0; i < list.size(); i++) {
			Inst inst = list.get(i);
			
			for(int j = 0; j < inst.getNumParam(); j++) {
				InstParam param = inst.getParam(j);
				
				if(param.isReference()) {
					Reference ref = param.getReference();
					
					Set<Integer> set = map.get(ref);
					if(set == null) {
						set = new HashSet<>();
						map.put(ref, set);
					}
					
					set.add(i);
				}
			}
		}
		
		InstList next = InstList.get();
		
		for(int i = 0; i < list.size(); i++) {
			Inst inst = list.get(i);
			
			if(inst.getType().isVolatile()) {
				next.add(inst);
				continue;
			}
			
			boolean hasRef = false;
			boolean used = false;
			for(int j = 0; j < inst.getNumParam(); j++) {
				InstParam param = inst.getParam(j);
				
				if(param.isReference()) {
					hasRef = true;
					
					Reference ref = param.getReference();
					
					if(map.get(ref).size() > 1) {
						used = true;
						break;
					}
				}
			}
			
			if(!hasRef || used) {
				next.add(inst);
			}
		}
		
		return next;
	}
	
	/**
	 * RET/BR OPTIMIZATION
	 * 
	 * <p>Remove all instructions after a {@code ret} or a {@code br}
	 * instruction until you reach a {@code label}.
	 * 
	 * @param list
	 */
	private InstList optimize_ret_br(InstList list) {
		InstList next = InstList.get();
		boolean write = true;
		
		for(Inst inst : list.list()) {
			if(inst.getType() == Type.LABEL) write = true;
			
			if(write) {
				next.add(inst);
			}
			
			if(inst.getType() == Type.RET) write = false;
			if(inst.getType() == Type.BR) write = false;
		}
		
		return next;
	}
	
	/**
	 * Jump optimization
	 * 
	 * <p>
	 * @param list
	 */
	private InstList optimize_jump(InstList list) {
		if(list.isEmpty()) return list;
		
		InstList next = InstList.get();
		
		final int len = list.size();
		for(int i = 0; i < len; i++) {
			Inst inst = list.get(i);
			Inst inxt = list.get(Math.min(i + 1, len - 1));
			
			
			/** Convert:
			 *    br[zn] $val, label
			 *    label:
			 * ---------------------
			 *    label:
			 */
			if(inst.getType().isJump() && inxt.getType() == Type.LABEL) {
				Reference ref = inst.getParam(inst.getNumParam() - 1).getReference();
				
				if(ref.equals(inxt.getParam(0).getReference())) {
					continue;
				}
			}
			
			/** Convert:
			 *    set $val, <const>
			 *    br[zn] $val, label
			 * ---------------------
			 * If we can convert the branch to a unconditional version of itself
			 * we do that
			 * ---------------------
			 *    br label
			 */
			if(inst.getType() == Type.SET && inxt.getType().isConditionalJump()) {
				Reference ref = inst.getParam(0).getReference();
				InstParam constant = inst.getParam(inst.getNumParam() - 1);
				
				if(ref.equals(inxt.getParam(0).getReference()) && constant.isNumber()) {
					InstParam target = inxt.getParam(inxt.getNumParam() - 1);
					
					next.add(inst);
					
					if(inxt.getType() == Type.BRZ) {
						if(constant.getNumber() == 0) {
							next.add(Inst.get(Type.BR).addParam(target));
						}
					} else if(inxt.getType() == Type.BNZ) {
						if(constant.getNumber() != 0) {
							next.add(Inst.get(Type.BR).addParam(target));
						}
					}
					
					i++;
					continue;
				}
			}
			
			next.add(inst);
		}
		
		return next;
	}
	
	<T> T throw_exception(Statement stat, String format, Object... args) {
		String extra = String.format("(line: %d, column: %d) ", stat.getStartOffset(), stat.getEndOffset());
		throw new CodeGenException(extra + format, args);
	}
	
	private List<Block> propagateSystem(InstList list) {
		List<Block> blocks = splitProgram(list);
		
		
		
		return blocks;
	}
	
	private List<Block> splitProgram(InstList list) {
		List<Block> blocks = new ArrayList<>();
		
		Block block = new Block();
		
		/**
		 * ret, br, brz, bnz, label, call
		 */
		for(Inst inst : list.list()) {
			switch(inst.getType()) {
				case LABEL: {
					if(!block.isEmpty()) {
						blocks.add(block);
						block = new Block();
					}
					
					block.list.add(inst);
					break;
				}
					
				case RET:
				case BR:
				case BRZ:
				case BNZ:
				case CALL: {
					block.list.add(inst);
					blocks.add(block);
					block = new Block();
					break;
				}
				
				default: {
					block.list.add(inst);
				}
			}
		}
		
		if(!block.isEmpty()) {
			blocks.add(block);
		}
		
		return blocks;
	}
	
	/**
	 * Stores information about the state of each block.
	 * 
	 * @author HardCoded
	 * @since 0.2.0
	 */
	class State {
		final Map<Reference, Set<Object>> map;
		
		State() {
			map = new HashMap<>(); 
		}
		
		void push(Reference ref, Object value) {
			Set<Object> set = map.get(ref);
			if(set == null) {
				set = new HashSet<>();
				map.put(ref, set);
			}
			
			set.add(value);
		}
		
		@Override
		public String toString() {
			return map.toString();
		}
	}
	
	/**
	 * Stores information about what values goes into and out from this block
	 * 
	 * @author HardCoded
	 * @since 0.2.0
	 */
	class Block {
		final List<State> states;
		final InstList list;
		
		Block() {
			states = new ArrayList<>();
			list = InstList.get();
		}
		
		boolean isEmpty() {
			return list.isEmpty();
		}
		
		void push(State state) {
			states.add(state);
		}
	}
}
