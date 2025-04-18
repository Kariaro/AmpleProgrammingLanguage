package me.hardcoded.interpreter;

import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.interpreter.AmpleContext.AmpleFunc;
import me.hardcoded.interpreter.value.Value;
import me.hardcoded.utils.error.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
		
		// Execute all globals
		Locals globals = new Locals(null);
		for (int i = 0; i < context.getCodeBlocks(); i++) {
			var block = context.getCodeBlock(i);
			runFunction(block, new Locals(null), globals, context);
		}
		
		// Run main function
		runFunction(main, new Locals(globals), context);
	}
	
	public void runRepl(ReplContext ctx) {
		AmpleContext context = new AmpleContext(ctx.file);
		if (ctx.local == null) {
			ctx.local = new Locals(null);
		}
		
		// Should these share locals?
		for (int i = ctx.index; i < context.getCodeBlocks(); i++) {
			AmpleFunc block = context.getCodeBlock(i);
			runFunction(block, new Locals(null), ctx.local, context);
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
		// LOGGER.debug("runFunction: {}", func);
		
		Locals local = new Locals(params.globals);
		local.add(params);
		
		return runFunction(func, params, local, context);
	}
	
	private Value runFunction(AmpleFunc func, Locals params, Locals local, AmpleContext context) {
		List<Inst> list = func.getInstructions();
		List<Value.ArrayValue> allocatedList = new ArrayList<>();
		
		int index = 0;
		try {
			long max = Long.MAX_VALUE;
			while (true) {
				if (--max < 0) {
					throw new RuntimeException("MAX LIMIT REACHED!!!!!!!!!!");
				}
				
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
				
				if (!inst.getSyntaxPosition().getPath().contains("std.amp")) {
					// LOGGER.debug("{}", ErrorUtil.createError(inst.getSyntaxPosition(), ""));
					// LOGGER.debug("{}", local);
					// LOGGER.debug("{}", local);
					// LOGGER.debug("  pre : {} ({})", index, inst);
					// System.out.println("-".repeat(100));
				}
				
				try {
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
							Locals funParams = new Locals(params.globals);
							
							int paramCount = called.getParamCount();
							
							if (called.isVararg()) {
								int varargSize = 0;
								for (int i = 0; i < inst.getParamCount() - 2; i++) {
									InstParam param = inst.getParam(i + 2);
									if (i >= paramCount - 1) {
										ValueType type = param.getSize();
										int typeSize = getSize(type);
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
									
									int typeSize = getSize(type);
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
						case ZEXT, SEXT, TRUNC, D_TO_F_CAST -> {
							InstRef dst = inst.getRefParam(0).getReference();
							ValueType type = dst.getValueType();
							InstParam src = inst.getParam(1);
							
							Value.ArrayValue arrayValue = null;
							
							boolean from_f = false;
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
									case Floating -> Double.doubleToRawLongBits(value.getFloating());
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
									result = arrayValue.withOffset(0);
								} else {
									if (src.getSize().getDepth() == 0) {
										LOGGER.info("{}", local);
										LOGGER.info("{}", inst);
										throw new RuntimeException("Undefined behavior. Casting from a number to memory 0x" + Long.toString(number, 16));
									}
									
									result = context.getMemory().getAllocated(number);
								}
							} else if (opcode == Opcode.D_TO_F_CAST) {
								int srcSize = src.getSize().calculateBytes();
								long srcMask = (-1L) >>> (64 - srcSize * 8);
								number &= srcMask;
								result = new Value.NumberValue((double) number);
							} else {
								if (type.isFloating()) {
									throw new RuntimeException("Floating type not extendable");
								}
								
								int typeSize = type.calculateBytes();
								long mask = (-1L) >>> (64 - typeSize * 8);
								int srcSize = src.getSize().calculateBytes();
								long srcMask = (-1L) >>> (64 - srcSize * 8);
								number &= srcMask;
								
								if (typeSize == 8 && arrayValue != null) {
									result = arrayValue.withOffset(0);
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
							Value a = convertFromParam(local, inst.getParam(0), context);
							Value b = convertFromParam(local, inst.getParam(1), context);
							
							boolean unsigned = switch (opcode) {
								case LTE, LT, GTE, GT -> true;
								default -> false;
							};
							
							ValueType type = dst.getValueType();
							
							long compare = switch (a.getType()) {
								case Integer -> {
									long av = a.getInteger();
									long bv = b.getInteger();
									if (unsigned) {
										yield Long.compareUnsigned(av, bv);
									}
									
									// make sure signed integers are sign extended
									switch (type.getSize()) {
										case 32 -> {
											av = ((int) av);
											bv = ((int) bv);
										}
										case 16 -> {
											av = ((short) av);
											bv = ((short) bv);
										}
										case 8 -> {
											av = ((byte) av);
											bv = ((byte) bv);
										}
									}
									
									yield Long.compare(av, bv);
								}
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
							Value a = convertFromParam(local, inst.getParam(0), context);
							Value b = convertFromParam(local, inst.getParam(1), context);
							
							// Arrays are always first
							Value.Type type = a.getType();
							if (b.getType() == Value.Type.Array) {
								type = Value.Type.Array;
							}
							int s = dst.getValueType().calculateBytes();
							if (dst.getValueType().isUnsigned()) {
								s = 8; // no sext  // why do I even need this, move should fix this
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
									case Integer -> a.getInteger(s) & b.getInteger(s);
									case Floating, Array -> throw new RuntimeException("Cannot AND " + type + " values");
								};
								case XOR -> switch (type) {
									case Integer -> a.getInteger(s) ^ b.getInteger(s);
									case Floating, Array -> throw new RuntimeException("Cannot XOR " + type + " values");
								};
								case SHR -> switch (type) {
									case Integer -> a.getInteger(s) >>> b.getInteger(s);
									case Floating, Array -> throw new RuntimeException("Cannot SHR " + type + " values");
								};
								case SHL -> switch (type) {
									case Integer -> a.getInteger(s) << b.getInteger(s);
									case Floating, Array -> throw new RuntimeException("Cannot SHL " + type + " values");
								};
								case OR -> switch (type) {
									case Integer -> a.getInteger(s) | b.getInteger(s);
									case Floating, Array -> throw new RuntimeException("Cannot OR " + type + " values");
								};
								case IMUL, MUL -> switch (type) {
									case Integer -> a.getInteger(s) * b.getInteger(s);
									case Floating -> Double.doubleToRawLongBits(a.getFloating() * b.getFloating());
									case Array -> throw new RuntimeException("Cannot MUL " + type + " values");
								};
								case IDIV -> switch (type) {
									case Integer -> a.getInteger(s) / b.getInteger(s);
									case Floating -> Double.doubleToRawLongBits(a.getFloating() * b.getFloating());
									case Array -> throw new RuntimeException("Cannot DIV " + type + " values");
								};
								case DIV -> switch (type) {
									case Integer -> Long.divideUnsigned(a.getInteger(s), b.getInteger(s));
									case Floating -> Double.doubleToRawLongBits(a.getFloating() * b.getFloating());
									case Array -> throw new RuntimeException("Cannot DIV " + type + " values");
								};
								case MOD -> switch (type) {
									case Integer -> Long.remainderUnsigned(a.getInteger(s), b.getInteger(s));
									case Floating -> Double.doubleToRawLongBits(a.getFloating() % b.getFloating());
									case Array -> throw new RuntimeException("Cannot MOD " + type + " values");
								};
								case IMOD -> switch (type) {
									case Integer -> a.getInteger(s) % b.getInteger(s);
									case Floating -> Double.doubleToRawLongBits(a.getFloating() % b.getFloating());
									case Array -> throw new RuntimeException("Cannot MOD " + type + " values");
								};
								case ADD -> switch (type) {
									case Integer, Array -> a.getInteger(s) + b.getInteger(s);
									case Floating -> Double.doubleToRawLongBits(a.getFloating() + b.getFloating());
								};
								case SUB -> switch (type) {
									case Integer, Array -> a.getInteger(s) - b.getInteger(s);
									case Floating -> Double.doubleToRawLongBits(a.getFloating() - b.getFloating());
								};
								default -> throw new RuntimeException("Arithmetic opcode '" + opcode + "' not implemented");
							};
							
							Value value = switch (type) {
								case Array -> {
									if (destroyArray) {
										yield new Value.NumberValue(result);
									}
									if (a instanceof Value.ArrayValue arv) {
										yield arv.withOffset(result - a.getInteger());
									}
									if (b instanceof Value.ArrayValue brv) {
										yield brv.withOffset(result - b.getInteger());
									}
									throw new RuntimeException("Unknown bug??");
								}
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
								case "file_size" -> {
									InstRef dst = inst.getRefParam(3).getReference();
									InstParam pathParam = inst.getParam(2);
									String path = "";
									
									if (pathParam instanceof InstParam.Str str) {
										path = str.getValue();
									} else {
										Value pathData = convertFromParam(local, pathParam, context);
										StringBuilder sb = new StringBuilder();
										for (int i = 0; i < 256; i++) { // max len
											Value item = pathData.getIndex(i, Primitives.U8, context.getMemory()::getAllocated);
											if (item.getInteger() == 0) {
												break;
											}
											sb.append((char) (int) item.getInteger());
										}
										path = sb.toString();
									}
									
									
									long size;
									try {
										size = Files.size(Path.of(inst.getSyntaxPosition().getPath() + "/../" + path));
									} catch (IOException e) {
										size = -1;
									}
									
									local.put(dst, new Value.NumberValue(size));
								}
								case "read_file" -> {
									Value dst = convertFromParam(local, inst.getParam(3), context);
									Value name = convertFromParam(local, inst.getParam(2), context);
									
									StringBuilder sb = new StringBuilder();
									for (int i = 0; i < 256; i++) { // max len
										Value item = name.getIndex(i, Primitives.U8, context.getMemory()::getAllocated);
										if (item.getInteger() == 0) {
											break;
										}
										sb.append((char) (int) item.getInteger());
									}
									String path = sb.toString();
									
									byte[] data;
									try {
										data = Files.readAllBytes(Path.of(inst.getSyntaxPosition().getPath() + "/../" + path));
									} catch (IOException e) {
										data = new byte[0];
									}
									
									for (int i = 0; i < data.length; i++) {
										int value = 0xff & data[i];
										dst.setIndex(i, new Value.NumberValue(value), Primitives.U8);
									}
								}
								default -> throw new RuntimeException("Unknown compile command '%s'".formatted(command));
							}
						}
						case NEG -> {
							InstRef dst = inst.getRefParam(0).getReference();
							Value a = convertFromParam(local, inst.getParam(1), context);
							local.put(dst, new Value.NumberValue(-a.getInteger()));
						}
						
						case NOT -> {
							InstRef dst = inst.getRefParam(0).getReference();
							Value a = convertFromParam(local, inst.getParam(1), context);
							local.put(dst, new Value.NumberValue(a.getInteger() != 0 ? 1 : 0));
						}
						case NOR -> {
							InstRef dst = inst.getRefParam(0).getReference();
							Value a = convertFromParam(local, inst.getParam(1), context);
							local.put(dst, new Value.NumberValue(~a.getInteger()));
						}
						
						// Member
						case SIZEOF -> {
							// Return size of struct
							InstRef dst = inst.getRefParam(0).getReference();
							var type = (InstParam.Type) inst.getParam(1);
							
							int size = getSize(type.getSize());
							local.put(dst, new Value.NumberValue(size));
						}
						case MEMBER_PTR -> {
							InstRef dst = inst.getRefParam(0).getReference();
							InstParam src = inst.getParam(1);
							InstParam idx = inst.getParam(2);
							int memberIndex = (int) inst.getNumParam(3).getValue();
							
							int arrayIdx;
							if (idx instanceof InstParam.Ref ref) {
								arrayIdx = (int) local.get(ref.getReference()).getInteger();
							} else if (idx instanceof InstParam.Num num) {
								arrayIdx = (int) num.getValue();
							} else {
								throw new RuntimeException("Invalid read position '" + idx + "'");
							}
							
							var structData = src.getSize().getStructData();
							var members = structData.getMembers();
							int sizeof = getSize(src.getSize().createArray(
								Math.max(0, src.getSize().getDepth() - 1)));
							
							int offset = 0;
							for (int i = 0; i < memberIndex; i++) {
								var member = members.get(i);
								int size = getSize(member.getValue());
								offset += size;
							}
							
							var srcData = convertFromParam(local, src, context);
							if (srcData instanceof Value.ArrayValue arr) {
								Value offsetValue = arr.withOffset(offset + (sizeof * arrayIdx));
								local.put(dst, offsetValue);
							} else {
								LOGGER.info("{}", srcData);
								throw new RuntimeException("Cannot get member_ptr from non pointer type");
							}
						}
						
						default -> throw new RuntimeException("Unknown instruction '%s'".formatted(opcode));
					}
					
				} finally {
					if (!inst.getSyntaxPosition().getPath().contains("std.amp")) {
						// LOGGER.debug("{}", ErrorUtil.createError(inst.getSyntaxPosition(), ""));
						// LOGGER.debug("{}", local);
						// LOGGER.debug("{}", local);
						// LOGGER.debug("  post: {} ({})", index, inst);
						// System.out.println("-".repeat(100));
					}
				}
				
				index++;
			}
		} catch (Exception e) {
			var inst = list.get(index);
			var syntaxPosition = inst.getSyntaxPosition();
			
			LOGGER.info(" : {}", local);
			LOGGER.info("Failed at : {}", inst);
			String message = "\nPath: (%s:%d:%d)%s".formatted(
				syntaxPosition.getPath(),
				syntaxPosition.getStartPosition().line() + 1,
				syntaxPosition.getStartPosition().column() + 1,
				ErrorUtil.createError(syntaxPosition, e.getMessage())
			);
			LOGGER.warn(message);
			e.printStackTrace();
			throw e;
		} finally {
			// Deallocate stack
			for (Value.ArrayValue item : allocatedList) {
				context.getMemory().deallocate(item.getInteger());
			}
		}
		
		return new Value.NumberValue(0);
	}
	
	private int getSize(ValueType type) {
		var structData = type.getStructData();
		if (type.getDepth() > 0 || structData == null) {
			return type.calculateBytes();
		}
		
		var members = structData.getMembers();
		int size = 0;
		for (var member : members) {
			size += getSize(member.getValue());
		}
		
		return size;
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
		private final Locals globals;
		private final Map<InstRef, Value> map = new LinkedHashMap<>();
		
		public Locals(Locals globals) {
			this.globals = globals;
		}
		
		public void put(InstRef ref, Value value) {
			// Globals
			if (globals != null && globals.map.containsKey(ref)) {
				globals.put(ref, value);
			}
			
			if (value == null) {
				throw new RuntimeException("Invalid value cannot set '" + ref + "' to null");
			}
			map.put(ref, value);
		}
		
		public Value get(InstRef ref) {
			// Globals
			if (globals != null && globals.map.containsKey(ref)) {
				return globals.get(ref);
			}
			
			if (!map.containsKey(ref)) {
				throw new RuntimeException("Invalid value cannot get '" + ref + "' because it does not exist");
			}
			
			return map.get(ref);
		}
		
		public void add(Locals locals) {
			this.map.putAll(locals.map);
		}
		
		public Locals getGlobals() {
			return globals;
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
