package hardcoded.compiler.instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import hardcoded.compiler.Expression.AtomType;
import hardcoded.compiler.constants.Insts;
import hardcoded.utils.StringUtils;

/**
 * This is a dualylinked list instruction.
 * 
 * @author HardCoded
 */
public class Instruction {
	private static String createValue(int value) {
		StringBuilder sb = new StringBuilder();
		
		while(true) {
			int v = (value) % 26;
			sb.insert(0, (char)(v + 97));
			
			if(value > 25) {
				value = ((value - v) / 26) - 1;
			} else {
				break;
			}
		}
		return sb.toString();
	}
	
	public static class Reg {
		public int index;
		
		public Reg() {
			
		}
		
		public Reg(int index) {
			this.index = index;
		}
		
		@Override
		public String toString() {
			return "$" + createValue(index);
		}
	}
	
	public static class NamedReg extends Reg {
		public String name;
		
		public NamedReg(String name) {
			this.name = name;
		}
		
		public NamedReg(String name, int index) {
			this.index = index;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static final AtomicInteger atomic_reg = new AtomicInteger();
	public static Reg temp() { return new Reg(atomic_reg.getAndIncrement()); }
	public static Reg temp(String name) { return new NamedReg(name, atomic_reg.getAndIncrement()); }
	public static Reg temp(Reg reg) { return reg != null ? reg:temp(); }
	
	public static class ObjectReg extends Reg {
		public Object obj;
		
		public ObjectReg(Object obj) {
			this.obj = obj;
		}
		
		@Override
		public String toString() {
			return Objects.toString(obj);
		}
	}
	
	private static final AtomicInteger atomic = new AtomicInteger();
	public static class Label extends Reg {
		public String name;
		public boolean compiler;
		
		public Label(String name) {
			this(name, true);
		}
		
		public Label(String name, boolean compiler) {
			this.compiler = compiler;
			this.name = (compiler ? "_":"") + name + (compiler ? ("_" + atomic.getAndIncrement() + ""):"");
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public Instruction next;
	public Instruction prev;
	// Size modifier
	
	public List<Reg> params = new ArrayList<>();
	public Insts op;
	public AtomType size;
	
	public Instruction() {
		this.op = Insts.nop;
	}
	
	public Instruction(Insts op) {
		this.op = op;
	}
	
	public Instruction(Insts op, Reg... regs) {
		this.op = op;
		this.params.addAll(Arrays.asList(regs));
	}
	
	public Insts type() {
		return op;
	}
	
	public Instruction next() {
		return next;
	}
	
	public Instruction prev() {
		return prev;
	}
	
	public Instruction append(Instruction inst) {
		if(inst == null) return last();
		Instruction first = inst.first();
		Instruction last = last();
		last.next = first;
		first.prev = last;
		return last();
	}
	
	/**
	 * @return the last instruction in the instructions chain.
	 */
	public Instruction last() {
		Instruction inst = this; // TODO: What if this gets stuck in a loop?
		while(inst.next != null) inst = inst.next;
		return inst;
	}
	
	/**
	 * @return the first instruction in the instructions chain.
	 */
	public Instruction first() {
		Instruction inst = this; // TODO: What if this gets stuck in a loop?
		while(inst.prev != null) inst = inst.prev;
		return inst;
	}
	
	public boolean hasNeighbours() {
		return (next != null) || (prev != null);
	}
	
	@Override
	public String toString() {
		if(op == Insts.label) return params.get(0) + ":";
		if(params.isEmpty()) return Objects.toString(op);
		return op + (size != null ? (" " + size):"")
				  + " [" + StringUtils.join("], [", params) + "]";
	}
}
