package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.parser.expr.NameExpr;
import me.hardcoded.compiler.parser.stat.GotoStat;
import me.hardcoded.compiler.parser.type.Reference;

import java.sql.Ref;
import java.util.*;

// Priority rules
// Global is checked last
// Lables before functions in references
// Locals before all globals
// Locals before all globals when calls
// Only reference and call can get function pointers
// (&function) (function())
// You cannot get references from overloaded functions
// TODO: Keep a list of references that needs to be imported
public class ProgramScope {
	private int count;
	private int tempCount;
	private final LinkedList<DataScope<Locals>> localScope;
	private final LinkedList<DataScope<Labels>> labelScope;
	private final Map<String, Reference> importedReference;
	private final Set<Reference> functions;
	
	public ProgramScope() {
		this.importedReference = new HashMap<>();
		this.localScope = new LinkedList<>();
		this.labelScope = new LinkedList<>();
		this.functions = new HashSet<>();
	}
	
	/**
	 * This will create a new block of variables. This represents different orders of visibility.
	 *
	 * The first time this is called will be the block containing GLOBAL variables.
	 * The second time it will contain LOCAL variables.
	 * The third time it will contain LAMBDA variables.
	 */
	public void pushVariableBlock() {
		localScope.addLast(new DataScope<>(Locals::new));
	}
	
	public void popVariableBlock() {
		localScope.removeLast();
	}
	
	public void pushLabelBlock() {
		labelScope.addLast(new DataScope<>(Labels::new));
	}
	
	public void popLabelBlock() {
		labelScope.removeLast();
	}
	
	public Reference addFunc(String name) {
		// TODO: Overloading
		
		if (getFunc(name) != null) {
			return null;
		}
		
		Reference reference = new Reference(name, count++, Reference.FUNCTION);
		functions.add(reference);
		return reference;
	}
	
	public Reference getFunc(String name) {
		for (Reference ref : functions) {
			if (name.equals(ref.getName())) {
				return ref;
			}
		}
		
		return null;
	}
	
	public void pushLocals() {
		localScope.getLast().pushScope();
	}
	
	public void popLocals() {
		localScope.getLast().popScope();
	}
	
	public Reference addLocalVariable(String name) {
		return localScope.getLast().getScope().addLocal(name);
	}
	
	public Reference getVariable(String name) {
		Iterator<DataScope<Locals>> iter = localScope.descendingIterator();
		
		while (iter.hasNext()) {
			Iterator<Locals> iter2 = iter.next().getAllScopes().descendingIterator();
			while (iter2.hasNext()) {
				Reference reference = iter2.next().getLocal(name);
				if (reference != null) {
					return reference;
				}
			}
		}
		
		return null;
	}
	
	public Reference getLocal(String name) {
		Iterator<Locals> iter = localScope.getLast().getAllScopes().descendingIterator();
		while (iter.hasNext()) {
			Reference reference = iter.next().getLocal(name);
			if (reference != null) {
				return reference;
			}
		}
		
		return null;
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
	
	public Map<String, Reference> getImportedReferences() {
		return importedReference;
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
	
	public Reference createImportedReference(String name) {
		Reference reference = importedReference.get(name);
		if (reference != null) {
			return reference;
		}
		
		reference = new Reference(name, count++, Reference.IMPORT);
		importedReference.put(name, reference);
		return reference;
	}
	
	public Reference createEmptyReference(String name) {
		return new Reference(name, -1 - (tempCount++), 0);
	}
	
	public class Locals {
		public final Map<String, Reference> locals;
		
		private Locals() {
			this.locals = new HashMap<>();
		}
		
		public Reference addLocal(String name) {
			if (locals.containsKey(name)) {
				return null;
			}
			
			Reference reference = new Reference(name, count++, 0);
			locals.put(name, reference);
			return reference;
		}
		
		public Reference getLocal(String name) {
			return locals.get(name);
		}
		
		@Override
		public String toString() {
			return locals.toString();
		}
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
			
			Reference reference = new Reference(name, count++, Reference.LABEL);
			definedLabels.put(name, reference);
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
