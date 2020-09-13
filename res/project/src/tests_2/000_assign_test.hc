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
	
	int c[1];
	
	--(0, a++, c)[b++];
	// decptr(add(comma(set($temp1, a), set(a, add(a, 1i)), c), comma(set($temp2, b), set(b, add(b, 1i)), $temp2)))
}