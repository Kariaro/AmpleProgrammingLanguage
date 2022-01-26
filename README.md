# Ample Programming Language
This compiler is my hobby project. This is my own programming language and it has similar features to C.

The idea of the language is to play around with lexers and compiler optimizations and get better with solving complex compiler problems.

This programming language is still under construction and is not be suitable for any real use outside of exporting to interesting instruction sets.

## Exporting
The compiler currently exports to four different languages:
* IR **My compiler instruction set*
* Spooky **Not stable*
* Assembly x64
* Chockintosh I

## Usage
To use this compiler you can call it from the console. *Only tested on windows*

```
Usage: [options]

options:
    -? -h --help
                  display this help message

    -w --working-directory <path>
                  set the working directory

    -s --source-folders "<path 1>;<path 2>; ..."
                  add source folder paths

    -f --format <format>
                  the output format type
                  [chockintosh]
                  [spooky]
                  [x86]
                  [ir]

    -i --input-file <pathname>
                  set the main entry point of the compiler

    -o --output-file <pathname>
                  set the output file of this compiler

    -b --bytecode
                  set the output to bytecode (default)

    -a --assembler
                  set the output to assembler

    -c --compile
                  set the compiler mode to compile (default)

    -r --run
                  set the compiler mode to run

```

## Compiler

Here is an example of the syntax:

```c
void print(int v);

void main() {
    int a = 3;
    print(a);
}

void print(int v) {
    *((int*)0xb8000) = v;
}
```


## Lexer
The compiler lexer is a large part of how this compiler functions. The lexer is located inside the directory `src/main/java/me.hardcoded/lexer`.

The lexer is first construction with a set of rules for how to group characters. All groups are written with regex currently.
An example of a number matching group would be `[0-9]+`.

All tokens returned by the lexer has information about where the token started and ended, file location, content and group information.

Here is an example of what the lexer does.
```
Input: "char hello ='a'"

[Identifier]  : char
[Whitespace]  :
[Literal]     : hello
[Whitespace]  :
[Assignment]  : =
[String]      : 'a'
```

## Visualization
The visualization classes are located inside the directory `src/main/java/me.hardcoded/visualization`.

Visualizations are used to debug how the compiler works and can help a developer identify errors made by the compiler andd make it easier to fix them.

Currently there is only one visualization that shows a tree structure of the current program loaded.