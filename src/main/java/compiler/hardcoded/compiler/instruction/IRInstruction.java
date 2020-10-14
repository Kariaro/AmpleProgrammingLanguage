package hardcoded.compiler.instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import hardcoded.compiler.Identifier;
import hardcoded.compiler.expression.AtomExpr;
import hardcoded.compiler.expression.LowType;
import hardcoded.utils.StringUtils;

/**
 * <s>This class is designed as to work like a double linked list and a
 * three register instruction set class. This class allows for greater
 * freedom in instructions are handled. Because all instructions are
 * linked with eachother it is easier to get the prevous or next instruction.</s><p>
 * 
 * A three register instruction set is when the instructions uses three
 * registers for a given operation.
 * 
 * @author HardCoded
 * 
 * @see List
 */
public class IRInstruction {
	private static final AtomicInteger atomic = new AtomicInteger();
	private static final boolean DEBUG_SIZE = false;
	public static final Param NONE = new Param() {
		public boolean equals(Object obj) { return false; }
		public String toString() { return "..."; }
		public LowType getSize() { return null; }
	};
	
	
	public List<Param> params = new ArrayList<>();
	public IRType op = IRType.nop;
	@Deprecated
	private LowType size;
	
	
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
		public default String getName() {
			return null;
		}
		
		/**
		 * Returns the index of a register or {@code -1} if the register is a variable register.
		 * @return
		 */
		public default int getIndex() {
			return -1;
		}
		
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
		public String toString() { return Objects.toString(object); }
	}
	
	public static final class Reg implements Param {
		public final String name;
		public int index;
		public LowType size;
		
		/**
		 * There are two types of register. Either they are generated or
		 * they are given by the coder.
		 */
		public final boolean isTemporary;
		
		public Reg(LowType type, int index) {
			this(null, type, index);
		}
		
		public Reg(String name, LowType size, int index) {
			this.index = index;
			this.name = name;
			this.size = size;
			this.isTemporary = (name == null);
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
		
		public String toString() {
			return name;
		}
	}
	
	public static class FunctionLabel extends LabelParam {
		@Deprecated
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
	
	@Deprecated public Param getLastParam() {
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
	
	// TODO: Remove
	@Deprecated public LowType sizeType() {
		return size;
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