package hardcoded.compiler.context;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import hardcoded.compiler.statement.*;
import hardcoded.lexer.Token.Type;

public class CompilerPattern<T> {
	private static final Object NONE_OBJECT = new Object();
	public static final CompilerPattern<Object> NONE = new CompilerPattern<>(List.of(), (ctx) -> NONE_OBJECT);
	
	final List<Consumer<Context>> syntax;
	final Function<Context, T> result;
	
	private CompilerPattern(List<Consumer<Context>> syntax, Function<Context, T> result) {
		this.syntax = syntax;
		this.result = result;
	}
	
	void evaluate(Context ctx) {
		
	}
	
	public static <T> CompilerPattern<T> of(List<Consumer<Context>> syntax, Function<Context, T> result) {
		return new CompilerPattern<>(syntax, result);
	}
	
	public static <T> CompilerPattern<T> of(Function<Context, T> result) {
		return new CompilerPattern<>(List.of(), result);
	}
	
	@SafeVarargs
	public static List<Consumer<Context>> syntax(Consumer<Context>... rules) {
		return List.of(rules);
	}
	
	public static Consumer<Context> require(Type type) {
		return (ctx) -> {
			if(ctx.reader.type() != type)
				ctx.syntaxError("Expected '%s' but got '%s'", type, ctx.reader.type());
			ctx.reader.advance();
		};
	}
	
	public static Consumer<Context> require(Type type, String errorMessage) {
		return (ctx) -> {
			if(ctx.reader.type() != type)
				ctx.syntaxError(errorMessage);
			ctx.reader.advance();
		};
	}
	
	public static Consumer<Context> require(Consumer<Context> consumer, String errorMessage) {
		return (ctx) -> {
			if(!ctx.execute(consumer))
				ctx.syntaxError(errorMessage);
		};
	}
	
	public static class Context {
		public List<String> errorList = new ArrayList<>();
		public LangContext reader;
		public boolean error;
		
		public void syntaxError(String format, Object... args) {
			errorList.add(format.formatted(args));
		}
		
		public boolean execute(Consumer<Context> consumer) {
			return false;
		}
		
		public void setError(boolean error) {
			this.error = error;
		}
		
		// Push a pattern that should be evaluated
		public void push(CompilerPattern<?> pattern) {
			
		}

		public <T> T get(int i) {
			return null;
		}
	}
	
	
	
	static {
		@SuppressWarnings("unused")
		CompilerPattern<Statement> EMPTY = CompilerPattern.of((ctx) -> Statement.newEmpty());
		
		CompilerPattern<VariableStat> VARIABLE_DEFINITION = null;
		CompilerPattern<Statement> STATEMENTS = null;
		CompilerPattern<ExprStat> EXPRESSION = null;
		@SuppressWarnings("unused")
		CompilerPattern<ForStat> FOR_LOOP = null;
		
		FOR_LOOP = CompilerPattern.of(
			syntax(
				require(Type.FOR),
				require(Type.LEFT_PARENTHESIS, "Expected '('"),
				require((context) -> {
					context.push((context.reader.type() != Type.SEMICOLON)
						? VARIABLE_DEFINITION
						: NONE
					);
				}, "Expected ';' or variable definition"),
				require(Type.SEMICOLON, "Expected ';'"),
				require((context) -> {
					context.push((context.reader.type() != Type.SEMICOLON)
						? EXPRESSION
						: NONE
					);
				}, "Expected ';' or expression"),
				require(Type.SEMICOLON, "Expected ';'"),
				require((context) -> {
					context.push((context.reader.type() != Type.RIGHT_PARENTHESIS)
						? EXPRESSION
						: NONE
					);
				}, "Expected ')' or expression"),
				require(Type.RIGHT_PARENTHESIS, "Expected ')'"),
				require((context) -> {
					context.push(STATEMENTS);
				}, "Expected for body")
			),
			(context) -> {
				ForStat stat = new ForStat();
				stat.setVariables(context.get(0));
				stat.setCondition(context.get(1));
				stat.setAction(context.get(2));
				stat.setBody(context.get(3));
				return stat;
			}
		);
	}
}
