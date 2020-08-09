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
}

void testing() {
	if(awesome > 10000) {
		// This is sick
	}
}


/* Inside the functions there will be alot of different
 * ways you can code.
 */
void test() {
	// Variable declaration
	// int a = 0;
	
	// Pointer declaration
	// int* c = &b;
	
	// Array declaration
	// int array[32];
	
	// Array writing
	// array[0] = 3;
	
	// Switch statements
	// switch(a) {
	// 	case 0: break;
	// 	case 1:
	// 	default: break;
	// }
	
	// Lables, branches and goto statements
	// Label:
	{
		// While loops
		// while(a++ < 2) {
			// goto Label
		// }
		
		// For loops
		// for(a = 0; a < 5; a++) {
			// ...
		// }
		
		// Comparisons
		if(a == 5) {
			
		}
	}
	
	// Casting of datatypes
	// char d = (char)a;
	
	// Declaration of objects
	// Object obj = new Object();
	
	// Declaration of structs
	// Data data;
	
	// Declaration of enums
	// Fields field;
	
	// Return statements
	// return;
}

void print(char* string) {
	// char* buffer = &(0xb8000);
	
	// while(string[0] > 0) {
		// buffer[0] = string[0];
		// string++;
		// buffer++;
	// }
	
	// for
	// do
	// case
	// switch
	// goto
	
	// asm(
		// "mov ah, 0",
		// ""
	// );
}
