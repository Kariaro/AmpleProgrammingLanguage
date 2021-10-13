package hardcoded.assembly.x86;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hardcoded.assembly.x86.AssemblyConsts.AsmOp;
import hardcoded.assembly.x86.AssemblyConsts.OprTy;
import hardcoded.utils.StringUtils;

/**
 * This class is used for debugging the assembly class.
 * 
 * <p>This class will provide methods
 * to generate random instructions in
 * batches.
 * 
 * @author HardCoded
 */
public final class RandomAsmGenerator {
	private RandomAsmGenerator() {}
	
	public static void main(String[] args) {
//		for(AsmMnm mnm : AsmMnm.values()) {
//			List<AsmOp> list = Assembly.get(mnm);
//			
//			for(AsmOp op : list) {
//				if(op.getNumOperands() == 2) {
//					OprTy ty0 = op.getOperand(0);
//					OprTy ty1 = op.getOperand(1);
//					
////					switch(ty.type()) {
////						case 'M':
////						case 'J':
////						case 'E':
////						case 'I':
////							continue;
////						
////						default:
////					}
////					
////					switch(ty) {
////						case FS: case GS:
////						case Eb:
////							continue;
////						
////						default:
////					}
//					
//					System.out.printf("%-10s%-20s%-20s{%-4s, %-4s}\n", op.getMnemonic(), ty0, ty1, ty0.name(), ty1.name());
//				}
//			}
//		}
//		
//		
//		if(true) return;
		String str = "";
		for(int i = 0; i < 100; i++) {
			AsmInst inst = generate();
			if(inst.getNumOperands() != 2) {
				i--;
				continue;
			}
			
			//System.out.println();
			//System.out.println();
			//System.out.println();
			//System.out.println("=====================================================");
			System.out.println(inst);
			
			str += 
					StringUtils.printHexString("", Assembly.compile(inst));
		}
		
		System.out.println(str);
	}
	
	private static Random random = new Random();
	public static void setSeed(long seed) {
		random.setSeed(seed);
	}
	
	public static List<AsmInst> generate(int size) {
		List<AsmInst> list = new ArrayList<>();
		
		if(size < 1) return list;
		
		for(int i = 0; i < size; i++) {
			list.add(generate());
		}
		
		return list;
	}
	
	public static AsmInst generate() {
		while(true) {
			AsmInst inst = _generate();
			if(inst == null) continue;
			
			if(!AsmLoader.canEncodeInstruction(inst))
				continue;
				
			return inst;
		}
	}
	
	private static AsmInst _generate() {
		AsmMnm mnemonic = pickRandom(AsmMnm.values());
		AsmOp op = pickRandom(AsmLoader.get(mnemonic));
		if(op == null) return null;
		
		AsmOpr[] params = new AsmOpr[op.getNumOperands()];
		for(int i = 0; i < params.length; i++) {
			AsmOpr opr = generateOperand(params[0], op.getOperand(i));
			if(opr == null) return null;
			
			params[i] = opr;
		}
		
		return new AsmInst(mnemonic, params);
	}
	
	private static AsmOpr generateOperand(AsmOpr first, OprTy type) {
		switch(type.type()) {
			case 'C': return new OprBuilder().reg(RegisterX86.get(RegisterType.control, random.nextInt(7))).get();
			case 'D': return new OprBuilder().reg(RegisterX86.get(RegisterType.debug, random.nextInt(7))).get();
			case 'S': return new OprBuilder().reg(RegisterX86.get(RegisterType.segment, random.nextInt(7))).get();
			
			case 'K': {
				String typeName = type.name();
				if(Character.isLowerCase(typeName.charAt(0))) {
					if(first == null || first.getSize() == 32)
						return new OprBuilder().fromString(typeName.toUpperCase());
					
					if(first.getSize() == 16) return new OprBuilder().fromString(typeName.substring(1));
					return new OprBuilder().fromString("R" + typeName.substring(1));
				}
				
				return new OprBuilder().fromString(typeName);
			}
			
			case 'J':
			case 'I': {
				switch(type.postfix()) {
					case 'b': return new OprBuilder().imm((byte)random.nextInt());
					case 'w': return new OprBuilder().imm((short)random.nextInt());
					case 'd': return new OprBuilder().imm(random.nextInt());
					case 'q': return new OprBuilder().imm(random.nextLong());
					case 'v': {
						if(first == null)
							return new OprBuilder().imm(random.nextInt());
						
						return new OprBuilder().imm(random.nextLong() & ((1L << (first.getSize())) - 1L));
					}
					case 'z': {
						if(first == null)
							return new OprBuilder().imm(random.nextInt());
						
						if(first.getSize() == 16)
							return new OprBuilder().imm((short)random.nextInt());
						
						return new OprBuilder().imm(random.nextInt());
					}
				}
				return new OprBuilder().imm((byte)random.nextInt());
			}
			case 'R':
			case 'E':
			case 'G': {
				if(type.type() == 'E' && random.nextBoolean()) {
					
				} else {
					switch(type.postfix()) {
						case 'b': return new OprBuilder().reg(RegisterX86.get(RegisterType.r8 , random.nextInt(15))).get();
						case 'w': return new OprBuilder().reg(RegisterX86.get(RegisterType.r16, random.nextInt(15))).get();
						case 'd': return new OprBuilder().reg(RegisterX86.get(RegisterType.r32, random.nextInt(15))).get();
						case 'q': return new OprBuilder().reg(RegisterX86.get(RegisterType.r64, random.nextInt(15))).get();
						case 'v': {
							if(first == null)
								return new OprBuilder().reg(RegisterX86.get(pickRandom(RegisterType.r16, RegisterType.r32, RegisterType.r64), random.nextInt(15))).get();
							
							if(first.getSize() == 16) return new OprBuilder().reg(RegisterX86.get(pickRandom(RegisterType.r16), random.nextInt(15))).get();
							if(first.getSize() == 32) return new OprBuilder().reg(RegisterX86.get(pickRandom(RegisterType.r32), random.nextInt(15))).get();
							return new OprBuilder().reg(RegisterX86.get(pickRandom(RegisterType.r64), random.nextInt(15))).get();
						}
	//					case 'z': return new OprBuilder().reg(RegisterX86.get(pickRandom(RegisterType.r16, RegisterType.r32), random.nextInt(15))).get();
					}
					return new OprBuilder().reg(pickRandom(RegisterX86.values())).get();
				}
			}
			case 'M': {
				RegisterX86 reg = RegisterX86.get(pickRandom(RegisterType.r32, RegisterType.r64), random.nextInt(15));
				switch(type.postfix()) {
					case '\0': return new OprBuilder().reg(reg).ptr();
					case 'b': return new OprBuilder().reg(reg).ptrByte();
					case 'w': return new OprBuilder().reg(reg).ptrWord();
					case 'd': return new OprBuilder().reg(reg).ptrDword();
					case 'q': return new OprBuilder().reg(reg).ptrQword();
					case 'v': {
						if(first == null)
							return new OprBuilder().reg(reg).ptr((8 << random.nextInt(3)));
						
						return new OprBuilder().reg(reg).ptr(first.getSize());
					}
				}
			}
		}
		
		return null;
	}
	
	@SafeVarargs
	private static <T> T pickRandom(T... array) {
		return array[random.nextInt(array.length)];
	}
	
	private static <T> T pickRandom(List<T> list) {
		if(list.isEmpty()) return null;
		return list.get(random.nextInt(list.size()));
	}
}
