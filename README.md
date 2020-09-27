# HCProgrammingLanguage
Creating a compiler for my own programing language.
This project is not finished and is currently under construction.



## Lexer
Lexical analysis is a large part of how a compiler reads the input source file and
converts it into tokens that is then understod by the compiler.

All code related to lexers are placed inside the directory *lexer/hardcoded/lexer*.

Lexical analysis is when you take a input string and group it into tokens. Each token
is then given a group and value representing the catigorisation of the token.


```
Input: "char hello ='a'"

[Primitive]   : char
[Space]       :
[Literal]     : hello
[Space]       :
[Operator]    : =
[CharLiteral] : 'a'
```

The input string was split into tokens of the types <code>Primitive, Space, Literal,
Operator and CharLiteral</code>. These can then be used by the compiler to determine
if the assigned value is of the correct type or if we are defining a variable or
creating a function.



## Visualization
All visualizations are placed inside the *lexer/hardcoded/visualization* path.

Visualization classes are used when debuging the output of the compiler. The visualizations
makes it easier to view and look at how the parsetree gets generated and how the compiler
then optimizes it step by step.

The visualization classes help debug complex data structures and tell the developer what is
going on.