package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.type.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ExportMap {
	private static final Logger LOGGER = LogManager.getLogger(ExportMap.class);
	final Map<String, Reference> functions;
	final Map<String, Reference> variables;
	
	protected ExportMap() {
		functions = new HashMap<>();
		variables = new HashMap<>();
	}
	
	protected boolean add(LinkableObject obj) {
		for (Reference reference : obj.getExportedReferences()) {
			if (reference.isFunction()) {
				String mangledName = reference.getMangledName();
				if (mangledName == null) {
					throw new NullPointerException("(%s) Exported function reference has missing mangled name (%s)".formatted(obj.getFile(), reference.getName()));
				}
				
				if (functions.put(mangledName, reference) != null) {
					throw new RuntimeException("(%s) The project already exports a function '%s' (%s)".formatted(obj.getFile(), reference.getName(), mangledName));
				}
			}
			
			if (reference.isVariable() && variables.put(reference.getName(), reference) != null) {
				throw new RuntimeException("(%s) The project already exports a variable '%s'".formatted(obj.getFile(), reference.getName()));
			}
		}
		
		return true;
	}
	
	public Reference getReference(Reference reference) {
		if (reference.isFunction()) {
			String mangledName = reference.getMangledName();
			return functions.get(mangledName);
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
				LOGGER.error("{} The imported symbol '{}' was not found in the project", obj.getFile(), reference.getPath());
				throw new RuntimeException("(%s) Missing symbol '%s'%s".formatted(
					obj.getFile(),
					reference.getPath(),
					reference.getMangledName() == null ? "" : (" [" + AmpleMangler.demangleFunction(reference.getMangledName()) + "]")
				));
			}
		}
		
		return true;
	}
}
