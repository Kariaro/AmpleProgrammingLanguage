package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.intermediate.generator.InstGenerator;
import me.hardcoded.compiler.intermediate.inst.Inst;
import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.compiler.intermediate.inst.Opcode;
import me.hardcoded.compiler.intermediate.inst.Procedure;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.ParseUtil;
import me.hardcoded.compiler.parser.serial.LinkableDeserializer;
import me.hardcoded.compiler.parser.serial.LinkableSerializer;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.visualization.ParseTreeVisualization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

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
		
		try {
			for (int i = 0; i < allObjects.size(); i++) {
				LinkableObject obj = allObjects.get(i);
				byte[] bytes = LinkableSerializer.serializeLinkable(obj);
				
				File debugFolder = new File("debug/out_" + obj.getFile().getName() + ".serial");
				if (debugFolder.exists() || debugFolder.createNewFile()) {
					try (FileOutputStream out = new FileOutputStream(debugFolder)) {
						out.write(bytes == null ? new byte[0] : bytes);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				byte[] readBytes = Files.readAllBytes(debugFolder.toPath());
				LinkableObject loaded = LinkableDeserializer.deserializeLinkable(readBytes);
				byte[] recombined = LinkableSerializer.serializeLinkable(loaded);
				
				if (Arrays.compare(bytes, recombined) != 0) {
					throw new ParseException("Linkable serializer did not match");
				}
				
				System.out.println(ParseUtil.stat(loaded.getProgram()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (LinkableObject link : allObjects) {
			System.out.println(link + ", " + link.getFile());
		}
		
		ExportMap exportMap = new ExportMap();
		if (!checkImports(exportMap, allObjects)) {
			throw new ParseException("Project is not linkable");
		}
		
		
		System.out.println("=".repeat(100));
		
		AmpleValidator validator = new AmpleValidator(exportMap);
		validator.validate(allObjects);
		
		for (LinkableObject link : allObjects) {
			System.out.println(ParseUtil.stat(link.getProgram()));
		}
		
		System.out.println("=".repeat(100));
		
		// Type checking would be easier to do but the file might consume a lot of memory when combining it
		// That's why we should separate them
		
		// TODO: Before we generate the instructions we need to validate types
		
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
