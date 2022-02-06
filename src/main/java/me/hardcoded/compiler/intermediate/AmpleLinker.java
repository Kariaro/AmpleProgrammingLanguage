package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.intermediate.generator.InstGenerator;
import me.hardcoded.compiler.intermediate.inst.Inst;
import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.compiler.intermediate.inst.Opcode;
import me.hardcoded.compiler.intermediate.inst.Procedure;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.visualization.ParseTreeVisualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for linking {@link LinkableObject}.
 *
 * This linker will create a {@link InstFile} that contains:
 * <ul>
 *   <li>Instructions</li>
 * </ul>
 *
 * @author HardCoded
 */
public class AmpleLinker {
	
	public AmpleLinker() {
	
	}
	
	public InstFile link(LinkableObject main, List<LinkableObject> list) {
		InstFile file = new InstFile();
		
		List<LinkableObject> allObjects = new ArrayList<>();
		allObjects.add(main);
		allObjects.addAll(list);
		
		for (LinkableObject link : allObjects) {
			System.out.println(link + ", " + link.getFile());
		}
		
		ExportMap exportMap = new ExportMap();
		if (!checkImports(exportMap, allObjects)) {
			throw new ParseException("Project is not linkable");
		}
		
		InstGenerator generator = new InstGenerator(file, exportMap);
		for (LinkableObject link : allObjects) {
			generator.generate(link);
		}
		
		new ParseTreeVisualization().show(main.getProgram());
		
		for (Procedure proc : file.getProcedures()) {
			System.out.println("# proc " + proc);
			for (Inst inst : proc.getInstructions()) {
				if (inst.getOpcode() == Opcode.LABLE) {
					System.out.println("    " + inst);
				} else {
					System.out.println("        " + inst);
				}
			}
		}
		
		return file;
	}
	
	private boolean checkImports(ExportMap exportMap, List<LinkableObject> list) {
		for (LinkableObject link : list) {
			if (!exportMap.add(link)) {
				return false;
			}
		}
		
		for (LinkableObject link : list) {
			if (!exportMap.containsThrowErrors(link)) {;
				return false;
			}
		}
		
		return true;
	}
	
	public static class ExportMap {
		private final Map<String, Reference> functions;
		private final Map<String, Reference> variables;
		
		private ExportMap() {
			functions = new HashMap<>();
			variables = new HashMap<>();
		}
		
		private boolean add(LinkableObject obj) {
			for (Reference reference : obj.getExportedReferences()) {
				if (reference.isFunction() && functions.put(reference.getName(), reference) != null) {
					throw new ParseException("(%s) The project already exports a function '%s'", obj.getFile(), reference.getName());
				}
				
				if (reference.isVariable() && variables.put(reference.getName(), reference) != null) {
					throw new ParseException("(%s) The project already exports a variable '%s'", obj.getFile(), reference.getName());
				}
			}
			
			return true;
		}
		
		public Reference getReference(Reference reference) {
			if (reference.isFunction()) {
				return functions.get(reference.getName());
			}
			
			if (reference.isVariable()) {
				return variables.get(reference.getName());
			}
			
			return null;
		}
		
		public boolean contains(LinkableObject obj) {
			for (Reference reference : obj.getImportedReferences()) {
				if (getReference(reference) == null) {
					return false;
				}
			}
			
			return true;
		}
		
		public boolean containsThrowErrors(LinkableObject obj) {
			for (Reference reference : obj.getImportedReferences()) {
				if (getReference(reference) == null) {
					throw new ParseException("(%s) The imported symbol '%s' was not found in the project", obj.getFile(), reference.getName());
				}
			}
			
			return true;
		}
	}
}
