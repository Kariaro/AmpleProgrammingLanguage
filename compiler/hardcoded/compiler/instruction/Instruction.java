package hardcoded.compiler.instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import hardcoded.compiler.Expression;
import hardcoded.compiler.Expression.AtomExpr;
import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.constants.Insts;
import hardcoded.utils.StringUtils;

/**
 * This class is designed as to work like a double linked list and a
 * three register instruction set class. This class allows for greater
 * freedom in instructions are handled. Because all instructions are
 * linked with eachother it is easier to get the prevous or next instruction.<p>
 * 
 * A three register instruction set is when the instructions uses three
 * registers for a given operation.
 * 
 * @author HardCoded
 * 
 * @see List
 */
public class Instruction implements Iterable<Instruction> {
	private Instruction prev;
	private Instruction next;
	
	public List<Reg> params = new ArrayList<>();
	public Insts op = Insts.nop;
	private AtomType size;
	
	/**
	 * Returns the first element found in the list.
	 * @return the first element found in the list
	 */
	public Instruction first() {
		Instruction inst = this;
		while(inst.prev != null) {
			inst = inst.prev;
		}
		
		return inst;
	}
	
	/**
	 * Returns the last element found in the list.
	 * @return the last element found in the list
	 */
	public Instruction last() {
		Instruction inst = this;
		while(inst.next != null) {
			inst = inst.next;
		}
		
		return inst;
	}
	
	/**
	 * Returns the length of the list.
	 * @return the length of the list
	 */
	public int length() {
		int size = 0;
		Instruction curr = next;
		while(curr != null) {
			size ++;
			curr = curr.next;
		}
		
		return size;
	}
	
	/**
	 * Remove this element from the list.<p>
	 * 
	 * This method will return the previous element if not {@code null}
	 * otherwise it will return the next element. If both the previous and next
	 * neighbours was {@code null} it will {@code null}.
	 * 
	 * @return the closest neighbour
	 */
	public Instruction remove() {
		if(next == null) {
			if(prev == null) return null;
			prev.next = null;
			return prev;
		}
		
		if(prev == null) {
			next.prev = null;
			return next;
		}
		
		prev.next = next;
		next.prev = prev;
		return prev;
	}
	
	/**
	 * Insert a new element infront of this element.
	 * 
	 * @param	e	the element to be inserted
	 * @throws	NullPointerException
	 *			The element was null
	 * @throws	IllegalArgumentException
	 * 			The element was already present in the list
	 * 
	 * @see #append(Instruction)
	 */
	public void add(Instruction e) {
		if(e == null) throw new NullPointerException();
		requireAbsent(e);
		
		if(next == null) {
			e.prev = this;
			next = e;
			return;
		}
		
		next.prev = e.last();
		e.last().next = next;
		e.first().prev = this;
		next = e;
		return;
	}
	
	/**
	 * Append the provided element to the end of the list.
	 * 
	 * @param	e	the element to append to the end of the list
	 * @throws	NullPointerException
	 * 			The element was null
	 * @throws	IllegalArgumentException
	 * 			The element was already present in the list
	 * 
	 * @see #add(Instruction)
	 */
	public void append(Instruction e) {
		last().add(e.first());
	}
	
	/**
	 * Returns the element at the specified position in the list.
	 * 
	 * @param	index	index of the element returned
	 * @return	the element at the specified position in the list
	 * @throws	ArrayIndexOutOfBoundsException
	 * 			If the {@code index} was outside the bounds of the list
	 */
	public Instruction get(int index) {
		Instruction curr = this;
		if(index == 0) return this;
		for(int i = 1; i < index; i++) {
			curr = curr.next;
			if(curr == null) throw new ArrayIndexOutOfBoundsException(i);
			
			if(i == index) {
				return curr;
			}
		}
		
		throw new ArrayIndexOutOfBoundsException(index);
	}
	
	/**
	 * Returns {@code true} if the element was found in the list.
	 * 
	 * @param	e	the element to be checked
	 * @return	{@code true} if the element was found in the list
	 */
	public boolean contains(Instruction e) {
		return indexOf(e) != -1;
	}
	
	/**
	 * Returns the index of the object found otherwise {@code -1}.
	 * 
	 * @param	e	the object to check
	 * @return	the index of the object found otherwise {@code -1}
	 */
	public int indexOf(Instruction e) {
		if(e == null) return -1;
		
		int index = 0;
		Instruction curr = this;
		while(curr != null) {
			if(curr == e) return index;
			
			index++;
			curr = curr.next;
		}
		
		return -1;
	}
	
	/**
	 * Returns this list as a array.
	 * @return this list as a array
	 */
	public Instruction[] toArray() {
		Instruction[] array = new Instruction[length() + 1];
		Instruction curr = this;
		for(int i = 0; i < array.length; i++) {
			array[i] = curr;
			curr = curr.next;
		}
		
		return array;
	}
	
	/**
	 * Returns a iterator of the elements in this list.
	 * @return a iterator of the elements in this list
	 */
	public Iterator<Instruction> iterator() {
		return new Iterator<Instruction>() {
			private Instruction curr = Instruction.this;
			
			public boolean hasNext() { return curr != null; }
			public Instruction next() {
				Instruction last = curr;
				curr = curr.next;
				return last;
			}
			
			public void remove() {
				if(curr == null) return;
				curr = curr.remove();
			}
		};
	}
	
	/**
	 * Requires the element provided to not be found in the list. If the
	 * element was found a <code>IllegalArgumentException</code> will be thrown.
	 * 
	 * @param	e	the element to check
	 * @throws	IllegalArgumentException
	 * 			If the element or subelements was present in the list
	 */
	private void requireAbsent(Instruction e) {
		Instruction first = first();
		for(Instruction a : e) {
			if(first.contains(a))
				throw new IllegalArgumentException("The element was already present in the list");
		}
	}
	
	
	// ==================== NON LIST METHODS ==================== //
	private static final AtomicInteger atomic_reg = new AtomicInteger();
	private static final AtomicInteger atomic = new AtomicInteger();
	public static void reset_counter() { atomic_reg.set(0); }
	public static Reg temp() { return new Reg(atomic_reg.getAndIncrement()); }
	public static Reg temp(String name) { return new NamedReg(name, atomic_reg.getAndIncrement()); }
	public static Reg temp(Reg reg) { return reg != null ? reg:temp(); }
	public static Reg temp(AtomType size) { return new Reg(size, atomic_reg.getAndIncrement()); }
	public static Reg temp(AtomType size, Reg reg) { return reg != null ? reg:temp(size); }
	
	
	private static final boolean DEBUG_SIZE = false;
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
		
		public Reg() {}
		public Reg(int index) {
			this.index = index;
		}
		
		public Reg(AtomType size, int index) {
			this.index = index;
			this.size = size;
		}
		
		@Override
		public String toString() {
			return "$" + createValue(index).toUpperCase() + (DEBUG_SIZE ? (":" + size):"");
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
			return name + (DEBUG_SIZE ? (":" + size):"");
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
	
	public Reg getParam(int index) {
		return params.get(index);
	}
	
	public Reg getLastParam() {
		return params.get(params.size() - 1);
	}
	
	public static class NumberReg extends Reg {
		public AtomExpr expr;
		
		public NumberReg(AtomExpr expr) {
			this.expr = expr;
			this.size = expr.calculateSize();
		}
		
		public String toString() {
			return Objects.toString(expr) + (DEBUG_SIZE ? (":" + size):"");
		}
	}
	
	public static class Register extends Reg {
		public Register(int index, AtomType size) {
			this.index = index;
			this.size = size;
		}
		
		public String toString() {
			return "$" + Integer.toString(index) + (DEBUG_SIZE ? (":" + size):"");
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
	
	public Instruction() {}
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
	
	public AtomType sizeType() {
		return size;
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
		
		return String.format("%-8s%-8s         [%s]", op, (size != null ? size:""), StringUtils.join("], [", params));
	}
}