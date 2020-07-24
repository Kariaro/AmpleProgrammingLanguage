package hc.parser;

public class Result {
	private int count;
	
	private Result(int count) {
		this.count = count;
	}
	
	public int numUsedSymbols() {
		return count;
	}
	
	public static Result create(int count) {
		return new Result(count);
	}
}
