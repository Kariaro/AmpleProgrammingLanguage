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
			throw new AmpleInterpreterException("Could not find main function");
		}
		
		// Update the inst format
		runFunction(main, new Locals(), context);
	}
	
	public void runRepl(ReplContext ctx) {
		AmpleContext context = new AmpleContext(ctx.file);
		if (ctx.local == null) {
			ctx.local = new Locals();
		}
		
		// Should these share locals?
		for (int i = ctx.index; i < context.getCodeBlocks(); i++) {
			AmpleFunc block = context.getCodeBlock(i);
			runFunction(block, new Locals(), ctx.local, context);
		}
		
		ctx.index = context.getCodeBlocks();
	}
	
	public static class ReplContext {
		private IntermediateFile file;
		private int index;
		private Locals local;
		
		public void setFile(IntermediateFile file) {
			this.file = file;
		}
		
		public void clear() {
			index = 0;
			local = null;
		}
	}
	
	public Value runFunction(AmpleFunc func, Locals params, AmpleContext context) {
		//LOGGER.debug("runFunction: {}", func);
		
		// TODO: Global variables
		Locals local = new Locals();
		local.add(params);
		
		return runFunction(func, params, local, context);
	}
	
	private Value runFunction(AmpleFunc func, Locals params, Locals local, AmpleContext context) {
		List<Inst> list = func.getInstructions();
		List<Value.ArrayValue> allocatedList = new ArrayList<>();
		
		try {
			int max = 100000;
			int index = 0;
			while (--max > 0) {
				if (index >= list.size()) {
					// This means that a return was not present but for code blocks this is fine
					break;
				}
				
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
							
							Value varargs = context.getMemory().allocate(varargSize);
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
							context.getMemory().deallocate(varargs.getInteger());
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
							return new Value.NumberValue(0);
						}
						
						InstParam src = inst.getParam(0);
						return convertFromParam(local, src, context);
					}
					case STACK_ALLOC -> {
						InstRef dst = inst.getRefParam(0).getReference();
						int size = (int) inst.getNumParam(1).getValue();
						
						Value.ArrayValue allocated = context.getMemory().allocate(size);
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
					case ZEXT, SEXT, TRUNC -> {
						InstRef dst = inst.getRefParam(0).getReference();
						ValueType type = dst.getValueType();
						InstParam src = inst.getParam(1);
						
						Value.ArrayValue arrayValue = null;
						
						long number;
						if (src instanceof InstParam.Str str) {
							// Allocate string
							Value.ArrayValue value = context.getMemory().allocateString(str.getValue());
							allocatedList.add(value);
							number = value.getInteger();
						} else if (src instanceof InstParam.Num num) {
							number = num.getValue();
						} else if (src instanceof InstParam.Ref ref) {
							Value value = local.get(ref.getReference());
							number = switch (value.getType()) {
								case Integer, Array -> value.getInteger();
								case Floating -> throw new RuntimeException("Cannot extend floating point");
							};
							
							if (value instanceof Value.ArrayValue arr) {
								arrayValue = arr;
							}
						} else {
							throw new RuntimeException("Unknown parameter type '" + src.getClass() + "' (" + src + ")");
						}
						
						Value result;
						if (type.getDepth() > 0) {
							// Only allowed if the casted value was a pointer. Changing between logical size
							if (arrayValue != null) {
								// Make sure it's unsigned
								result = new Value.OffsetArrayValue(arrayValue, 0);
							} else {
								if (src.getSize().getDepth() == 0) {
									LOGGER.info("{}", local);
									LOGGER.info("{}", inst);
									throw new RuntimeException("Undefined behavior. Casting from a number to memory");
								}
								
								result = context.getMemory().getAllocated(number);
							}
						} else {
							if (type.isFloating()) {
								throw new RuntimeException("Floating type not extendable");
							}
							
							int typeSize = type.calculateBytes();
							long mask = switch (typeSize) {
								case 8 -> 0xffffffffffffffffL;
								case 4 -> 0x00000000ffffffffL;
								case 2 -> 0x000000000000ffffL;
								case 1 -> 0x00000000000000ffL;
								default -> throw new RuntimeException("Unknown integer type size '" + typeSize + "'");
							};
							
							int srcSize = src.getSize().calculateBytes();
							long srcMask = (-1L) >>> (srcSize * 8);
							number &= srcMask;
							
							if (typeSize == 8 && arrayValue != null) {
								result = new Value.OffsetArrayValue(arrayValue, 0);
							} else {
								if (opcode == Opcode.SEXT) {
									// Sign extend if last bit is set
									
									if ((number & (1L << (srcSize * 8 - 1))) != 0) {
										number |= ~srcMask;
									}
								}
								
								number &= mask;
								result = new Value.NumberValue(number & mask);
							}
						}
						
						local.put(dst, result);
					}
					// Equality operators
					case LTE, LT, GTE, GT, ILTE, ILT, IGTE, IGT, NEQ, EQ -> {
						InstRef dst = inst.getRefParam(0).getReference();
						Value a = convertFromParam(local, inst.getParam(1), context);
						Value b = convertFromParam(local, inst.getParam(2), context);
						
						boolean unsigned = switch (opcode) {
							case LTE, LT, GTE, GT -> true;
							default -> false;
						};
						
						long compare = switch (a.getType()) {
							case Integer -> unsigned
								? Long.compareUnsigned(a.getInteger(), b.getInteger())
								: Long.compare(a.getInteger(), b.getInteger());
							case Floating -> Double.compare(a.getFloating(), b.getFloating());
							case Array -> Long.compareUnsigned(a.getInteger(), b.getInteger());
						};
						
						boolean result = switch (opcode) {
							case LTE, ILTE -> compare <= 0;
							case LT, ILT -> compare < 0;
							case GTE, IGTE -> compare >= 0;
							case GT, IGT -> compare > 0;
							
							case NEQ -> compare != 0;
							case EQ -> compare == 0;
							default -> false; // Never reached
						};
						
						local.put(dst, new Value.NumberValue(result ? 1 : 0));
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
					case AND, XOR, SHR, SHL, OR, SUB, ADD, MUL, DIV, MOD, IMUL, IDIV, IMOD -> {
						InstRef dst = inst.getRefParam(0).getReference();
						Value a = convertFromParam(local, inst.getParam(1), context);
						Value b = convertFromParam(local, inst.getParam(2), context);
						
						// Arrays are always first
						Value.Type type = a.getType();
						if (b.getType() == Value.Type.Array) {
							type = Value.Type.Array;
						}
						
						boolean destroyArray = switch (opcode) {
							case ADD, SUB -> false;
							default -> {
								if (type == Value.Type.Array) {
									// This will destroy the safety of the pointer
									type = Value.Type.Integer;
								}
								yield true;
							}
						};
						
						long result = switch (opcode) {
							case AND -> switch (type) {
								case Integer -> a.getInteger() & b.getInteger();
								case Floating, Array -> throw new RuntimeException("Cannot AND " + type + " values");
							};
							case XOR -> switch (type) {
								case Integer -> a.getInteger() ^ b.getInteger();
								case Floating, Array -> throw new RuntimeException("Cannot XOR " + type + " values");
							};
							case SHR -> switch (type) {
								case Integer -> a.getInteger() >> b.getInteger();
								case Floating, Array -> throw new RuntimeException("Cannot SHR " + type + " values");
							};
							case SHL -> switch (type) {
								case Integer -> a.getInteger() << b.getInteger();
								case Floating, Array -> throw new RuntimeException("Cannot SHL " + type + " values");
							};
							case OR -> switch (type) {
								case Integer -> a.getInteger() | b.getInteger();
								case Floating, Array -> throw new RuntimeException("Cannot OR " + type + " values");
							};
							case IMUL, MUL -> switch (type) {
								case Integer -> a.getInteger() * b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() * b.getFloating());
								case Array -> throw new RuntimeException("Cannot MUL " + type + " values");
							};
							case IDIV -> switch (type) {
								case Integer -> a.getInteger() / b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() * b.getFloating());
								case Array -> throw new RuntimeException("Cannot DIV " + type + " values");
							};
							case DIV -> switch (type) {
								case Integer -> Long.divideUnsigned(a.getInteger(), b.getInteger());
								case Floating -> Double.doubleToRawLongBits(a.getFloating() * b.getFloating());
								case Array -> throw new RuntimeException("Cannot DIV " + type + " values");
							};
							// TODO: Unsigned modulo and signed modulo
							case MOD, IMOD -> switch (type) {
								case Integer -> a.getInteger() % b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() % b.getFloating());
								case Array -> throw new RuntimeException("Cannot MOD " + type + " values");
							};
							case ADD -> switch (type) {
								case Integer, Array -> a.getInteger() + b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() + b.getFloating());
							};
							case SUB -> switch (type) {
								case Integer, Array -> a.getInteger() - b.getInteger();
								case Floating -> Double.doubleToRawLongBits(a.getFloating() - b.getFloating());
							};
							default -> throw new RuntimeException("Arithmetic opcode '" + opcode + "' not implemented");
						};
						
						Value value = switch (type) {
							case Array -> destroyArray
								? new Value.NumberValue(result)
								: new Value.OffsetArrayValue((Value.ArrayValue) a, (int) (result - a.getInteger()));
							case Integer, Floating -> new Value.NumberValue(type == Value.Type.Floating, result);
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
						Value result = array.getIndex(arrayIdx, dst.getValueType(), context.getMemory()::getAllocated);
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
									Value item = a.getIndex(i, Primitives.U8, context.getMemory()::getAllocated);
									sb.append((char) (int) item.getInteger());
								}
								
								System.out.print(sb);
								// LOGGER.info("INTERPRETER -> '{}'", sb);
							}
						}
					}
					case NEG -> {
						InstRef dst = inst.getRefParam(0).getReference();
						Value a = convertFromParam(local, inst.getParam(1), context);
						local.put(dst, new Value.NumberValue(-a.getInteger()));
					}
					
					default -> throw new RuntimeException("Unknown instruction '%s'".formatted(opcode));
				}
				
				index++;
			}
		} finally {
			// Deallocate stack
			for (Value.ArrayValue item : allocatedList) {
				context.getMemory().deallocate(item.getInteger());
			}
		}
		
		return new Value.NumberValue(0);
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
		return new Value.NumberValue(num.getSize().isFloating(), num.getValue());
	}
	
	private static Value convertFrom(InstParam.Str str, AmpleContext context) {
		// TODO: Deallocate strings after creation
		return context.getMemory().allocateString(str.getValue());
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
