package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.intermediate.generator.IntermediateGenerator;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.type.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * This class is responsible for linking {@link LinkableObject}.
 *
 * This linker will create a {@link IntermediateFile} that contains:
 * <ul>
 *   <li>Instructions</li>
 * </ul>
 *
 * @author HardCoded
 */
public class AmpleLinker {
	private static final Logger LOGGER = LogManager.getLogger(AmpleLinker.class);

	public AmpleLinker() {

	}

	public IntermediateFile link(List<LinkableObject> list) {
		IntermediateFile file = new IntermediateFile();

		LOGGER.info("Intermediate Files:");
		for (LinkableObject link : list) {
			LOGGER.info("  - {}", link.getFile());
		}

		ExportMap exportMap = new ExportMap();
		if (!checkImports(exportMap, list)) {
			throw new ParseException("Project is not linkable");
		}

		LOGGER.info("=".repeat(100));
		
//		AmpleValidator validator = new AmpleValidator(exportMap);
//		validator.validate(allObjects);
//
//		for (LinkableObject link : allObjects) {
//			LOGGER.info("{}", ParseUtil.stat(link.getProgram()));
//		}
//
//		LOGGER.info("=".repeat(100));
//
//		// Type checking would be easier to do but the file might consume a lot of memory when combining it
//		// That's why we should separate them
//
//		// TODO: Before we generate the instructions we need to validate types
//
		IntermediateGenerator generator = new IntermediateGenerator(file, exportMap);
		for (LinkableObject link : list) {
			generator.generate(link);
		}
		
//		for (Procedure proc : file.getProcedures()) {
//			System.out.println("# proc " + proc);
//			for (Inst inst : proc.getInstructions()) {
//				ISyntaxPosition pos = inst.getSyntaxPosition();
//				String test = "(line: %3d, colum: %3d) ".formatted(pos.getStartPosition().line, pos.getStartPosition().column);
//
//				if (inst.getOpcode() == Opcode.LABEL) {
//					System.out.println("    %s".formatted(test) + inst);
//				} else {
//					System.out.println("        %s".formatted(test) + inst);
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
