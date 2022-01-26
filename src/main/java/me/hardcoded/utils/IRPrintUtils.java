package me.hardcoded.utils;

import java.util.List;
import java.util.ListIterator;

import me.hardcoded.compiler.instruction.*;

public class IRPrintUtils {
	public static String printPretty(IRProgram program) {
		StringBuilder sb = new StringBuilder();
		sb.append(".data.strings:\n");
		{
			int index = 0;
			for(String s : program.getContext().getStrings()) {
				sb.append("%4d:   \"%s\"\n".formatted(index++, StringUtils.escapeString(s)));
			}
			
			sb.append("\n");
		}
		
		for(IRFunction func : program.getFunctions()) {
			sb.append(printPretty(func)).append("\n");
		}
		
		return sb.toString().trim();
	}
	
	public static String printPretty(IRFunction func) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n").append(func).append("\n");
		
		List<IRInstruction> list = func.getInstructions();
		for(int i = 0, line = 0, len = list.size(); i < len; i++) {
			IRInstruction inst = list.get(i);
			
			if(inst.type() == IRType.label) {
				sb.append("\n%4d: %s\n".formatted(line, inst));
			} else {
				sb.append("%4d:   %s\n".formatted(line, inst));
				line++;
			}
		}
		
		return sb.toString().trim();
	}
	
	public static class IRListIterator implements ListIterator<IRInstruction> {
		private final ListIterator<IRInstruction> iterator;
		public final List<IRInstruction> list;
		
		private IRListIterator(List<IRInstruction> list) {
			this.list = list;
			this.iterator = list.listIterator();
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		@Override
		public IRInstruction next() {
			return iterator.next();
		}
		
		@Override
		public boolean hasPrevious() {
			return iterator.hasPrevious();
		}
		
		@Override
		public IRInstruction previous() {
			return iterator.previous();
		}
		
		@Override
		public int nextIndex() {
			return iterator.nextIndex();
		}
		
		@Override
		public int previousIndex() {
			return iterator.previousIndex();
		}
		
		@Override
		public void remove() {
			iterator.remove();
		}
		
		@Override
		public void set(IRInstruction e) {
			iterator.set(e);
		}
		
		@Override
		public void add(IRInstruction e) {
			iterator.add(e);
		}
		
		public int index() {
			return iterator.nextIndex() - 1;
		}
		
		public IRInstruction peakNext() {
			int index = nextIndex();
			if(index >= list.size()) return null;
			return list.get(index);
		}
		
		public IRInstruction peakPrevious() {
			int index = previousIndex();
			if(index < 0) return null;
			return list.get(index);
		}
	}
	
	public static IRListIterator createIterator(List<IRInstruction> list) {
		return new IRListIterator(list);
	}
	
	public static IRListIterator createIterator(IRFunction func) {
		return createIterator(func.getInstructions());
	}
}
