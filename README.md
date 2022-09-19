# Ample Programming Language

The main idea of this compiler is to play around with lexers, optimizations and solving problems that comes with large
projects.

The programming language is still under construction and is not be suitable for any real uses outside of exporting to
interesting instruction sets.

## Exporting

The compiler currently exports to four different languages:

* Ir **Compiler instructions*
* Spooky **Not stable*
* Assembly x64
* Chockintosh I

## Usage

> The compiler cli has not been tested

```
Usage: [options]

options:
    --format-list               displays a list of available output formats

    --project, -p <xml>         compile the project from an xml file

    --target, -t <value>        set the target output of the compiler

    --format, -f <value>        set the format of the compiler

    --use-cache <boolean>       change how the compiler deals with cache files

    -i <source>                 specify the input file to compile

    -o <outputFolder>           specify the output folder
```

## Compiler

Here is an example of the language syntax:

```c
func printhex (i64: number) {
    u8[]: hex_data = stack_alloc<u8, 16>("0123456789abcdef");
    u8[]: hex_strs = stack_alloc<u8, 17>("................\n");

    for (i32: i = 15; i >= 0; i = i - 1) {
        hex_strs[i] = hex_data[number & cast<i64>(15)];
        number = number >> cast<i64>(4);
        continue;
    }

    printstr(hex_strs, 17L);
    ret;
}
```

## Lexer

The lexer is located in the directory `src/main/java/me/hardcoded/lexer`

The compiler lexer is used to take combine a list of characters into bigger groups. One example of a group is numbers
which could be combined using the simple regex `[1-9][0-9]+`

All tokens returned by the lexer contains information about where the token started and ended, file location, content
and group information.

Here is an example of what the lexer does.

```
Input: "char hello ='a'"

Output:
List<Token> = [
    { type: TYPE,   value: "char" , index: 0  },
    { type: SPACE,  value: " ",     index: 4  },
    { type: NAME,   value: "hello", index: 5  },
    { type: SPACE,  value: " ",     index: 10 },
    { type: EQUALS, value: "=",     index: 11 },
    { type: STRING, value: "'a'",   index: 12 }
];
```

## Visualization

The visualization classes are located inside the directory `src/main/java/me/hardcoded/visualization`

Visualizations are used to debug how the compiler works and can help a developer identify errors made by the compiler
and make it easier to fix them.

Currently, there exists three visualization.

* Source Code Viewer
* Parse Tree Viewer
* Instruction Viewer