/* Declaration of the print function */
bool print(char* string);

/* This code should print to the screen buffer */
export void main() {
	// TODO: Array assignment
	char* text = "TEST\0";
	print(text);
	
	return;
}

bool print(char* string) {
	char* buffer = &(0xb8000);
	
	// TODO: ++ and -- operators
	while(string[0] > 0) {
		buffer[0] = string[0];
		string = string + 1;
		buffer = buffer + 2;
	}
	
	return 0;
}