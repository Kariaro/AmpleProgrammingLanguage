# Intermediate code
Intermediate code converts from a parse tree (AST) to an intermediate format (IR) before compilation

The intermediate code step converts from a tree structure to a list structure

# Definition
The intermediate code step generates instruction files `InstFile` that contains procedures
```
InstFile
  - Procedure
```

A procedure is a combination of multiple different types of data
- Function
- Code
- Constant