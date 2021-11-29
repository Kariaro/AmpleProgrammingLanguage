void assign_value() {
	int a = 0;
	
	a += 1;
	(1, a) -= 2;
	
	a++;
	++a;
}

void assign_array() {
	int b[1];
	
	b[0] = 1;
	(1, b[0]) += 1;
	
	b[0]--;
	--b[0];
}

void assign_comma() {
	int a = 0;
	int b = 0;
	
	(1, b, a)++;
	
	char c[1];
	
	--(0, a++, c)[b++];
}

void test() {
	assign_value();
	assign_array();
	assign_comma();
}