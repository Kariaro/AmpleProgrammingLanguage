package hardcoded.assembly.x86;

import static hardcoded.assembly.x86.AssemblyConsts.*;

import java.io.IOException;
import java.util.*;

import hardcoded.assembly.x86.AssemblyConsts.AsmOp;
import hardcoded.assembly.x86.AssemblyConsts.OprTy;
import hardcoded.utils.FileUtils;
import hardcoded.utils.buffer.IntBuffer;

/**
 * This class loads all x86 assembly instructions and provide
 * some validity checks for instructions.<p>
 * 
 * https://wiki.osdev.org/X86-64_Instruction_Encoding
 * https://reverseengineering.stackexchange.com/questions/19693/how-many-registers-does-an-x86-64-cpu-actually-have
 * 
 * @author HardCoded
 */
public final class AsmLoader {
	private static final AsmLoader INSTRUCTIONS;
	
	static {
		List<AsmOp> list = null;
		
		try {
			list = loadInstructions("/assembler/insts.dat");
		} catch(IOException e) {
			e.printStackTrace();
			System.err.println("Failed to load instructions : '/assembler/insts.dat'");
		}
		
		INSTRUCTIONS = new AsmLoader(list);
	}
	
	private static List<AsmOp> loadInstructions(String path) throws IOException {
		byte[] bytes = FileUtils.readInputStream(AsmLoader.class.getResourceAsStream(path));
		String[] lines = new String(bytes).split("(\r\n|\n|\r)");
		
		List<AsmOp> list = new ArrayList<>();
		for(String line : lines) {
			line = line.trim();
			if(line.isEmpty() || line.startsWith("#")) continue;
			
			String[] parts = line.split("\\s+");
			IntBuffer buffer = new IntBuffer(32);
			
			int flagsIndex = -1;
			for(int i = 2; i < parts.length; i++) {
				if(parts[i].equals("]")) {
					flagsIndex = i + 1;
					break;
				}
				
				buffer.write(Integer.parseInt(parts[i], 16));
			}
			
			int[] opcode = buffer.toArray();
			if(flagsIndex >= parts.length) {
				list.add(new AsmOp(parts[0], opcode, 0, new OprTy[0]));
				continue;
			}
			
			List<OprTy> params = new ArrayList<>();
			for(int i = flagsIndex + 1; i < parts.length; i++) {
				if(parts[i].trim().equals(",")) continue;
				String opr = parts[i].replace(",", "");
				
				// TODO: We need a way to define rXX/eXX for both the AX->DI and R8->R15
				//       This is only a temporary solution to not getting any syntax errors.
				if(opr.indexOf('/') < 0) {
					params.add(OprTy.valueOf(opr));
				} else {
					params.add(OprTy.valueOf(opr.substring(0, opr.indexOf('/'))));
				}
			}
			
			list.add(new AsmOp(parts[0], opcode, flags(parts[flagsIndex]), params.toArray(new OprTy[0])));
		}
		
		return list;
	}
	
	public static List<AsmOp> lookup(AsmInst inst) {
		List<AsmOp> list = new ArrayList<>(get(inst.getMnemonic()));
		if(list.isEmpty()) return list;
		
		for(int i = 0; i < list.size(); i++) {
			AsmOp op = list.get(i);
			if(!canBeEncoded(op, inst)) {
				list.remove(i);
				i--;
				continue;
			}
		}
		
		return list;
	}
	
	/**
	 * Returns {@code true} if the instruction can be encoded.
	 * @param inst the instruction to validate
	 * @return {@code true} if the instruction can be encoded
	 */
	public static boolean canEncodeInstruction(AsmInst inst) {
		for(AsmOp op : get(inst.getMnemonic())) {
			if(canBeEncoded(op, inst)) return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if a instruction can be encoded with the provided {@code AsmOp}.
	 * @param	op		the {@code AsmOp} to compare with
	 * @param	inst	the {@code AsmInst} to check
	 * @return {@code true} if the instruction can be encoded
	 */
	private static boolean canBeEncoded(AsmOp op, AsmInst inst) {
		int length = inst.getNumOperands();
		
		if(length != op.getNumOperands()) {
			return false;
		}
		
		if(length == 0) return true;
		
		// done: The first operand matched is the easiest one to check for.
		//       The problem becomes the second operand that can still be
		//       accepted but have an invalid size.
		// 
		//       This problem is see with the combination [ r/m16/32/64, r16/32/64 ]
		//       where you could write [ RAX, CX ] and it would accept it...
		//
		//       To prevent this we need save the first operands size and
		//       try to cross match that size with the next operands.
		
		// done: There is a big difference if the first opcode is varying or
		//       not. A varying operand is when its size can be changed by a
		//       prefix.
		// 
		
		// done: AH and rex is incompatible. Any register that encodes a
		//       value or index above 7 will become incompatible with any
		//       ah value.
		
		
		if(!matches(op, op.getOperand(0), inst.getOperand(0))) {
			return false;
		}
		
		if(length == 1) return true;
		
		AsmOpr first = inst.getOperand(0);
		boolean isVarying = op.getOperand(0).isVarying();
		
		int rexUsage = checkRexUsage(first);
		for(int j = 1; j < length; j++) {
			OprTy type = op.getOperand(j);
			AsmOpr opr = inst.getOperand(j);
			
			if(!matches(op, type, opr)) {
				return false;
			}
			
			if(isVarying) {
				switch(type.postfix()) {
					case 'v': {
						// Size must match the size of the first operand.
						if(opr.isImmediate()) {
							return first.getSize() >= opr.getSize();
						}
						if(first.getSize() != opr.getSize()) return false;
						break;
					}
					case 'z': {
						// first is 16 bit.... opr must be less or eq to 16 bit.
						// first is 32/64 bit. opr must be less or eq to 32 bit.

						if((first.getSize() == 16 && opr.getSize() > 16)
						|| (first.getSize() >= 32 && opr.getSize() > 32)) {
							return false;
						}
						break;
					}
				}
			}
			
			rexUsage |= checkRexUsage(opr);
		}
		
		// This means that the instruction used both REX and [ AH, CH, DH, BH ] and that is invalid...
		if(rexUsage == 3) return false;
		
		return true;
	}
	
	/**
	 * Returns a bit field containing the rex usage for this operand.
	 *<pre>
	 *[ .1 ] : Uses [SPL, BPL, SIL, DIL]
	 *[ 1. ] : Uses [AH, CH, DH, BH] 
	 *</pre>
	 *
	 * @param opr
	 * @return a bit field containing the rex usage for this operand.
	 */
	private static int checkRexUsage(AsmOpr opr) {
		int flags = 0;
		for(int i = 0; i < opr.length(); i++) {
			Object obj = opr.getObject(i);
			if(!(obj instanceof RegisterX86)) continue;
			
			RegisterX86 reg = (RegisterX86)obj;
			
			// If the register encodes a REX register then the first flag is set.
			if(reg.matchesAny(RegisterX86.SPL, RegisterX86.BPL, RegisterX86.SIL, RegisterX86.DIL) || reg.index > 7) flags |= 1;
			
			// If the register encodes any of [AH, CH, DH, BH] the second flag is set.
			if(reg.matchesAny(RegisterX86.AH, RegisterX86.CH, RegisterX86.DH, RegisterX86.BH)) flags |= 2;
		}
		
		return flags;
	}
	
	private static boolean matches(AsmOp op, OprTy type, AsmOpr opr) {
		// NOTE: Could potentially remove numbers that are by default 32 bit sized and 64 bit numbers are allowed.....
		if(op.has64bitFlag() && opr.getSize() == 32) return false;
		
		// TODO: Make sure all numbers inside memory pointers are below 32 bits.
		
		
		switch(type.type()) {
			case 'K': {
				// The operand must be a register
				if(!opr.isRegister()) return false;
				RegisterX86 reg = opr.getRegister(0);
				
				String typeName = type.name();
				if(Character.isLowerCase(typeName.charAt(0))) {
					// Lowercase name letter means that it's either a [ eXX ] or [ rXX ]
					// [ eXX ] only allows 16/32 bit operators.
					// [ rXX ] only allows 16/32 or 64 bit operators.
					
					if(!reg.matchesAny(RegisterType.r16, RegisterType.r32, RegisterType.r64))
						return false;
					
					if(typeName.charAt(0) == 'e') {
						if(reg.type == RegisterType.r64) return false;
					}
					
					return reg.name().endsWith(typeName.substring(1));
				}
				
				// Uppercase names means that it's a named register.
				// The name must match exactly.
				return typeName.equals(reg.name());
			}
			case 'O': {
				// This is a offset of a memory value. qword [ moffsXX ]
				if(!opr.isMemory() || opr.length() != 1) return false;
				if(!type.hasSize(opr.getSize())) return false;
				return opr.getObject(0) instanceof Long;
			}
			case 'J': // Relative offset is a number.
			case 'I': {
				// The operator must be a immediate value.
				if(!opr.isImmediate()) return false;
				
				// The number needs to fit the target size.
				return type.hasSizeAboveOrEqual(opr.getSize());
			}
			case 'M': {
				// The operator must be a memory pointer.
				if(!opr.isMemory()) return false;
				// TODO: Check with the manuals if 'M' does not have to specify size of the pointer!
				
				// return type.hasSize(opr.getSize());
				return type.hasSizeAboveOrEqual(opr.getSize());
			}
			case 'C': {
				if(!opr.isRegister()) return false;
				return opr.getRegister(0).type == RegisterType.control;
			}
			case 'D': {
				if(!opr.isRegister()) return false;
				return opr.getRegister(0).type == RegisterType.debug;
			}
			case 'S': {
				if(!opr.isRegister()) return false;
				return opr.getRegister(0).type == RegisterType.segment;
			}
			case 'R': // Part of a ModR/M byte but can never be a memory pointer.
			case 'G': {
				if(!opr.isRegister()) return false;
				
				// If the register is a special group register then it's not allowed.
				if(opr.getRegister(0).matchesAny(RegisterType.special)) return false;
				
				return type.hasSize(opr.getSize());
			}
			case 'E': {
				if(opr.isMemory())
					return type.hasSize(opr.getSize());
				
				if(opr.isRegister()) {
					RegisterX86 reg = opr.getRegister(0);
					
					// The operand must be a register and cannot be segments/debug/control etc ...
					return reg.matchesAny(RegisterType.r8, RegisterType.r16, RegisterType.r32, RegisterType.r64)
						&& type.hasSize(opr.getSize());
				}
				
				return false;
			}
			case 'F': return false; // TODO: Implement flags registers.
			case 'X': return false; // TODO: Implement memory DS:eSI
			case 'Y': return false; // TODO: Implement memory ES:eDI
		}
		
		return false;
	}
	
	
	private final Map<String, List<AsmOp>> map;
	private AsmLoader(List<AsmOp> instructions) {
		map = new LinkedHashMap<>();
		
		for(AsmOp op : instructions) {
			List<AsmOp> list = map.getOrDefault(op.getMnemonic(), new ArrayList<>());
			if(!map.containsKey(op.getMnemonic())) map.put(op.getMnemonic(), list);
			list.add(op);
		}
		
		// Make all elements unmodifiable so that we cannot accidently remove them.
		for(String key : map.keySet()) {
			map.put(key, Collections.unmodifiableList(map.get(key)));
		}
	}
	
	/**
	 * Returns a list containing all the {@code AsmOp} instructions that
	 * has the same name as the {@code mnemonic} specified.
	 * @param	mnemonic	the mnemonic used to search for the instructions
	 * @return	A unmodifiable list containing {@code AsmOp}.
	 */
	public static List<AsmOp> get(AsmMnm mnemonic) {
		List<AsmOp> list = INSTRUCTIONS.map.get(mnemonic.name());
		if(list == null) return Collections.emptyList();
		return list;
	}
}
