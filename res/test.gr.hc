void test() {
	// Variable declaration
	int a = 0;
	
	// Pointer declaration
	int* c = &b;
	
	// Array declaration
	int array[32];
	
	// Array writing
	array[0] = 3;
	
	// Switch statements
	switch(a) {
		case 0: break;
		case 1:
		default: break;
	}
	
	// Lables, branches and goto statements
	Label: {
		// While loops
		while(a++ < 2) {
			goto Label
		}
		
		// For loops
		for(a = 0; a < 5; a++) {
			// ...
		}
		
		// Comparisons
		if(a == 5) {
			
		}
	}
	
	// Casting of datatypes
	char d = (char)a;
	
	// Declaration of objects
	Object obj = new Object();
	
	// Declaration of structs
	Data data;
	
	// Declaration of enums
	Fields field;
	
	// Return statements
	return;
}