// include "test.hc";


/* This is a multiline comment.
 * 
 * A multiline comment is a comment that can
 * span accross multiple lines.
 * 
 * This file will also include everything tha
 * my compiler will be able to parse and do.
 */
 
// Single line comments will also be available.
//
// These comments can be places anywhere but will
// consume all characters after the two dashes.


// An include statement will be used to link multiple
// files together into one file.
// %include "testing.hc";

void main(int asd2) {

}

/* My language will allow for functions to
 * be created outside classes and to allow
 * modifiers that change how the function
 * would be compiled.
 * 
 * When a function has the 'export' modifier.
 * Then the compiled code will have it's name
 * and address put into a export header that
 * can be used to call the function from another
 * process.
 */
export void main(int input, int a, int b, int c) {
	int var = 204 ^ 229 * 119 ^ 75 ^ (5) - 163 + 69 + 10 * 30 * 191 | 145 * 139 + 183 * 165 * 248 * 6 & 183 * 243 ^ 11;
	
	int test = 32;
	int* test2 = &test; // Unary get pointer address to value.
	int test3[(204 ^ 229 * 119 ^ 75 ^ (5) - 163 + 69 + 10 * 30 * 191 | 145 * 139 + 183 * 165 * 248 * 6 & 183 * 243 ^ 11)];// + 32];
	int i = 1 + 2 + 3 + 4 + 5;
	
	int test4[(1 ? 15:32)];
	
	if((1 + 2) || 3 || 5 || 123232323) {
		// Testing BiExprV2
	}
	
	if(test3[3][2][1] == 0) {
		// TEST
	}
	
	if(a > b) {
		while(1 > 0) {
			break;
		}
	}
	
	if(input > 3) {
		// print("This is a \" \" \"static message!");
	} else {
		// print("Inside else statement!");
	}
	// test();
	// print(c);
	
	
	for(int i = 0; i < 10; i++) {
		print("TEST");
	}
	
	int testing = 1 + 3 * 427 & 43 + 20312 + print("testing");
	testing += 32;
	
	char* text = "TESTING";
	print("TESTING");
	
	bool result = print(text);
	return result;
}

bool print(char* string) {
	char* buffer = &(0xb8000);
	
	while(string[0] > 0) {
		buffer[0] = string[0];
		string++;
		buffer++;
	}
	
	return 0;
}

int testing() {
	return 0;
}