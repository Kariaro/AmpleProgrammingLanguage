void pointers() {
	char* string = "test\0";
	char* buffer = &(0x1000);
	
	while(*string) {
		buffer[0] = string[0];
		
		
		string++;
		buffer++;
	}
}

/*
void, pointers
	mov		i8*		[$string], ["test\0"]
	mov		i8*		[$buffer], [0x1000]
	
	
	while(*string) {
		buffer[0] = string[0];
		
		
		string++;
		buffer++;
	}
}
*/