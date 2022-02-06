package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.parser.type.Reference;

public interface InstParam {
	
	class Ref implements InstParam {
		private final InstRef ref;
		
		public Ref(InstRef ref) {
			this.ref = ref;
		}
		
		public InstRef getReference() {
			return ref;
		}
		
		@Override
		public String toString() {
			return ref.toString();
		}
	}
	
	
	class Num implements InstParam {
		private final String TEMP;
		
		public Num(String TEMP) {
			this.TEMP = TEMP;
		}
		
		@Override
		public String toString() {
			return TEMP;
		}
	}
}
