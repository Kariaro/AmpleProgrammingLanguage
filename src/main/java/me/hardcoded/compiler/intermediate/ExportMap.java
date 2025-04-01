package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ReferenceSyntax;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.error.ErrorUtil;
import me.hardcoded.utils.types.MangledFunctionMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportMap {
	private static final Logger LOGGER = LogManager.getLogger(ExportMap.class);
	final MangledFunctionMap functions;
	final Map<String, Reference> variables;
	final Map<String, Reference> types;
	
	public ExportMap() {
		functions = new MangledFunctionMap();
		variables = new HashMap<>();
		types = new HashMap<>();
	}
	
	public void clear() {
		functions.clear();
		variables.clear();
		types.clear();
	}
	
	public boolean add(LinkableObject obj) throws ParseException {
		// First link all variables and types
		for (ReferenceSyntax referenceSyntax : obj.getExportedReferences()) {
			Reference reference = referenceSyntax.getReference();
			
			if (reference.isVariable() && variables.put(reference.getName(), reference) != null) {
				throw new ParseException(ErrorUtil.createFullError(referenceSyntax.getSyntaxPosition(),
					"The project already exports a variable '%s'".formatted(
						reference.getName()
					)
				));
			}
			
			if (reference.isType()) {
				System.out.println("REFERENCED - " + reference);
			}
			if (reference.isType() && types.put(reference.getName(), reference) != null) {
				throw new ParseException(ErrorUtil.createFullError(referenceSyntax.getSyntaxPosition(),
					"The project already exports a type '%s'".formatted(
						reference.getName()
					)
				));
			}
		}
		
		// Calculate functions, some might need to be linked as well
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
				
				// TODO: We need to demangle with the context to resolve imported types
				
				System.out.println("Adding " + reference);
				if (!functions.put(reference)) {
					Reference blocker = functions.getBlocker(reference);
					String mangled = null;
					if (blocker != null) {
						mangled = AmpleMangler.demangleFunction(blocker.getMangledName()).toString();
					}
					System.out.println(blocker + "," + reference);
					System.out.println(functions);
					System.out.println(mangledName);
					throw new ParseException(ErrorUtil.createFullError(referenceSyntax.getSyntaxPosition(),
						"The project already exports a function '%s' (%s)".formatted(
							reference.getName(),
							mangled
						)
					));
				}
			}
		}
		
		return true;
	}
	
	public Reference getReference(Reference reference) {
		if (reference.isFunction()) {
			return functions.get(reference);
		}
		
		if (reference.isVariable()) {
			return variables.get(reference.getName());
		}
		
		if (reference.isType() || reference.getValueType().isLinked()) {
			return types.get(reference.getName());
		}
		
		return null;
	}
	
	public Reference getType(ValueType type) {
		return types.get(type.getName());
	}
	
	public Reference getMangledFunctionReference(Reference reference, List<Reference> parameters) {
		String mangledName = AmpleMangler.mangleFunction(Primitives.LINKED, reference.getNamespace(), reference.getName(), parameters);
		
		Reference result;
		if ((result = functions.get(mangledName)) != null) {
			return result;
		}
		
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
				String demangled = reference.getMangledName();
				if (demangled != null) {
					try {
						if (reference.isFunction()) {
							demangled = AmpleMangler.demangleFunction(demangled).toString();
						} else if (reference.isVariable()) {
							demangled = AmpleMangler.demangleType(demangled).toString();
						}
					} catch (Exception ignore) {
						demangled = "error<" + demangled + ">";
					}
				}
				
				LOGGER.warn("The imported symbol '{}' was not found in the project", reference.getPath());
				LOGGER.warn("{}", ErrorUtil.createFullError(
					referenceSyntax.getSyntaxPosition(),
					"Missing symbol '%s'%s".formatted(
						reference.getPath(),
						demangled == null ? "" : (" [" + demangled + "]")
					))
				);
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return "ExportMap[\n" +
			" - functions=" + functions + "\n" +
			" - variables=" + variables + "\n" +
			" - types=" + types + "\n" +
			']';
	}
}
