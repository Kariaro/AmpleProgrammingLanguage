# Lexer file
# Regex inside square brackets

%DISCARD WHITESPACE: ['[ \t\r\n]']
%DISCARD COMMENT: %DELIMITER('/*', '', '*/')
                  ['//[^\r\n]*']

BUILD_INCLUDE: '%include'
BUILD_SPECIFY: 'specify'

# Primitive types. (Always present with the compiler)
PRIMITIVE: 'void' 'byte' 'char' 'bool' 'int' 'short' 'long' 'float' 'double'

# The most important keyword is 'asm'
KEYWORD: 'if' 'for' 'while' 'asm'
         'return' 'break' 'continue' 'as'

# This should be changed by the compiler
TYPE_NAME:


DELIMITER:  '{' '}' '(' ')' '[' ']' '.' ';' ',' ':' '?'
ASSIGNMENT: '+=' '-=' '*=' '/=' '%=' '^=' '>>=' '<<=' '='
OPERATOR:   '>>' '<<' '++' '--' '+' '-' '*' '/' '%' '^' '&' '|' '~'
COMPARISON: '||' '&&' '==' '>=' '<=' '!=' '>' '<'

IDENTIFIER: ['[a-zA-Z_][a-zA-Z0-9_]*']

STRINGLITERAL: %DELIMITER('\"', '\\', '\"')
               %DELIMITER('\'', '\\', '\'')

DECIMALLITERAL: ['(-)?[0-9]+[.][0-9]+']

INTEGERLITERAL: ['(-)?[0-9]+']
                ['(-)?0b[0-1]+']
                ['(-)?0x[0-9a-fA-F]+']
                '0'