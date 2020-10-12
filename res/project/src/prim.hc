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
	int len = chars[0];
	
	for(int i = 0; i < len; i++) {
		print(chars[-(i + 1)]);
	}
}

void println(char* chars) {
	prints(chars);
	print('\n');
}

void main() {
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
}