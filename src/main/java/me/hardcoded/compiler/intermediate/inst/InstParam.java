package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.parser.type.ValueType;

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
		
		@Deprecated
		public Num(int value) {
			// TODO: Allow numbers here
			this.TEMP = "" + value;
		}
		
		@Deprecated
		public Num(String TEMP) {
			// TODO: Allow numbers here
			this.TEMP = TEMP;
		}
		
		@Override
		public String toString() {
			return TEMP;
		}
	}
	
//	class Str implements InstParam {
//		private final String value;
//
//		public Str(String value) {
//			this.value = value;
//		}
//
//		public String getValue() {
//			return value;
//		}
//
//		@Override
//		public String toString() {
//			return value;
//		}
//	}
	
	class Type implements InstParam {
		private final ValueType valueType;
		
		public Type(ValueType valueType) {
			this.valueType = valueType;
		}
		
		public ValueType getValueType() {
			return valueType;
		}
		
		@Override
		public String toString() {
			return valueType.toString();
		}
	}
}
