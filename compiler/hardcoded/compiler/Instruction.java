package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.constants.Insts;
import hardcoded.utils.StringUtils;

public class Instruction {
	public static class Reg {
		public int index;
	}
	
	public Instruction next;
	public Instruction prev;
	
	public List<Reg> params = new ArrayList<>();
	public Insts op;
	
	public Instruction() {
		
	}
	
	public Instruction(Insts op) {
		this.op = op;
	}
	
	public Insts type() {
		return op;
	}
	
	@Override
	public String toString() {
		return op + " " + StringUtils.join(", ", params);
	}
}
