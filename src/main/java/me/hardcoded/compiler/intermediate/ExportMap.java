package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ReferenceSyntax;
import me.hardcoded.utils.error.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportMap {
	private static final Logger LOGGER = LogManager.getLogger(ExportMap.class);
	final Map<String, Reference> functions;
	final Map<String, Reference> variables;
	final Map<String, Reference> varargFunctions;
	
	protected ExportMap() {
		varargFunctions = new HashMap<>();
		functions = new HashMap<>();
		variables = new HashMap<>();
	}
	
	// Instead of making a map that links different symbols to
	
	protected boolean add(LinkableObject obj) throws ParseException {
		for (ReferenceSyntax referenceSyntax : obj.getExportedReferences()) {
			Reference reference = referenceSyntax.getReference();
			
			if (reference.isFunction()) {
				String mangledName = reference.getMangledName();
				if (mangledName == null) {
					throw new ParseException(ErrorUtil.createFullError(referenceSyntax.getSyntaxPosition(),
						"Exported function reference has missing mangled name (%s)".formatted(
							reference.getName()
						)
					));
				}
				
				if (functions.put(mangledName, reference) != null) {
					throw new ParseException(ErrorUtil.createFullError(referenceSyntax.getSyntaxPosition(),
						"The project already exports a function '%s' (%s)".formatted(
							reference.getName(),
							mangledName
						)
					));
				}
			}
			
			if (reference.isVariable() && variables.put(reference.getName(), reference) != null) {
				throw new ParseException(ErrorUtil.createFullError(referenceSyntax.getSyntaxPosition(),
					"The project already exports a variable '%s'".formatted(
						reference.getName()
					)
				));
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
	
	public Reference getMangledFunctionReference(Reference reference, List<Reference> parameters) {
		String mangledName = AmpleMangler.mangleFunction(reference.getNamespace(), reference.getName(), parameters);
		
		Reference result;
		if ((result = functions.get(mangledName)) != null) {
			return result;
		}
		
		//		if ((result = variables.get(mangledName)) != null) {
		//			return result;
		//		}
		
		return null;
	}
	
	public boolean contains(LinkableObject obj) {
		for (ReferenceSyntax referenceSyntax : obj.getImportedReferences()) {
			Reference reference = referenceSyntax.getReference();
			
			if (getReference(reference) == null) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean containsThrowErrors(LinkableObject obj) throws ParseException {
		for (ReferenceSyntax referenceSyntax : obj.getImportedReferences()) {
			Reference reference = referenceSyntax.getReference();
			
			if (getReference(reference) == null) {
				LOGGER.warn("The imported symbol '{}' was not found in the project", reference.getPath());
				LOGGER.warn("{}", ErrorUtil.createFullError(referenceSyntax.getSyntaxPosition(),
					"Missing symbol '%s'%s".formatted(
						reference.getPath(),
						reference.getMangledName() == null ? "" : (" [" + AmpleMangler.demangleFunction(reference.getMangledName()) + "]")
					))
				);
				//				throw new ParseException(ErrorUtil.createFullError(referenceSyntax.getSyntaxPosition(),
				//					"Missing symbol '%s'%s".formatted(
				//						reference.getPath(),
				//						reference.getMangledName() == null ? "" : (" [" + AmpleMangler.demangleFunction(reference.getMangledName()) + "]")
				//					))
				//				);
			}
		}
		
		return true;
	}
}
