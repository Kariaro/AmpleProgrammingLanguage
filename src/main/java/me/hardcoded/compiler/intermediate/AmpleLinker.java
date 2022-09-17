package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.intermediate.generator.IntermediateGenerator;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.type.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for linking {@link LinkableObject}.
 * <p>
 * This linker will create a {@link IntermediateFile} that contains:
 * <ul>
 *   <li>Instructions</li>
 * </ul>
 *
 * @author HardCoded
 */
public class AmpleLinker {
	private static final Logger LOGGER = LogManager.getLogger(AmpleLinker.class);
	private final AmpleConfig ampleConfig;
	
	public AmpleLinker(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
	}
	
	public IntermediateFile link(List<LinkableObject> list) {
		IntermediateFile file = new IntermediateFile();
		
		ExportMap exportMap = new ExportMap();
		if (!checkImports(exportMap, list)) {
			throw new RuntimeException("Project is not linkable");
		}
		
		LOGGER.info("");
		LOGGER.info("Generate Intermediate File:");
		IntermediateGenerator generator = new IntermediateGenerator(file, exportMap);
		for (int i = list.size() - 1; i >= 0; i--) {
			// Include each linkable object in the intermediate generator
			LinkableObject link = list.get(i);
			LOGGER.info(" - {} : {}", link.getChecksum(), ampleConfig.getConfiguration().getWorkingDirectory().toPath().relativize(link.getFile().toPath()));
			
			generator.generate(link);
		}
		
		//		for (Procedure proc : file.getProcedures()) {
		//			LOGGER.debug("# proc " + proc);
		//			for (Inst inst : proc.getInstructions()) {
		//				ISyntaxPosition pos = inst.getSyntaxPosition();
		//				String details = "(line: %3d, column: %3d) ".formatted(pos.getStartPosition().line, pos.getStartPosition().column);
		//
		//				if (inst.getOpcode() == Opcode.LABEL) {
		//					LOGGER.debug("    {} {}", details, inst);
		//				} else {
		//					LOGGER.debug("        {} {}", details, inst);
		//				}
		//			}
		//		}
		
		return file;
	}
	
	private boolean checkImports(ExportMap exportMap, List<LinkableObject> list) {
		for (LinkableObject link : list) {
			if (!exportMap.add(link)) {
				return false;
			}
		}
		
		for (LinkableObject link : list) {
			if (!exportMap.containsThrowErrors(link)) {
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
					throw new RuntimeException("(%s) The project already exports a function '%s'".formatted(obj.getFile(), reference.getName()));
				}
				
				if (reference.isVariable() && variables.put(reference.getName(), reference) != null) {
					throw new RuntimeException("(%s) The project already exports a variable '%s'".formatted(obj.getFile(), reference.getName()));
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
					throw new RuntimeException("(%s) The imported symbol '%s' was not found in the project".formatted(obj.getFile(), reference.getName()));
				}
			}
			
			return true;
		}
	}
}
