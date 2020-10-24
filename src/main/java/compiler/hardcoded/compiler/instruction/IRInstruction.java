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
 * This class is a representation of this compilers internal instruction language
 * or more commonly known as its <i>immediate representation</i> language. A <i>IR</i>
 * language allows a compiler to use more advanced optimization techniques when compiling
 * input syntax.
 * 
 * 
 * <p>Each instruction in this <i>IR</i> language is written with either two or three
 * parameters with the only exception beeing the call instruction that can have a
 * variable amount of parameters.
 * 
 * 
 * @author HardCoded
 */
public class IRInstruction {
	private static final AtomicInteger atomic = new AtomicInteger();
	private static final boolean DEBUG_SIZE = false;
	public static final Param NONE = new Param() {
		public boolean equals(Object obj) { return false; }
		public String toString() { return "..."; }
	};
	
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
		public default LowType getSize() {
			return null;
		}
	}
	
	/**
	 * This is a parameter used for debuging the code and will never be outputed to the end user.
	 * 
	 * @author HardCoded
	 */
	public static class DebugParam implements Param {
		private final Object object;
		
		public DebugParam(Object object) {
			this.object = "" + object;
		}
		
		public Object getValue() {
			return object;
		}
		
		public String toString() {
			return Objects.toString(object);
		}
	}
	
	public static final class Reg implements Param {
		private final String name;
		private final LowType size;
		private final int index;
		
		/**
		 * Tells the developer if this register was made by the
		 * compiler or by the developer.
		 */
		private final boolean isTemporary;
		
		public Reg(LowType type, int index) {
			this(null, type, index);
		}
		
		public Reg(String name, LowType size, int index) {
			this.index = index;
			this.name = name;
			this.size = size;
			this.isTemporary = (name == null);
			
			if(size == null)
				throw new NullPointerException();
		}
		
		public boolean isTemporary() {
			return isTemporary;
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
				String value = StringUtils.toStringCustomBase(index, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", false);
				
				return "$" + value + (DEBUG_SIZE ? (":" + size):"");
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
		
		public int getIndex() {
			return index;
		}
		
		public String toString() {
			return label + "[" + index + "]";
		}
	}
	
	public static class NumberReg implements Param {
		private final LowType size;
		private final long value;
		
		public NumberReg(AtomExpr expr) {
			this(expr.i_value, expr.atomType);
		}
		
		public NumberReg(long value, LowType size) {
			this.value = value;
			this.size = size;
			
			if(size == null)
				throw new NullPointerException();
		}
		
		public long getValue() {
			return value;
		}
		
		public LowType getSize() {
			return size;
		}
		
		public String toString() {
			// TODO: Print this value depending on the LowType
			return Long.toString(value);
		}
	}
	
	public static class DataParam implements Param {
		private final Object obj;
		private final int index;
		
		// FIXME: Remove the Object and replace with the type [ String ]
		public DataParam(Object obj, int index) {
			this.index = index;
			this.obj = "" + obj;
		}
		
		public int getIndex() {
			return index;
		}
		
		public Object getValue() {
			return obj;
		}
		
		public String toString() {
			return "[" + index + "] = '" + Objects.toString(obj) + "'";
		}
	}
	
	// Used for functions. Data and more.
	public static class LabelParam implements Param {
		private final boolean isTemporary;
		private final String name;
		
		public LabelParam(String name) {
			this(name, true);
		}
		
		public LabelParam(String name, boolean isTemporary) {
			this.isTemporary = isTemporary;
			
			if(isTemporary) {
				this.name = "_" + name + "_" + atomic.getAndIncrement();
			} else {
				this.name = name;
			}
		}
		
		public LabelParam(String name, boolean isTemporary, Object tmp) {
			this.isTemporary = isTemporary;
			this.name = name;
		}
		
		public boolean equals(Object obj) {
			if(!(obj instanceof LabelParam)) return false;
			LabelParam param = (LabelParam)obj;
			return name.equals(param.name) && isTemporary == param.isTemporary;
		}
		
		public boolean isTemporary() {
			return isTemporary;
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public static class FunctionLabel extends LabelParam {
		private final LowType size;
		
		public FunctionLabel(Identifier ident) {
			this(ident.name(), ident.low_type());
		}
		
		public FunctionLabel(String name, LowType size) {
			super(name, false);
			this.size = size;
		}
		
		public LowType getSize() {
			return size;
		}
	}
	
	protected final List<Param> params = new ArrayList<>();
	protected IRType op = IRType.nop;
	
	// TODO: Find a way to calculate the size of a instruction duing the generation/optimization stage.
//	@Deprecated
	//private LowType test_size;
	
	public IRInstruction() {
		
	}
	
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
	
	/**
	 * Returns the parameter at the specified index.
	 * @param	index	the index of the parameter
	 * @return	the parameter at the specified index
	 */
	public Param getParam(int index) {
		return params.get(index);
	}
	
	/**
	 * Returns an unmodifiable list of this instructions parameters.
	 * @return an unmodifiable list of this instructions parameters
	 */
	public List<Param> getParams() {
		return Collections.unmodifiableList(params);
	}
	
	/**
	 * Returns the amount of parameters of this instruction has.
	 * @return the amount of parameters of this instruction has
	 */
	public int getNumParams() {
		return params.size();
	}
	
	@Deprecated public Param getLastParam() {
		return params.get(params.size() - 1);
	}
	
	public LowType getSize() {
		// TODO: Cache the size with the local size variable
		if(params.isEmpty()) return null;
		if(op == IRType.call) return params.get(1).getSize();
		if(op == IRType.brz || op == IRType.bnz || op == IRType.br) return null;
		return params.get(0).getSize();
	}
	
	public String toString() {
		if(op == IRType.label) return params.get(0) + ":";
		if(params.isEmpty()) return Objects.toString(op);
		
		LowType size = getSize();
		return String.format("%-8s%-8s         [%s]", op, (size == null ? "":size), StringUtils.join("], [", params));
	}
}