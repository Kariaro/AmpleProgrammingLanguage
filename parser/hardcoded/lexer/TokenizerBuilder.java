package hardcoded.lexer;

import java.util.*;
import java.util.regex.Pattern;

public class TokenizerBuilder {
	TokenizerBuilder() {
		
	}
	
	private Map<String, List<Pattern>> groups = new LinkedHashMap<>();
	private Map<String, List<Pattern>> escapes = new HashMap<>();
	
	// TODO: Set linefeed to this...
	
	/**
	 * This will set the standard line feed to this string.<br>
	 * This will convert all <code>(\r\n|\r|\n)</code> into the specified linefeed.
	 * 
	 * @param linefeed
	 */
	public TokenizerBuilder setLinefeedCharacter(String linefeed) {
		return this;
	}
	
	/**
	 * Adds a new delimiter group. This can be used to capture
	 * strings and comments.
	 * 
	 * @param groupName
	 * @param open
	 * @param escape
	 * @param close
	 */
	public TokenizerBuilder addDelimiterGroup(String groupName, String open, String escape, String close) {
		// TODO: Implement me.
		
		return this;
	}
	
	/**
	 * Adds a new capture group
	 * 
	 * @param groupName
	 * @param patterns
	 */
	public TokenizerBuilder addRegexGroup(String groupName, String... patterns) {
		System.out.println("addGroup(\"" + groupName + "\", " + new ArrayList<>(Arrays.asList(patterns)) + ")");
		
		List<Pattern> list = groups.getOrDefault(groupName, new ArrayList<>());
		for(String regex : patterns)
			list.add(Pattern.compile(regex));
		
		groups.putIfAbsent(groupName, list);
		
		return this;
	}
	
	/**
	 * Adds a escape for a group. If this character is found inside a group then it will escape that character.
	 * 
	 * @param groupName
	 * @param array
	 */
	public TokenizerBuilder addEscape(String groupName, String... array) {
		System.out.println("addEscape(\"" + groupName + "\", " + new ArrayList<>(Arrays.asList(array)) + ")");
		
		List<Pattern> list = escapes.getOrDefault(groupName, new ArrayList<>());
		for(String regex : array)
			list.add(Pattern.compile(regex));
		
		groups.putIfAbsent(groupName, list);
		
		return this;
	}
	
	/**
	 * If this group is found inside a string it will be removed from the 
	 * parsed string.
	 * 
	 * @param groupName
	 */
	public TokenizerBuilder discardGroup(String groupName) {
		return this;
	}
	
	/**
	 * Discards multiple groups from the parsed string.
	 * 
	 * @param groupNames
	 */
	public TokenizerBuilder discardGroups(String... groupNames) {
		return this;
	}
	
	/**
	 * Set the default groupName if some words did not match any groups.
	 * 
	 * @param groupName
	 */
	public TokenizerBuilder defaultGroup(String groupName) {
		
		return this;
	}
	
	
	
	public Tokenizer build() {
		return new Tokenizer(groups);
	}
}
