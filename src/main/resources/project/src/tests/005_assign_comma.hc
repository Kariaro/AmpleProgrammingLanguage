void main() {
	int a = 3;
	int b = 1;
	
	int x = 0;
	int y = 3;
	
	(a += 2, b += 2, x) = y++ + (x += 2);
	
	(a += 2, x) = (y += 2, y);
}