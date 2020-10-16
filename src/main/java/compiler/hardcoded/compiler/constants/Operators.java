package hardcoded.compiler.constants;

import java.util.*;

/**
 * This class contains all the operator keywords that are used in the programming language.
 * @author HardCoded
 */
public final class Operators {
	private static final Set<String> ASSIGNMENT;
	private Operators() {}
	
	static {
		Set<String> assignment = new HashSet<>();
		assignment.addAll(Arrays.asList(
			"=",
			"+=", "-=",
			"*=", "/=",
			"%=", "&=",
			"^=", "|=",
			"<<=", ">>="
		));
		
		ASSIGNMENT = Collections.unmodifiableSet(assignment);
	}
	
	/**
	 * Checks if the string value is an assignment operator.<br>
	 * An assignment operator can be one of the following values.<br>
	 *<PRE>
	 *&lt&lt=  &gt&gt=    =
	 * +=   -=   *=   /=
	 * %=   &=   ^=   |=
	 *</PRE>
	 * @param	value
	 * @return	{@code true} if the string value was an assignment operator
	 */
	public static boolean isAssignmentOperator(String value) {
		return ASSIGNMENT.contains(value);
	}
}