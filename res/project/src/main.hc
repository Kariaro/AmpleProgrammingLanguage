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
// @import "testing.hc";

bool print(char* string);
void fortest() {
	*((int*)0xf) = 5;
	
	for(; *((int*)0xf) > 0; *((int*)0xf)--) {
		print("Loop");
	}
}

void expr() {
	int _ = 1 - 32 + 32 - 32 + 32; // Exptected 1
	
	if(10 != 9) {
		print("Why?!??!?!?");
	}
}

void main2(int args) {
	int array[2];
	
	array[0] = 1;
	array[1] = 2;
	
	char chars[2];
	
	// Convert number into string...
	chars[0] = array[0] + 0x20;
	chars[1] = '\0';
	
	print(chars);
}

// specify Structure {};
// specify GLOBAL 15;

//@import "test.hc";
@set GLOBAL 15;
@type WORD short;
// @unset void;

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
	
	int test = 32, arg2 = 3, arg73 = "test";
	int* test2 = &test; // Unary get pointer address to value.
	// int test3[(204 ^ 229 * 119 ^ 75 ^ (5) - 163 + 69 + 10 * 30 * 191 | 145 * 139 + 183 * 165 * 248 * 6 & 183 * 243 ^ 11)];// + 32];
	int i = 1 + 2 + 3 + 4 + 5;
	int var2 = (int)(byte)1234283; // Should be 107
	
	WORD test3[GLOBAL];
	int i32 = 0 + 0;
	
	// TODO: Add assert
	
	int test4 = -1;
	
	byte chr = (byte)23;
	
	if(0 && (test4 += 1)) print("Because of the zero, the rest should be removed.");
	if(1 || (test4 += 1)) print("Because of the one, the rest should be removed.");
	if(1 || (test4 += 1)) print("Because of the one, the rest should be removed.");
	
	// Should throw an error.
	void __0 = (byte)32;
	void __1 = (short)32;
	void __2 = (int)32;
	void __3 = (long)32;
	// void __4 = (float)32;
	// void __5 = (double)32;
	
	// int test44[(1 ? 15:32)];
	if(1 || 2 || 3) print("1st");
	if(1 && 2 && 3) print("2nd");
	if(0 || 0 || 0) print("3rd");
	if(1 && 2 && 0) print("4th");
	
	// if((1 + 2) && (0, += 32) && 1) continue;
	if(test3[3] == 0) break; // [2][1]
	
	if(a > b) {
		while(1 > 0) {
			break;
		}
	}
	
	if(input > 3) {
		print("This is a \" \" \"static message!");
	} else {
		print("Inside else statement!");
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
	int* index = (int*)0xb7ffc;
	buffer += *index;
	
	while(string[0]) {
		buffer[0] = string[0];
		buffer++;
		string++;
		(*index)++;
	}
	
	buffer[0] = '\n';
	(*index)++;
	
	return 0;
}