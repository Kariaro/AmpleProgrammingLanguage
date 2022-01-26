package me.hardcoded.test;

public class Mixer {
	
	private int num1;
	private int num2;
	static int total = 0;
	
	public Mixer(int num1, int num2) {
	this.num1 = num1;
	this.num2 = num2;
	
	total = total + num1;
	System.out.println("Total in the constructor is " + total);
	}
	
	public String toString() {
	return "num1: " + num1 + ", num2: " + num2;
	}
	
	public static void updateTotal(int num1, int num2) {
	total = total + num1 + num2;
	System.out.println("Total after update is " + total);
	}
	
	public static void magicCalculation(int num1, int num2) {
	num1 = num1 + num2;
	num2 = num1 - num2;
	
	System.out.println("method: num1 = " + num1 + " and num2 = " + num2);
	}
	
	public static void mixContent(Mixer mA, Mixer mB) {
	int tempNum1 = mA.num1;
	int tempNum2 = mA.num2;
	
	mA.num1 = mB.num2;
	mA.num2 = mB.num1;
	mB.num1 = tempNum2;
	mB.num2 = tempNum1;
	}
	
	public static void main(String[] args) {
	
	Mixer mA = new Mixer(-5, -7);
	Mixer mB = new Mixer(3, 5);
	Mixer mC = new Mixer(4, 2);
	
	mixContent(mA, mB);
	updateTotal(mA.num1, mB.num1);
	System.out.println("mA is " + mA);
	System.out.println("mB is " + mB);
	System.out.println("mC is " + mC);
	
	int num1 = 8;
	int num2 = 3;
	magicCalculation(num1, num2);
	System.out.println("main: num1 = " + num1+ " and num2 = " + num2);
	
	mixContent(mA, mC);
	System.out.println("mA is " + mA);
	
	mB = mC;
	mB.num1 = 10;
	System.out.println("mB is " + mB);
	System.out.println("mC is " + mC);
	
	mC.num1 = num1;
	mC.num2 = num2;
	System.out.println("mA: "+mA);
	System.out.println("mB: "+mB);
	System.out.println("mC: "+mC);
	}
	}
