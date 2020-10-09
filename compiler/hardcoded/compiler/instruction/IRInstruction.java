package hardcoded.compiler.instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import hardcoded.compiler.Identifier;
import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.expression.AtomExpr;
import hardcoded.compiler.expression.LowType;
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
public class IRInstruction implements Iterable<IRInstruction> {
	IRInstruction prev;
	IRInstruction next;
	
	public List<Param> params = new ArrayList<>();
	public IRType op = IRType.nop;
	private Atom size;
	
	/**
	 * Returns the first element found in the list.
	 * @return the first element found in the list
	 */
	public IRInstruction first() {
		IRInstruction inst = this;
		while(inst.prev != null) {
			inst = inst.prev;
		}
		
		return inst;
	}
	
	/**
	 * Returns the last element found in the list.
	 * @return the last element found in the list
	 */
	public IRInstruction last() {
		IRInstruction inst = this;
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
		int size = 1;
		IRInstruction curr = next;
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
	public IRInstruction remove() {
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
	 * @see #append(IRInstruction)
	 */
	public void add(IRInstruction e) {
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
	 * @see #add(IRInstruction)
	 */
	public void append(IRInstruction e) {
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
	public IRInstruction get(int index) {
		IRInstruction curr = this;
		if(index == 0) return this;
		for(int i = 1; i <= index; i++) {
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
	public boolean contains(IRInstruction e) {
		return indexOf(e) != -1;
	}
	
	/**
	 * Returns the index of the object found otherwise {@code -1}.
	 * 
	 * @param	e	the object to check
	 * @return	the index of the object found otherwise {@code -1}
	 */
	public int indexOf(IRInstruction e) {
		if(e == null) return -1;
		
		int index = 0;
		IRInstruction curr = this;
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
	public IRInstruction[] toArray() {
		IRInstruction[] array = new IRInstruction[length() + 1];
		IRInstruction curr = this;
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
	public Iterator<IRInstruction> iterator() {
		return new Iterator<IRInstruction>() {
			private IRInstruction curr = IRInstruction.this;
			
			public boolean hasNext() { return curr != null; }
			public IRInstruction next() {
				IRInstruction last = curr;
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
	private void requireAbsent(IRInstruction e) {
		IRInstruction first = first();
		for(IRInstruction a : e) {
			if(first.contains(a))
				throw new IllegalArgumentException("The element was already present in the list");
		}
	}
	
	
	// ==================== NON LIST METHODS ==================== //
	private static final AtomicInteger atomic_reg = new AtomicInteger();
	private static final AtomicInteger atomic = new AtomicInteger();
	
	public static void reset_counter() { atomic_reg.set(0); }
	public static Param temp(LowType size, String name) { return new Reg(name, size, atomic_reg.getAndIncrement()); }
	public static Param temp(LowType size) { return new Reg(size, atomic_reg.getAndIncrement()); }
	
	public static final Param NONE = new Param() {
		public boolean equals(Object obj) { return false; }
		public String toString() { return "..."; }
		public String getName() { return null; }
		public int getIndex() { return -1; }
		public LowType getSize() { return null; }
	};
	
	private static final boolean DEBUG_SIZE = false;
	private static String createValue(int value) {
		StringBuilder sb = new StringBuilder();
		
		while(true) {
			int v = (value) % 26;
			sb.insert(0, (char)(v + 97));
			
			if(value > 25) {
				value = ((value - v) / 26) - 1;
			} else break;
		}
		
		return sb.toString();
	}
	
	public static interface Param {
		/**
		 * Returns the name of this register.
		 * @return the name of this register
		 */
		public String getName();
		
		/**
		 * Returns the index of a register or {@code -1} if the register is a variable register.
		 * @return
		 */
		public int getIndex();
		
		/**
		 * Returns the size of this register.
		 * @return the size of this register
		 */
		public LowType getSize();
	}
	
	/**
	 * This is a parameter used for debuging the code and will never be outputed to the end user.
	 * 
	 * @author HardCoded
	 */
	public static class DebugParam implements Param {
		private final Object object;
		
		public DebugParam(Object object) {
			this.object = object;
		}
		
		public LowType getSize() { return null; }
		public int getIndex() { return -1; }
		public String getName() { return null; }
		public String toString() { return Objects.toString(object); }
	}
	
	public static final class Reg implements Param {
		public String name;
		public int index;
		public LowType size;
		
		/**
		 * There are two types of register. Either they are generated or
		 * they are given by the coder.
		 */
		public boolean isTemporary;
		
		public Reg(LowType type, int index) {
			this(null, type, index);
		}
		
//		public Reg(int size, int index) { this(null, LowType.get(size, 0), index); }
//		public Reg(String name, int size, int index) { this(name, LowType.get(size, 0), index); }
		
		public Reg(String name, LowType size, int index) {
			this.index = index;
			this.name = name;
			this.size = size;
			
			if(name == null)
				isTemporary = true;
		}
		
		public LowType getSize() {
			return size;
		}
		
		public String getName() {
			return name;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String toString() {
			if(isTemporary) {
				return "$" + createValue(index).toUpperCase() + (DEBUG_SIZE ? (":" + size):"");
			}
			
			return "@" + name + (DEBUG_SIZE ? (":" + size):"");
		}
	}
	
	// Reference to a label
	// TODO: Fix this class.
	public static class RefReg implements Param {
		public String label;
		public int index;
		
		public RefReg(String label, int index) {
			this.label = label;
			this.index = index;
		}
		
		public String getName() {
			return label;
		}
		
		// TODO: We need to get the size of a reference register.
		public LowType getSize() {
			return null;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String toString() {
			return label + "[" + index + "]";
		}
	}
	
	public static class NumberReg implements Param {
		public long value;
		public LowType size;
		
		public NumberReg(AtomExpr expr) {
			this(expr.i_value, expr.atomType);
		}
		
		public NumberReg(long value, LowType size) {
			this.value = value;
			this.size = size;
		}
		
		public long value() {
			return value;
		}
		
		public LowType getSize() {
			return size;
		}
		
		public String getName() {
			return null;
		}
		
		public int getIndex() {
			return -1;
		}
		
		public String toString() {
			return Long.toString(value);
		}
	}
	
	public static class DataParam implements Param {
		public Object obj;
		public int index;
		
		// FIXME: Remove the Object and replace with the type [ String ]
		public DataParam(Object obj, int index) {
			this.index = index;
			this.obj = obj;
		}
		
		public LowType getSize() {
			return null;
		}
		
		public String getName() {
			return null;
		}
		
		public int getIndex() {
			return -1;
		}
		
		public String toString() {
			return "[" + index + "] = '" + Objects.toString(obj) + "'";
		}
	}
	
	// Used for functions. Data and more.
	public static class LabelParam implements Param {
		public String name;
		public String rawName;
		public boolean compiler;
		
		public LabelParam(String name) {
			this(name, true);
		}
		
		public LabelParam(String name, boolean compiler) {
			this.compiler = compiler;
			this.rawName = name;
			this.name = (compiler ? "_":"") + name + (compiler ? ("_" + atomic.getAndIncrement() + ""):"");
		}
		
		public LowType getSize() {
			return null;
		}
		
		public String getName() {
			return rawName;
		}
		
		public int getIndex() {
			return -1;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public static class FunctionLabel extends LabelParam {
		public Identifier ident;
		// TODO: Remove any reference to Identifier inside params!
		
		public FunctionLabel(Identifier ident) {
			super(ident.toString(), true);
			this.ident = ident;
		}
		
		public String getName() {
			return ident.name();
		}
		
		public String toString() {
			return ident.name();
		}
	}
	
	
	public Param getParam(int index) {
		return params.get(index);
	}
	
	public Param getLastParam() {
		return params.get(params.size() - 1);
	}
	
	public IRInstruction() {}
	public IRInstruction(IRType op) {
		this.op = op;
	}
	
	public IRInstruction(IRType op, Param... regs) {
		this.op = op;
		this.params.addAll(Arrays.asList(regs));
	}
	
	public IRType type() {
		return op;
	}
	
	public IRInstruction next() {
		return next;
	}
	
	public IRInstruction prev() {
		return prev;
	}
	
	public Atom sizeType() {
		return size;
	}
	
	public boolean hasNeighbours() {
		return (next != null) || (prev != null);
	}
	
	public LowType calculateSize() {
		if(params.isEmpty()) return null;
		
		if(op == IRType.call) return params.get(1).getSize();
		
		if(op == IRType.brz
		|| op == IRType.bnz
		|| op == IRType.br) {
			return null;
		}
		
		return params.get(0).getSize();
	}
	
	@Override
	public String toString() {
		if(op == IRType.label) return params.get(0) + ":";
		if(params.isEmpty()) return Objects.toString(op);
		
		LowType size = calculateSize();
		return String.format("%-8s%-8s         [%s]", op, (size == null ? "":size), StringUtils.join("], [", params));
	}
}