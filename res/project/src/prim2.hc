
// SpookyVM test class

// Imported function
// extern
void printInt(int value);
void print(int value);
int random();

bool isPrime(int value) {
	for(int i = 2; i < value - 1; i++) {
		if((value % i) == 0) return 0;
	}
	
	return 1;
}

void main() {
	int a = 1;
	int b = 2;
	int c = 3;
	int d = 4;
	int e = 5;
	printInt(a);
	printInt(b);
	printInt(c);
	printInt(d);
	printInt(e);
	print('\n');
}

// SpookyVM test class

// Imported function
// extern
void printInt(int value);
void print(int value);
int random();

void ptc(int a, int b, int c, int d, int e) {
	print(a);
	print(b);
	print(c);
	print(d);
	print(e);
	return 0;
}

// Check if a number is a prime or not.

bool isPrime(int value) {
	for(int i = 2; i < value - 1; i++) {
		if((value % i) == 0) return 0;
	}
	
	return 1;
}

void main() {
	ptc('F','a','l','s','e');
	print('\n');
	
	for(int i = 9; i < 10; i++) {
		bool test = isPrime(i);
		
		ptc('I','s',' ',0,0);
		printInt(test);
		ptc(' ','p','r','i','m');
		ptc('e','?',' ',0,0);
		
		if(test) {
			ptc('T','r','u','e',0);
		} else {
			ptc('F','a','l','s','e');
		}
		ptc('\n',0,0,0,0);
	}
}