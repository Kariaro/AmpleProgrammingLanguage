package com.hardcoded.compiler.llcode;

import java.util.Arrays;

import com.hardcoded.compiler.api.Instruction;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.instruction.CodeGenException;
import com.hardcoded.compiler.impl.instruction.Inst;
import com.hardcoded.compiler.impl.instruction.InstParam;
import com.hardcoded.logger.Log;

/**
 * A instruction verifier.
 * 
 * <p>A static class for verifying if instructions are typed correctly
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleInstVerifier {
	private static final Log LOGGER = Log.getLogger();
	private AmpleInstVerifier() {
		
	}
	
	
	static <T> T require(T type, Class<?> clazz) {
		if(!clazz.isInstance(type)) {
			throw new CodeGenException("Invalid parameter type. Expected '%s' but got '%s'", clazz.getSimpleName(), type);
		}
		
		return type;
	}
	
	static void require(InstParam param, InstParam.Type type) {
		if(param.getType() != type) {
			throw new CodeGenException("Invalid parameter type. Expected '%s' but got '%s'", type, param.getType());
		}
	}
	
	static boolean require_ref_types(InstParam param, Reference.Type... types) {
		if(!param.isReference()) {
			throw new CodeGenException("Invalid parameter type. Expected 'REF' but got '%s'", param.getType());
		}
		
		boolean found = false;
		Reference ref = param.getReference();
		for(Reference.Type type : types) {
			if(ref.getType() == type) {
				found = true;
				break;
			}
		}
		
		if(!found) {
			throw new CodeGenException("Invalid parameter type. Reference was not one of '%s'", Arrays.deepToString(types));
		}
		
		return found;
	}
	
	static void requireRefLabel(InstParam param) {
		require_ref_types(param, Reference.Type.LABEL);
	}
	
	static void requireRefValue(InstParam param) {
		if(param.isNumber() || param.isString()) return;
		
		require_ref_types(param, Reference.Type.VAR);
	}
	
	/**
	 * Verify a instruction.
	 * 
	 * @return the instruction
	 */
	public static Instruction verify(Instruction i) {
		if(!(i instanceof Inst)) return i;
		Inst inst = (Inst)i;
		
		switch(inst.getType()) {
			case ADD: case SUB:
			case DIV: case MUL:
			case SHR: case SHL:
			case XOR: case MOD:
			case AND: case OR:
			case NEQ: case EQ:
			case GTE: case GT:
			case LTE: case LT: {
				require(inst.getParam(0), InstParam.Type.REF);
				requireRefValue(inst.getParam(1));
				requireRefValue(inst.getParam(2));
				break;
			}
			
			case SET: case NEG:
			case NOT: case NOR: {
				require(inst.getParam(0), InstParam.Type.REF);
				requireRefValue(inst.getParam(1));
				break;
			}
			
			case BR:
			case LABEL: {
				requireRefLabel(inst.getParam(0));
				break;
			}
			
			case BRZ: case BNZ: {
				require(inst.getParam(0), InstParam.Type.REF);
				requireRefLabel(inst.getParam(1));
				break;
			}
			
			case RET: {
				break;
			}
			
			case CALL: {
				break;
			}
			
			default: {
				LOGGER.warn("Undefined type '%s'", inst.getType());
			}
		}
		
		return inst;
	}
}
