
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