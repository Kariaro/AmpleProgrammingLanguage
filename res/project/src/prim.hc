// SpookyVM test class

// Imported function
// extern
void printInt(int value);

// Check if a number is a prime or not.
bool isPrime(int value) {
	for(int i = 2; i < value - 1; i++) {
		int a = (value / i) * i;
		if(a == value) return 0;
	}
	
	return 1;
}

void main() {
	int a = isPrime(661);			// True
	int b = isPrime(2500);			// False
	//int c = isPrime(661);		// True
	
	printInt(a);
	//printInt(b);
	//printInt(c);
}