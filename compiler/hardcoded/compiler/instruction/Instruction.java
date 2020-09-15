package hardcoded.compiler.instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import hardcoded.compiler.Expression;
import hardcoded.compiler.Expression.AtomExpr;
import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.constants.Insts;
import hardcoded.utils.StringUtils;

/**
 * This is a doubly linked list.
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
		public AtomType size;
		
		public Reg() {
			
		}
		
		public Reg(int index) {
			this.index = index;
		}
		
		public Reg(AtomType size, int index) {
			this.index = index;
			this.size = size;
		}
		
		@Override
		public String toString() {
			return "$" + createValue(index).toUpperCase() + ":" + size;
		}
	}
	
	public static final Reg NONE = new Reg() {
		{
			size = null;
			index = -1;
		}
		
		public boolean equals(Object obj) { return false; }
		public String toString() { return "..."; }
	};
	
	public static class RefReg extends Reg {
		public String label;
		
		public RefReg(String label, int index) {
			this.label = label;
			this.index = index;
		}
		
		@Override
		public String toString() {
			return label + "[" + index + "]";
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
			return name + ":" + size;
		}
	}
	
	public static class CallReg extends Reg {
		public String name;
		
		public CallReg(String name) {
			this.name = name;
		}
		
		public CallReg(AtomType size, String name) {
			this.name = name;
			this.size = size;
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
	
	public static Reg temp(AtomType size) { return new Reg(size, atomic_reg.getAndIncrement()); }
	public static Reg temp(AtomType size, Reg reg) { return reg != null ? reg:temp(size); }
	
	public static class NumberReg extends Reg {
		public AtomExpr expr;
		
		public NumberReg(AtomExpr expr) {
			this.expr = expr;
			this.size = expr.calculateSize();
		}
		
		public String toString() {
			return Objects.toString(expr) + ":" + size;
		}
	}
	
	public static class ObjectReg extends Reg {
		public Object obj;
		
		public ObjectReg(Object obj) {
			this.obj = obj;
			
			if(obj instanceof Expression) {
				size = ((Expression)obj).calculateSize();
			}
		}
		
		@Override
		public String toString() {
			return Objects.toString(obj);
		}
	}
	
	private static final AtomicInteger atomic = new AtomicInteger();
	/**
	 * This is a label.
	 * @author HardCoded
	 */
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
	public AtomType size;
	// Size modifier
	
	public List<Reg> params = new ArrayList<>();
	public Insts op;
	
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
	
	/**
	 * Remove this instruction from its neighbours.
	 */
	public Instruction remove() {
		if(next == null) {
			if(prev == null) {
				return null;
			} else {
				prev.next = null;
				return prev;
			}
		} else {
			if(prev == null) {
				// curr next
				next.prev = null;
				return next;
			} else {
				prev.next = next;
				next.prev = prev;
				return prev;
			}
		}
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
	
	public int length() {
		int result = 0;
		Instruction inst = this;
		while(inst.next != null) {
			result ++;
			inst = inst.next;
		}
		
		return result;
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
		
		AtomType size = params.get(0).size;
		if(op == Insts.call) size = params.get(1).size;
		
		if(op == Insts.brz
		|| op == Insts.bnz
		|| op == Insts.br) {
			size = null;
		}
		
		return op + "\t" + (size != null ? size:"   ")
				  + "\t\t [" + StringUtils.join("], [", params) + "]";
	}
}
