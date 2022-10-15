package me.hardcoded.interpreter;

import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.interpreter.AmpleContext.AmpleFunc;
import me.hardcoded.interpreter.value.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ample code context class for running the language
 */
public class AmpleRunner {
	private static final Logger LOGGER = LogManager.getLogger(AmpleRunner.class);
	
	public void run(IntermediateFile instFile) throws AmpleInterpreterException {
		AmpleContext context = new AmpleContext(instFile);
		AmpleFunc main = context.getMainFunction();
		
		if (main == null) {
			throw new AmpleInterpreterException();
		}
		
		// Update the inst format
		runFunction(main, new Locals(), context);
	}
	
	public Value runFunction(AmpleFunc func, Locals params, AmpleContext context) {
		//LOGGER.debug("runFunction: {}", func);
		
		// TODO: Global variables
		Locals local = new Locals();
		local.add(params);
		
		List<Inst> list = func.getInstructions();
		List<Value.ArrayValue> allocatedList = new ArrayList<>();
		
		try {
			int index = 0;
			while (index < 100) {
				Inst inst = list.get(index);
				Opcode opcode = inst.getOpcode();
				
				if (opcode == Opcode.LABEL) {
					index++;
					continue;
				}
				
				//LOGGER.debug("{}", local);
				//LOGGER.debug("  exec: {} ({})", index, inst);
				
				switch (opcode) {
					case MOV -> {
						// 1st param is a always a reference
						InstRef dst = inst.getRefParam(0).getReference();
						
						// 2nd param has more options
						InstParam src = inst.getParam(1);
						local.put(dst, convertFromParam(local, src, context));
					}
					case CALL -> {
						// 1st, 2nd param is a always a reference
						InstRef dst = inst.getRefParam(0).getReference();
						InstRef fun = inst.getRefParam(1).getReference();
						AmpleFunc called = context.getFunction(fun);
						Locals funParams = new Locals();
						
						int paramCount = called.getParamCount();
						
						if (called.isVararg()) {
							int varargSize = 0;
							for (int i = 0; i < inst.getParamCount() - 2; i++) {
								InstParam param = inst.getParam(i + 2);
								if (i >= paramCount - 1) {
									ValueType type = param.getSize();
									int typeSize = (type.getDepth() > 0) ? ValueType.getPointerSize() : (type.getSize() >> 3);
									varargSize += typeSize;
								} else {
									InstRef paramRef = called.getParameters().get(i);
									funParams.put(paramRef, convertFromParam(local, param, context));
								}
							}
							
							Value varargs = context.allocate(varargSize);
							int offset = 0;
							for (int i = paramCount - 1; i < inst.getParamCount() - 2; i++) {
								InstParam param = inst.getParam(i + 2);
								
								ValueType type = param.getSize();
								Value value = convertFromParam(local, param, context);
								varargs.setIndex(offset, value, type);
								
								int typeSize = (type.getDepth() > 0) ? ValueType.getPointerSize() : (type.getSize() >> 3);
								offset += typeSize;
							}
							
							// Set varargs param
							funParams.put(called.getParameters().get(paramCount - 1), varargs);
							context.deallocate(varargs.getInteger());
						} else {
							for (int i = 0; i < paramCount; i++) {
								InstRef paramRef = called.getParameters().get(i);
								funParams.put(paramRef, convertFromParam(local, inst.getParam(i + 2), context));
							}
						}
						
						Value result = runFunction(called, funParams, context);
						local.put(dst, result);
					}
					case RET -> {
						if (inst.getParamCount() == 0) {
							// Return unspecified
							return new Value.IntegerValue(false, 0);
						}
						
						InstParam src = inst.getParam(0);
						return convertFromParam(local, src, context);
					}
					case STACK_ALLOC -> {
						InstRef dst = inst.getRefParam(0).getReference();
						int size = (int) inst.getNumParam(1).getValue();
						
						Value.ArrayValue allocated = context.allocate(size);
						allocatedList.add(allocated);
						local.put(dst, allocated);
					}
					case STORE -> {
						InstRef dst = inst.getRefParam(0).getReference();
						InstParam idx = inst.getParam(1);
						InstParam src = inst.getParam(2);
						
						int arrayIdx;
						if (idx instanceof InstParam.Ref ref) {
							arrayIdx = (int) local.get(ref.getReference()).getInteger();
						} else if (idx instanceof InstParam.Num num) {
							arrayIdx = (int) num.getValue();
						} else {
							throw new RuntimeException("Invalid store position '" + idx + "'");
						}
						
						Value value = convertFromParam(local, src, context);
						local.get(dst).setIndex(arrayIdx, value, src.getSize());
					}
					case CAST -> {
						InstRef dst = inst.getRefParam(0).getReference();
						ValueType type = inst.getTypeParam(1).getValueType();
						InstParam src = inst.getParam(2);
						
						long number;
						if (src instanceof InstParam.Str str) {
							// Allocate string
							Value.ArrayValue value = context.allocateString(str.getValue());
							allocatedList.add(value);
							number = value.getInteger();
						} else if (src instanceof InstParam.Num num) {
							number = num.getValue();
						} else if (src instanceof InstParam.Ref ref) {
							Value value = local.get(ref.getReference());
							number = switch (value.getType()) {
								case Integer, Array -> value.getInteger();
								case Floating -> Double.doubleToRawLongBits(value.getFloating());
							};
						} else {
							throw new RuntimeException("Unknown parameter type '" + src.getClass() + "' (" + src + ")");
						}
						
						Value result;
						if (type.getDepth() > 0) {
							result = context.getAllocated(number);
						} else {
							int typeSize = (type.getDepth() > 0) ? ValueType.getPointerSize() : (type.getSize() >> 3);
							if (type.isFloating()) {
								switch (typeSize) {
									case 8 -> result = new Value.FloatingValue(Double.longBitsToDouble(number));
									case 4 -> result = new Value.FloatingValue(Float.intBitsToFloat((int) number));
									default -> throw new RuntimeException("Unknown floating type size '" + typeSize + "'");
								}
							} else {
								long mask = switch (typeSize) {
									case 8 -> 0xffffffffffffffffL;
									case 4 -> 0x00000000ffffffffL;
									case 2 -> 0x000000000000ffffL;
									case 1 -> 0x00000000000000ffL;
									default -> throw new RuntimeException("Unknown integer type size '" + typeSize + "'");
								};
								
								result = new Value.IntegerValue(type.isUnsigned(), number & mask);
							}
						}
						
						local.put(dst, result);
					}
					// Equality operators
					case LTE, LT, GTE, GT, NEQ, EQ -> {
						InstRef dst = inst.getRefParam(0).getReference();
						Value a = convertFromParam(local, inst.getParam(1), context);
						Value b = convertFromParam(local, inst.getParam(2), context);
						
						long compare = switch (a.getType()) {
							case Integer -> a.isUnsigned()
								? Long.compareUnsigned(a.getInteger(), b.getInteger())
								: Long.compare(a.getInteger(), b.getInteger());
							case Floating -> Double.compare(a.getFloating(), b.getFloating());
							case Array -> Long.compareUnsigned(a.getInteger(), b.getInteger());
						};
						
						boolean result = switch (opcode) {
							case LTE -> compare <= 0;
							case LT -> compare < 0;
							case GTE -> compare >= 0;
							case GT -> compare > 0;
							case NEQ -> compare != 0;
							case EQ -> compare == 0;
							default -> false; // Never reached
						};
						
						local.put(dst, new Value.IntegerValue(false, result ? 1 : 0));
					}
					
					// Branch operators
					case JZ, JNZ, JMP -> {
						if (opcode == Opcode.JMP) {
							index = func.getLabel(inst.getRefParam(0).getReference());
							continue;
						}
						
						// 2nd param is always ref
						Value a = convertFromParam(local, inst.getParam(0), context);
						
						boolean isZero = switch (a.getType()) {
							case Integer, Array -> a.getInteger() == 0;
							case Floating -> a.getFloating() == 0;
						};
						
						if ((opcode == Opcode.JZ) == isZero) {
							InstRef ref = inst.getRefParam(1).getReference();
							// Jump to the reference
							index = func.getLabel(ref);
							continue;
						}
					}
					
					// Arithmetic operators
					case AND, XOR, SHR, SHL, OR, MUL, DIV, SUB, ADD -> {
						InstRef dst = inst.getRefParam(0).getReference();
						Value a = convertFromParam(local, inst.getParam(1), context);
						Value b = convertFromParam(local, inst.getParam(2), context);
						
						long result = switch (opcode) {
							case AND -> switch (a.getType()) {
								case Integer, Array -> a.getInteger() & b.getInteger();
								case Floating -> throw new RuntimeException("Cannot AND floating point values");
							};
							case XOR -> switch (a.getType()) {
								case Integer, Array -> a.getInteger() ^ b.getInteger();
								case Floating -> throw new RuntimeException("Cannot XOR floating point values");
							};
							case SHR -> switch (a.getType()) {
								case Integer, Array -> a.getInteger() >> b.getInteger();
								case Floating -> throw new RuntimeException("Cannot SHR floating point values");
							};
							case SHL -> switch (a.getType()) {
								case Integer, Array -> a.getInteger() << b.getInteger();
								case Floating -> throw new RuntimeException("Cannot SHL floating point values");
							};
							case OR -> switch (a.getType()) {
								case Integer, Array -> a.getInteger() | b.getInteger();
								case Floating -> throw new RuntimeException("Cannot or floating point values");
							};
							case MUL -> switch (a.getType()) {
								case Integer, Array -> a.getInteger() * b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() * b.getFloating());
							};
							case DIV -> switch (a.getType()) {
								case Integer, Array -> a.isUnsigned()
									? Long.divideUnsigned(a.getInteger(), b.getInteger())
									: a.getInteger() / b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() * b.getFloating());
							};
							case ADD -> switch (a.getType()) {
								case Integer, Array -> a.getInteger() + b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() + b.getFloating());
							};
							case SUB -> switch (a.getType()) {
								case Integer, Array -> a.getInteger() - b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() - b.getFloating());
							};
							default -> throw new RuntimeException("Arithmetic opcode '" + opcode + "' not implemented");
						};
						
						Value value = switch (a.getType()) {
							case Array -> new Value.OffsetArrayValue((Value.ArrayValue) a, (int) (result - a.getInteger()));
							case Integer -> new Value.IntegerValue(a.isUnsigned(), result);
							case Floating -> new Value.FloatingValue(Double.longBitsToDouble(result));
						};
						
						local.put(dst, value);
					}
					case LOAD -> {
						InstRef dst = inst.getRefParam(0).getReference();
						InstRef arr = inst.getRefParam(1).getReference();
						InstParam idx = inst.getParam(2);
						
						int arrayIdx;
						if (idx instanceof InstParam.Ref ref) {
							arrayIdx = (int) local.get(ref.getReference()).getInteger();
						} else if (idx instanceof InstParam.Num num) {
							arrayIdx = (int) num.getValue();
						} else {
							throw new RuntimeException("Invalid load position '" + idx + "'");
						}
						
						Value array = local.get(arr);
						Value result = array.getIndex(arrayIdx, dst.getValueType(), context::getAllocated);
						local.put(dst, result);
					}
					case INLINE_ASM -> {
						String type = inst.getStrParam(0).getValue();
						
						// int for interpreted mode
						if (!type.equals("int")) {
							break;
						}
						
						String command = inst.getStrParam(1).getValue();
						switch (command) {
							case "print" -> {
								// 1st param -> pointer
								// 2nd param -> length
								Value a = convertFromParam(local, inst.getParam(2), context);
								Value b = convertFromParam(local, inst.getParam(3), context);
								
								StringBuilder sb = new StringBuilder();
								
								long len = b.getInteger();
								for (int i = 0; i < len; i++) {
									Value item = a.getIndex(i, Primitives.U8, context::getAllocated);
									sb.append((char) (int) item.getInteger());
								}
								
								System.out.print(sb);
								// LOGGER.info("INTERPRETER -> '{}'", sb);
							}
						}
					}
					
					default -> throw new RuntimeException("Unknown instruction '%s'".formatted(opcode));
				}
				
				index++;
			}
		} finally {
			// Deallocate stack
			for (Value.ArrayValue item : allocatedList) {
				context.deallocate(item.getInteger());
			}
		}
		
		return new Value.IntegerValue(false, 0);
	}
	
	private static Value convertFromParam(Locals local, InstParam param, AmpleContext context) {
		if (param instanceof InstParam.Num num) {
			return convertFrom(num, context);
		} else if (param instanceof InstParam.Str str) {
			return convertFrom(str, context);
		} else if (param instanceof InstParam.Ref ref) {
			return local.get(ref.getReference());
		} else {
			throw new RuntimeException("Invalid param '" + param + "'");
		}
	}
	
	private static Value convertFrom(InstParam.Num num, AmpleContext context) {
		ValueType type = num.getSize();
		
		Value result;
		if (type.isFloating()) {
			result = new Value.FloatingValue(Double.longBitsToDouble(num.getValue()));
		} else {
			result = new Value.IntegerValue(type.isUnsigned(), num.getValue());
		}
		
		return result;
	}
	
	private static Value convertFrom(InstParam.Str str, AmpleContext context) {
		// TODO: Deallocate strings after creation
		return context.allocateString(str.getValue());
	}
	
	public int executeInstruction(int index, AmpleFunc func, AmpleContext context) {
		return 0;
	}
	
	private static class Locals {
		private final Map<InstRef, Value> map = new LinkedHashMap<>();
		
		public void put(InstRef ref, Value value) {
			if (value == null) {
				throw new RuntimeException("Invalid value cannot set '" + ref + "' to null");
			}
			map.put(ref, value);
		}
		
		public Value get(InstRef ref) {
			if (!map.containsKey(ref)) {
				throw new RuntimeException("Invalid value cannot get '" + ref + "' because it does not exist");
			}
			
			return map.get(ref);
		}
		
		public void add(Locals locals) {
			this.map.putAll(locals.map);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			
			var iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				var entry = iter.next();
				sb.append(entry.getKey().toSimpleString()).append('=').append(entry.getValue());
				if (iter.hasNext()) {
					sb.append(", ");
				}
			}
			
			return sb.append('}').toString();
		}
	}
}
