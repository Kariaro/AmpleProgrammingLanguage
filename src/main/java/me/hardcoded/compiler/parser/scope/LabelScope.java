package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.parser.expr.NameExpr;
import me.hardcoded.compiler.parser.stat.GotoStat;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.*;

public class LabelScope {
	private final ProgramScope programScope;
	private final LinkedList<DataScope<Labels>> labelScope;
	
	LabelScope(ProgramScope programScope) {
		this.programScope = programScope;
		this.labelScope = new LinkedList<>();
	}
	
	public void clear() {
		labelScope.clear();
	}
	
	public void pushBlock() {
		labelScope.addLast(new DataScope<>(Labels::new));
	}
	
	public void popBlock() {
		labelScope.removeLast();
	}
	
	public void pushLabels() {
		labelScope.getLast().pushScope();
	}
	
	public void popLabels() {
		labelScope.getLast().popScope();
	}
	
	public Reference addLabel(String name) {
		return labelScope.getLast().getScope().addLabel(name);
	}
	
	public void addGoto(GotoStat stat) {
		labelScope.getLast().getScope().addGoto(stat);
	}
	
	public Reference getLocalLabel(String name) {
		return labelScope.getLast().getScope().getLabel(name);
	}
	
	public List<GotoStat> getGotos() {
		return labelScope.getLast().getScope().getDeclaredGotos();
	}
	
	public void addMissingLabelReference(NameExpr name) {
		labelScope.getLast().getAllScopes().getFirst().addMissingReference(name);
	}
	
	public boolean isGlobalLabelScope() {
		return labelScope.size() == 1;
	}
	
	public List<NameExpr> getMissingLabelReferences() {
		return labelScope.getLast().getAllScopes().getFirst().getMissingReferences();
	}
	
	public void addGlobalMissingLabelReference(NameExpr name) {
		labelScope.getFirst().getAllScopes().getFirst().addMissingReference(name);
	}
	
	public List<NameExpr> getGlobalMissingLabelReferences() {
		return labelScope.getFirst().getAllScopes().getFirst().getMissingReferences();
	}
	
	public class Labels {
		private final Map<String, Reference> definedLabels;
		private final List<GotoStat> declaredGotos;
		private final List<NameExpr> missingReference;
		
		private Labels() {
			this.definedLabels = new HashMap<>();
			this.declaredGotos = new ArrayList<>();
			this.missingReference = new ArrayList<>();
		}
		
		public List<GotoStat> getDeclaredGotos() {
			return declaredGotos;
		}
		
		public Set<String> getDefinedLabels() {
			return definedLabels.keySet();
		}
		
		public Reference addLabel(String name) {
			if (definedLabels.containsKey(name)) {
				return null;
			}
			
			Reference reference = new Reference(name, ValueType.UNDEFINED, programScope.count++, Reference.LABEL);
			definedLabels.put(name, reference);
			programScope.allReferences.add(reference);
			return reference;
		}
		
		public void addGoto(GotoStat stat) {
			declaredGotos.add(stat);
		}
		
		public Reference getLabel(String name) {
			return definedLabels.get(name);
		}
		
		public void addMissingReference(NameExpr expr) {
			missingReference.add(expr);
		}
		
		public List<NameExpr> getMissingReferences() {
			return missingReference;
		}
	}
}
