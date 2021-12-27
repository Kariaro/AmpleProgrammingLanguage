package hardcoded.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hardcoded.utils.StringUtils;

public class GenericLexerContext<T> {
	protected final List<LexerRule> rules;
	protected final T whitespace;
	
	public GenericLexerContext() {
		this.rules = new ArrayList<>();
		this.whitespace = null;
	}
	
	public GenericLexerContext(T whitespace) {
		this.rules = new ArrayList<>();
		this.whitespace = whitespace;
	}
	
	public GenericLexerContext<T> addRule(T type, Consumer<LexerRule> consumer) {
		LexerRule rule = new LexerRule(type);
		consumer.accept(rule);
		rules.add(rule);
		return this;
	}
	
	public List<LexerToken> parse(String input) {
		List<LexerToken> tokenList = new ArrayList<>();
		int last_length = 0;
		
		LexerToken lexerToken;
		while((lexerToken = nextToken(input)) != null) {
			if(input.isEmpty() || (last_length == input.length())) break;
			
			if(lexerToken.type != whitespace) {
				tokenList.add(lexerToken);
			}
			
			last_length = input.length();
			input = input.substring(lexerToken.length);
		}
		
		return tokenList;
	}
	
	public GenericLexerContext<T> toImmutable() {
		return new ImmutableGenericContext<T>(this);
	}
	
	public LexerToken nextToken(String input) {
		LexerRule selectedRule = null;
		int longestRule = 1;
		for(LexerRule rule : rules) {
			int length = rule.getMatchLength(input);
			
			if(length >= longestRule) {
				longestRule = length;
				selectedRule = rule;
			}
		}
		
		return selectedRule == null ? null:new LexerToken(selectedRule.type, input.substring(0, longestRule));
	}
	
	public class LexerToken {
		public final T type;
		public final String content;
		public final int length;
		
		public LexerToken(T type, String content) {
			this.length = content.length();
			this.content = content;
			this.type = type;
		}
	}
	
	public class LexerRule {
		public final List<Pattern> matches;
		public final T type;
		
		public LexerRule(T type) {
			this.matches = new ArrayList<>();
			this.type = type;
		}
		
		public LexerRule addString(String value) {
			this.matches.add(Pattern.compile(StringUtils.regexEscape(value)));
			return this;
		}
		
		public LexerRule addStrings(String... values) {
			for(String value : values) {
				addString(value);
			}
			
			return this;
		}
		
		public LexerRule addRegex(String regex) {
			this.matches.add(Pattern.compile(regex));
			return this;
		}
		
		public LexerRule addRegexes(String... regexes) {
			for(String regex : regexes) {
				addRegex(regex);
			}
			
			return this;
		}
		
		public LexerRule addSingleline(String open, String close) {
			return addSingleline(open, "", close);
		}
		
		public LexerRule addSingleline(String open, String escape, String close) {
			return addDelimiter(open, escape, close, 0);
		}
		
		public LexerRule addMultiline(String open, String close) {
			return addMultiline(open, "", close);
		}
		
		public LexerRule addMultiline(String open, String escape, String close) {
			return addDelimiter(open, escape, close, Pattern.DOTALL);
		}
		
		private LexerRule addDelimiter(String open, String escape, String close, int flags) {
			String s = StringUtils.regexEscape(open);
			String c = StringUtils.regexEscape(close);
			
			String regex;
			if(escape.isEmpty()) {
				regex = s + ".*?" + c;
			} else {
				String e = StringUtils.regexEscape(escape);
				regex = s + "(?:" + e + "(?:" + e + "|" + c + "|(?!" + c + ").)|(?!" + e + "|" + c + ").)*" + c;
			}
			
			this.matches.add(Pattern.compile(regex, flags));
			return this;
		}
		
		public int getMatchLength(String string) {
			int length = 0;
			for(Pattern pattern : matches) {
				Matcher matcher = pattern.matcher(string);
				if(matcher.lookingAt()) {
					length = Math.max(length, matcher.end());
				}
			}
			
			return length < 1 ? -1:length;
		}
	}
	
	public static class ImmutableGenericContext<T> extends GenericLexerContext<T> {
		private ImmutableGenericContext(GenericLexerContext<T> context) {
			this.rules.addAll(context.rules);
		}
		
		@Override
		public GenericLexerContext<T> addRule(T type, Consumer<LexerRule> consumer) {
			throw new UnsupportedOperationException();
		}
		
	}
}
