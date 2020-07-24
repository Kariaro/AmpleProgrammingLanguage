package hc.grammar;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import hc.token.Symbol;
import hc.token.Tokenizer;

// https://en.wikipedia.org/wiki/LR_parser
public class Grammar {
	private static boolean debug = false;
	private final Map<String, Type> types;
	
	public Grammar(String path) throws IOException {
		types = new HashMap<>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
		int ruleIndex = 1;
		
		
		Type type = null;
		String line;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			
			// Remove comments and empty lines
			if(line.startsWith("#") || line.isEmpty()) continue;
			
			String first = null;
			{
				String[] split = line.split("[ \t]+");
				first = split[0];
				
				// Remove the first token from the line and
				// set the first to the first token.
				line = line.substring(first.length()).trim();
			}
			
			if(first.endsWith(":")) { // Type start
				type = new Type(first.substring(0, first.length() - 1));
				
				if(debug) System.out.println("New Type: '" + type + "'");
				
				// Make this into a add pattern
				if(!line.isEmpty()) first = "|";
			}
			
			// Add new pattern
			if(first.equals("|")) {
				type.matches.add(createMatch(ruleIndex++, line));
			}
			
			if(first.equals(";")) {
				if(debug) {
					for(Match m : type.matches) {
						System.out.println("    - " + m);
					}
					System.out.println("End Type: '" + type + "'");
					System.out.println();
				}
				
				types.put(type.name, type);
				type = null;
			}
			
		}
		reader.close();
	}
	

	protected MatchList createMatch(int ruleId, String line) {
		Symbol start = Tokenizer.generateSymbolChain(line.getBytes());
		
		MatchList list = new MatchList(ruleId, line);
		Symbol symbol = start;
		
		while(symbol != null) {
			String value = symbol.toString();
			
			if(value.equals("{")) {
				Symbol pattern = symbol.next();
				if(!pattern.next().equals("}")) return null;
				list.add(new MatchRegex(pattern));
				symbol = pattern.next(2);
				continue;
			}
			
			if(value.equals("[") || value.equals("(")) {
				MatchBracket match = createMatchBracket(symbol);
				
				list.add(match);
				symbol = symbol.next(match.symbol.remaining() + 3);
				continue;
			}
			
			if(value.startsWith("\"")) {
				// Must be a string
				list.add(new MatchString(symbol));
			} else {
				// Must be a type
				list.add(new MatchType(symbol));
			}
			
			symbol = symbol.next();
		}
		
		return list;
	}
	
	protected MatchBracket createMatchBracket(Symbol start) {
		boolean repeat = start.equals("[");
		MatchBracket bracket = new MatchBracket(start, repeat);
		start = start.next();
		
		Symbol symbol = start;
		int count = -1;
		while(symbol != null) {
			String value = symbol.toString();
			count++;
			
			if(value.equals("{")) {
				Symbol pattern = symbol.next();
				if(!pattern.next().equals("}")) return null;
				bracket.add(new MatchRegex(pattern));
				symbol = pattern.next(2);
				continue;
			}
			
			if(value.equals("[") || value.equals("(")) {
				MatchBracket match = createMatchBracket(symbol);
				
				bracket.add(match);
				symbol = symbol.next(match.symbol.remaining() + 3);
				count += match.symbol.remaining() + 2;
				continue;
			}
			
			if(repeat && value.equals("]") || !repeat && value.equals(")")) {
				bracket.symbol = start.clone(count - 1);
				break;
			}
			
			if(value.startsWith("\"")) {
				// Must be a string
				bracket.add(new MatchString(symbol));
			} else {
				// Must be a type
				bracket.add(new MatchType(symbol));
			}
			
			symbol = symbol.next();
		}
		
		return bracket;
	}
	
	public boolean test(Symbol symbol) {
		System.out.println(symbol.toString(" ", 100));
		System.out.println();
		
		for(Type type : types.values()) {
			System.out.println("Type: '" + type.name + "'");
			try {
				type.match(symbol);
//				for(Match match : type.matches) {
//					try {
//						System.out.print("     - (" + match + ") ");
//						System.out.println(match.match(symbol));
//					} catch(Exception e) {
//						System.out.println("ERROR - " + e.getMessage());
//					}
//				}
			} catch(Throwable e) {
				System.out.println();
				System.out.println(e.getCause());
				System.out.println();
				//e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean test(Type type, Symbol symbol) {
		debug = true;
		
		System.out.println(symbol.toString(" ", 100));
		System.out.println();
		
		try {
			type.match(symbol);
		} catch(Throwable e) {
			System.out.println();
			System.out.println(e.getCause());
			System.out.println();
			//e.printStackTrace();
		}
		
		System.out.println("============================================================================");
		
		return false;
	}
	
	public Type getType(String name) {
		return types.get(name);
	}

	/**
	 * If stuff is nested to deeply it will give a
	 * 'throw new ExpressionNestedTooDeepException()'
	 */
	private final int maxCount = 10;
	private int curCount = 1;
	private int stackIndex = 0;
	
	private void printfst(String format, int padding, Object... obj) {
		/*
		if(padding < 1) padding = 1;
		String ident = String.format("%" + (stackIndex == 0 ? "":(stackIndex * padding) + "") + "s", "");
		System.out.printf(ident + format, obj);
		*/
	}
	
	private void printfst2(String format, int padding, Object... obj) {
		if(padding < 1) padding = 1;
		String ident = String.format("%" + (stackIndex == 0 ? "":(stackIndex * padding) + "") + "s", "");
		System.out.printf(ident + format, obj);
	}
	
	// TODO: Precedence
	class Type {
		private final String name;
		private final List<MatchList> matches;
		
		Type(String name) {
			this.matches = new ArrayList<>();
			this.name = name;
		}
		
		public int match(Symbol symbol) {
			for(MatchList match : matches) {
				try {
					Thread.sleep(50);
					// System.out.println("     - (" + match + ") ");
					
					// stack.add(match);
					// printfst("+++ %s\t%s\n", 3, match, match.getRuleString());
					printfst2("%-7s - %s\n", 3, match.getRuleString(), match);
					
					/* Sometimes a expression can match the first token but not the rest of the
					 * tokens in a string.
					 * 
					 * 
					 * 
					 */
					stackIndex++;
					int length = match.match(symbol);
					stackIndex--;
					
					// printfst("--- %s\t%d\n", 3, match, length);
					// printfst2("exit  %s\n", 3, match.getRuleString());
					
					//System.out.println(length);
					//System.out.println("str = " + symbol.toString(" ", 1000));
					
					if(length != -1) {
						return length;
					}
				} catch(Exception e) {
					System.out.println("ERROR - " + e.getMessage());
					e.printStackTrace();
				}
			}
			
			return -1;
		}
		
		public String toString() {
			return name;
		}
	}
	
	abstract class Match {
		protected final int ruleId;
		
		public Match() { this(0); }
		public Match(int ruleId) { this.ruleId = ruleId; }
		public String getRuleString() { return "Rule" + ruleId; }
		
		/**
		 * Returns the amount of symbols that matched this pattern.
		 * @param stack The current symbols used
		 * @param symbol The symbol to check if it matches this pattern.
		 * @return Returns -1 if no match was found.
		 */
		protected abstract int match(Symbol symbol);
	}
	
	class MatchList extends Match {
		private final List<Match> matches;
		private final String line;
		
		public MatchList(int ruleId, String line) {
			super(ruleId);
			
			matches = new ArrayList<>();
			this.line = line;
		}
		
		public void add(Match match) {
			matches.add(match);
		}
		
		protected int match(Symbol symbol) {
			int count = 0;
			for(int i = 0; i < matches.size(); i++) {
				Match match = matches.get(i);
				
				// Not enough symbols to match pattern
				if(symbol == null) {
					printfst("  - %s\n", stackIndex, "not enough tokens");
					
					return -1;
				}
				
				printfst("  - %s\n", stackIndex, match + ", \"token = '" + symbol + "'\", \"symbol = '" + symbol.toString(10) + "'\"");
				
				int result = match.match(symbol);
				if(result < 0) {
					printfst("  - %s\n", stackIndex, "token was incorrect. was \"" + symbol + "\" should have been " + match);
					return -1;
				}
				
				count += result;
				printfst("  - %s\n", stackIndex, "token was correct \"" + symbol.toString(result) + "\"");
				
				// Matched
				symbol = symbol.next(result);
				
				
				
				// Planing to fix left recursing.
			}
			
			// printfst("  - result = %d\n", stackIndex, count);
			
			return count;
		}
		
		public String toString() {
			String string = line;
			string = matches.toString();
			return string.substring(1, string.length() - 1);
		}
	}
	
	class MatchBracket extends Match {
		private List<Match> matches;
		private boolean repeat;
		private Symbol symbol;
		
		public MatchBracket(Symbol symbol) {
			this(symbol, false);
		}
		
		public MatchBracket(Symbol symbol, boolean repeat) {
			matches = new ArrayList<>();
			this.symbol = symbol;
			this.repeat = repeat;
		}
		
		public void add(Match match) {
			matches.add(match);
		}
		
		public int match(Symbol symbol) {
			int total = 0;
			
			do {
				int count = 0;
				for(Match match : matches) {
					// Not enough symbols to match pattern
					if(symbol == null) return total;
					
					int result = match.match(symbol);
					if(result < 0) return total;
					count += result;
					
					// Matched
					symbol = symbol.next(result);
				}
				
				if(count == 0) return 0;
				total += count;
			} while(repeat);
			
			return total;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder().append(repeat ? "[":"(");
			for(Match match : matches) sb.append(match.toString()).append(", ");
			if(!matches.isEmpty()) {
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.append(repeat ? "]":")").toString();
		}
	}
	
	class MatchType extends Match {
		private String name;
		public MatchType(Symbol symbol) {
			this.name = symbol.toString();
		}
		
		public int match(Symbol symbol) {
			Type type = getType(name);
			return type.match(symbol);
			/*
			for(Match match : type.matches) {
				int result = match.match(symbol);
				if(result != -1) return result;
			}
			return -1;
			*/
		}
		
		public String toString() { return "t:" + name; }
	}
	
	class MatchString extends Match {
		private String value;
		public MatchString(Symbol symbol) {
			value = symbol.toString();
			value = value.substring(1, value.length() - 1);
		}
		
		public int match(Symbol symbol) {
			return symbol.equals(value) ? 1 : -1;
		}
		
		public String toString() { return "s:\"" + value + "\""; }
	}
	
	class MatchRegex extends Match {
		private Pattern pattern;
		public MatchRegex(Symbol symbol) {
			String regex = symbol.toString();
			regex = regex.substring(1, regex.length() - 1);
			pattern = Pattern.compile(regex);
		}
		
		public int match(Symbol symbol) {
			return pattern.matcher(symbol.toString()).matches() ? 1 : -1;
		}
		
		public String toString() { return "r:" + pattern; }
	}
	
	class RuleList {
		private Set<Integer> list = new HashSet<>();
		public boolean add(int i) { return list.add(i); }
		public boolean add(Match match) { return add(match.ruleId); }
		public boolean remove(int i) { return list.remove(i); }
		public boolean remove(Match match) { return remove(match.ruleId); }
		public boolean contains(int i) { return list.contains(i); }
		public boolean contains(Match match) { return list.contains(match.ruleId); }
		public int size() { return list.size(); }
		public String toString() { return list.toString(); }
	}
}
