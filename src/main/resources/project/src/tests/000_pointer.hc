void pointers() {
	char* string = "test\0";
	char* buffer = &(0x1000);
	
	while(*string) {
		buffer[0] = string[0];
		string++;
		buffer++;
	}
}