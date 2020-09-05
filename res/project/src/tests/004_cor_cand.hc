void main() {
	int a = 0;
	int b = (1 && 2 && (a += 3, 4));
	// a = 3
	// b = 1
	
	int c = 0;
	int d = (b && a && c && (c += (c += 3) + 3));
	// c = 3
	// d = 1
	
	int x = 0;
	int y = 3;
	
	(a += 2, b += 2, x) = y++ + (x += 2);
	
	(a += 2, x) = (y += 2, y);
}