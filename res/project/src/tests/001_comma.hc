int comma() {
	int a = 4;
	(a += 3, 4, 5, a) += 3;
	// 'a' should have a value of '10'
	
	int b = 3;
	int c = ((b) += 3);
	// 'b' and 'c' should have a value of '6'
	
	int d = 2;
	int e = (d++);
	// 'd' should have a value of '3'
	// 'e' should have a value of '2'
	
	return a;
}