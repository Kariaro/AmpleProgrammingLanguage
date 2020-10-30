// SpookyVM test class

// Imported function
void printInt(int value);
void print(int value);
int random();

bool isPrime(int number) {
	if(number < 2) return false;
	for(int i = 2; i * i <= number; i++) {
		if(!(number % i))
			return false;
	}
	
	return true;
}

void prints(char* chars) {
	while(*chars != 0) {
		char b = *(chars++);
		int a = b + (b / b) * 0;
		print(a);
	}
}

void println(char* chars) {
	prints(chars);
	print('\n');
}

void main() {
	prints("Testing weird stuff Huh 11223344\nabcd");
	
	/*
	for(int i = 0; i < 100; i++) {
		prints("Is ");
		printInt(i);
		prints(" prime? (");
		
		if(isPrime(i)) {
			println("True)\n");
		} else {
			println("False)");
		}
	}
	
	print('\n');
	*/
}