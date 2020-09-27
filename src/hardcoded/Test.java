package hardcoded;

public class Test {
	public static void main(String[] args) {
		String[] words;
		int[] score;
		char[] letters;
		
		words = new String[] { "add","dda","bb","ba","add" };
		score = new int[] {3,9,8,9,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		letters = new char[] {'a','a','a','a','b','b','b','b','c','c','c','c','c','d','d','d' };
//		words = new String[] {"xxxz","ax","bx","cx" };
//		letters = new char[] {'z','a','b','c','x','x','x'};
//		score = new int[] { 4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,0,10};
		
		Test test = new Test();
		for(int i = 0; i < 100000000; i++);
		
		long ns = System.nanoTime();
		int value = test.maxScoreWords(words, letters, score);
		ns = System.nanoTime() - ns;
		System.out.println("Output: " + value + ", Took: " + String.format("%.5f ms", (ns / 1000000.0D)));
	}
	
	public int[] construct(char[] s) {
		int[] result = new int[26];
		for(int i = 0; i < s.length; i++) result[s[i] - 97]++;
		return result;
	}
	
	public int[] construct(String s) {
		int[] result = new int[26];
		for(int i = 0; i < s.length(); i++) {
			int v = s.charAt(i) - 97;
			if(++result[v] > AVAILABLE[v]) return null;
		}
		
		return result;
	}
	
	public int score(int[] array) {
		int result = 0;
		for(int i = 0; i < 26; i++) result += SCORE[i] * array[i];
		return result;
	}
	
	public boolean combine(int[] mat1, int[] mat2) {
		for(int i = 0; i < 26; i++) {
			TEMP[i] = mat1[i] + mat2[i];
			if(TEMP[i] > AVAILABLE[i]) return false;
		}
		return true;
	}
	
	public int[][] MATRIX;
	public int[] SCORE;
	public int[] AVAILABLE;
	public int[] TEMP;
	public int size;
	
	public int maxScoreWords(String[] words, char[] letters, int[] score) {
		size = words.length;
		SCORE = score;
		
		AVAILABLE = construct(letters);
		MATRIX = new int[size][];
		TEMP = new int[26];
		
		for(int i = 0, j = 0; i < words.length; i++, j++) {
			MATRIX[j] = construct(words[i]);
			if(MATRIX[j] == null) {
				j--;
				size--;
			}
		}
		
		int result = 0;
		for(int i = 0; i < size; i++) {
			result = Math.max(result, Math.max(score(MATRIX[i]), recurse(i + 1, MATRIX[i])));
		}
		
		return result;
	}
	
	public int recurse(int i, int[] mat1) {
		int result = 0;
		
		for(; i < size; i++) {
			if(combine(mat1, MATRIX[i])) {
				result = Math.max(result, Math.max(score(TEMP), recurse(i + 1, TEMP.clone())));
			}
		}
		
		return result;
	}
}
