package hardcoded.compiler.instruction;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.constants.Identifier;
import hardcoded.compiler.expression.AtomExpr;
import hardcoded.compiler.expression.LowType;
import hardcoded.utils.DebugUtils;
import hardcoded.utils.StringUtils;

public interface Param {
	static final Param NONE = new Param() {
		@Override
		public boolean equals(Object obj) {
			return false;
		}
		
		@Override
		public ParamType type() {
			return ParamType.DEBUG;
		}
		
		@Override
		public String toString() {
			return "...";
		}
	};
	
	/**
	 * Returns the name of this register.
	 */
	default String getName() {
		return null;
	}
	
	/**
	 * Returns the index of a register or {@code -1} if the register is a variable register.
	 */
	default int getIndex() {
		return -1;
	}
	
	/**
	 * Returns the size of this register.
	 */
	default LowType getSize() {
		return null;
	}
	
	/**
	 * Returns the type of this parameter.
	 */
	ParamType type();
	
	/**
	 * This is a parameter used for debuging the code and will never be outputed to the end user.
	 * 
	 * @author HardCoded
	 */
	public class DebugParam implements Param {
		private final Object object;
		
		public DebugParam(Object object) {
			this.object = "" + object;
		}
		
		public Object getValue() {
			return object;
		}
		
		@Override
		public ParamType type() {
			return ParamType.DEBUG;
		}
		
		@Override
		public String toString() {
			return Objects.toString(object);
		}
	}
	
	public class RegParam implements Param {
		private final String name;
		private final LowType size;
		private final int index;
		private final boolean isTemporary;
		
		public RegParam(LowType type, int index) {
			this(null, type, index);
		}
		
		public RegParam(String name, LowType size, int index) {
			this.index = index;
			this.name = name;
			this.size = Objects.requireNonNull(size);
			this.isTemporary = (name == null);
		}
		
		// User for registers that are not parameters
		public boolean isTemporary() {
			return isTemporary;
		}
		
		@Override
		public LowType getSize() {
			return size;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public int getIndex() {
			return index;
		}
		
		@Override
		public ParamType type() {
			return ParamType.REGISTER;
		}
		
		@Override
		public String toString() {
			String size = "";
			if(DebugUtils.DEBUG_IRCODE_SIZE) {
				size = ":" + this.size;
			}
			
			if(isTemporary) {
				String value = StringUtils.toStringCustomBase(index, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", false);
				return "$%s%s".formatted(value, size);
			}
			
			return "@%s%s".formatted(name, size);
		}
	}
	
	public class RefParam implements Param {
		public String label;
		public int index;
		
		public RefParam(String label, int index) {
			this.label = label;
			this.index = index;
		}
		
		@Override
		public String getName() {
			return label;
		}
		
		@Override
		public int getIndex() {
			return index;
		}
		
		@Override
		public LowType getSize() {
			// All references are pointers
			return LowType.create(Atom.i64);
		}
		
		@Override
		public ParamType type() {
			return ParamType.REFERENCE;
		}
		
		@Override
		public String toString() {
			return "%s[%d]".formatted(label, index);
		}
	}
	
	// Used to represent numbers
	public class NumParam implements Param {
		private final LowType size;
		private final long value;
		
		public NumParam(AtomExpr expr) {
			this(expr.number(), expr.getAtomType());
		}
		
		public NumParam(long value, LowType size) {
			this.value = value;
			this.size = Objects.requireNonNull(size);
		}
		
		public long getValue() {
			return value;
		}
		
		@Override
		public LowType getSize() {
			return size;
		}
		
		@Override
		public ParamType type() {
			return ParamType.NUMBER;
		}
		
		@Override
		public String toString() {
			return Long.toString(value);
		}
	}
	
	// Used for functions. Data and more.
	public class LabelParam implements Param {
		private static final AtomicInteger atomic = new AtomicInteger();
		private final boolean isTemporary;
		private final String name;
		
		public LabelParam(String name) {
			this(name, true);
		}
		
		public LabelParam(String name, boolean isTemporary) {
			this.isTemporary = isTemporary;
			
			if(isTemporary) {
				this.name = "_%s_%s".formatted(name, atomic.getAndIncrement());
			} else {
				this.name = name;
			}
		}
		
		public LabelParam(String name, boolean isTemporary, Object tmp) {
			this.isTemporary = isTemporary;
			this.name = name;
		}
		
		public boolean isTemporary() {
			return isTemporary;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof LabelParam param)) return false;
			return name.equals(param.name) && isTemporary == param.isTemporary;
		}

		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public ParamType type() {
			return ParamType.LABEL;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	public class FunctionLabel extends LabelParam {
		private final LowType size;
		
		public FunctionLabel(Identifier ident) {
			this(ident.getName(), ident.getLowType());
		}
		
		public FunctionLabel(String name, LowType size) {
			super(name, false);
			this.size = size;
		}
		
		@Override
		public ParamType type() {
			return ParamType.FUNCTION_LABEL;
		}

		@Override
		public LowType getSize() {
			return size;
		}
	}
	
	public enum ParamType {
		// Used for number parameters
		NUMBER,
		
		// Used for registers
		REGISTER,
		
		// Used for labels
		LABEL,
		
		// Used for function labels
		FUNCTION_LABEL,
		
		// Used for reference params
		REFERENCE,
		
		// Used for debug values
		DEBUG
	}
}
