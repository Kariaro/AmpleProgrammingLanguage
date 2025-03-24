package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.ValueType;

import java.io.IOException;

public class BinaryExpr extends Expr {
	private Expr left;
	private Expr right;
	private Operation operation;
	
	public BinaryExpr(ISyntaxPos syntaxPos, Operation operation, Expr left, Expr right) {
		super(syntaxPos);
		this.left = left;
		this.right = right;
		this.operation = operation;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public Expr getLeft() {
		return left;
	}
	
	public Expr getRight() {
		return right;
	}
	
	@Override
	public boolean isEmpty() {
		// TODO: Some binary operations are not empty
		return operation != Operation.ASSIGN;
	}
	
	@Override
	public boolean isPure() {
		return left.isPure() && right.isPure();
	}
	
	@Override
	public ValueType getType() {
		// Both left and right must have the same value type
		if (operation == Operation.ARRAY) {
			// This will return the type of one lower
			// Should be invalid for non pointer types
			return left.getType().createArray(left.getType().getDepth() - 1);
		}
		
		if (operation == Operation.MEMBER) {
			return right.getType();
		}
		
		return left.getType();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.BINARY;
	}
	
	@Override
	public String toString() {
		return "(" + left + " " + operation.getName() + " " + right + ")";
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		left.serialize(stream);
		stream.writeVarInt(operation.ordinal());
		right.serialize(stream);
	}
	
	public static BinaryExpr deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Expr left = stream.deserializeExpr();
		Operation operation = Operation.VALUES[stream.readVarInt()];
		Expr right = stream.deserializeExpr();
		return new BinaryExpr(head.syntaxPos(), operation, left, right);
	}
}
