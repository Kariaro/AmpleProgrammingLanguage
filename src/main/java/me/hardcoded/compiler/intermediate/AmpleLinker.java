package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.errors.CompilerException;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.intermediate.generator.IntermediateGenerator;
import me.hardcoded.compiler.intermediate.inst.Inst;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.compiler.intermediate.inst.Opcode;
import me.hardcoded.compiler.intermediate.inst.Procedure;
import me.hardcoded.compiler.parser.LinkableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

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
	
	// TODO: Implement an LinkableIntermediateFile
	// TODO: Implement an assembler language for ample intermediate instructions
	public IntermediateFile link(List<LinkableObject> list) throws CompilerException {
		IntermediateFile file = new IntermediateFile();
		
		ExportMap exportMap = new ExportMap();
		if (!checkImports(exportMap, list)) {
			throw new RuntimeException("Project is not linkable");
		}
		
		LOGGER.debug("");
		LOGGER.debug("Generate Intermediate File:");
		IntermediateGenerator generator = new IntermediateGenerator(file, exportMap);
		for (int i = list.size() - 1; i >= 0; i--) {
			// Include each linkable object in the intermediate generator
			LinkableObject link = list.get(i);
			
			Path path = link.getFile().toPath();
			if (path.isAbsolute()) {
				LOGGER.debug(" - {} : {}", link.getChecksum(), ampleConfig.getConfiguration().getWorkingDirectory().toPath().relativize(path));
			} else {
				LOGGER.debug(" - {} : {}", link.getChecksum(), path);
			}
			//			LOGGER.debug("\n{}", ParseUtil.stat(link.getProgram()));
			
			generator.generate(link);
		}
		
		LOGGER.debug("");
		for (Procedure proc : file.getProcedures()) {
			switch (proc.getType()) {
				case FUNCTION -> LOGGER.debug("# func {}", proc);
				case VARIABLE -> LOGGER.debug("# variable {}", proc);
				default -> LOGGER.debug("# proc = {}", proc.getType());
			}
			
			for (Inst inst : proc.getInstructions()) {
				ISyntaxPos pos = inst.getSyntaxPosition();
				String details = "(line: %3d, column: %3d)".formatted(pos.getStartPosition().line(), pos.getStartPosition().column());
				
				if (inst.getOpcode() == Opcode.LABEL) {
					LOGGER.debug("    {}     {}", details, inst);
				} else {
					LOGGER.debug("        {}     {}", details, inst);
				}
			}
		}
		
		return file;
	}
	
	private boolean checkImports(ExportMap exportMap, List<LinkableObject> list) throws ParseException {
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
}
