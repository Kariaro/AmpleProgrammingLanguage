# HCProgrammingLanguage
Creating a compiler for my own programing language.
This project is not finished and is currently under construction.


This will not be a library but a nice way for other coders to understand how a compiler compiler
may be built using moderna day code.


# Compiler compiler

## Parser
All code for the LR Parser is put in the source directory *parser/hardcoded/parser*.
There you can see my implementation of a Grammar and a LRParser.

The idea of a LR parser is that it's hard to code a parser that creates parse trees,
therefore we use something that is called a compiler compiler.

A compiler compiler is a generator that generates a compiler for a specified grammar.




## Grammar

A grammar is a way of expressing how a language should be built. It builds on the same
principles as language but in a more unambiguous way. Most context-free-grammars requires
that the set of production rules you specify are unambiguous for parsing.

An example of a grammar could be
```
NUMBER: regex '[0-9]+'

statement: '{' statement '}'
         | 'if' expression 'do' statement
         | 'print' expression ';'

expression: NUMBER '>' NUMBER
          | '(' expression ')'
```

We also need to tell the grammar to process <code>NUMBER</code> as a token and not a
production rule. My way of solving this was to allow regex to be applied.

Using this grammar we could create a simple string and check if it follows our grammar.

Checking if the following string works in our grammar we can use a LR parser.
```
// N is a NUMBER
// S is a STATEMENT
// E is an EXPRESSION

if ( 32 > 31 ) do { print 33 ; }
|  | N  |  N | |  | |      N | |
|  | '- E -' | |  | |      E | |
|   '-- E --'  |  |  '---- S'  |
|       |      |   '------ S -'
|       '-----.|           |
 '------------ S ---------'      
```

And there we have generated a parse tree for this input and can validate that because
all tokens were simplified into one statement that everything follows the grammar and
therefor this is a valid string with our grammar.
