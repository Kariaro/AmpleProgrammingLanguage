# Lexer file
# Regex inside square brackets

BUILD_INCLUDE: '%include'
BUILD_SPECIFY: 'specify'

PRIMITIVE: 'void' 'int' 'char' 'bool' 'long'

# The most important keyword is 'asm'
KEYWORD: 'if' 'for' 'while' 'asm'

DELIMITER:  '(' ')' '[' ']' '.' ';'
ASSIGNMENT: '+=' '-=' '*=' '/=' '%=' '^=' '>>=' '<<=' '='
OPERATORS:  '+' '-' '*' '/' '%' '^' '&' '|' '>>' '<<'
COMPARISON: '||' '&&' '==' '>=' '<=' '!=' '>' '<'


STRINGLITERAL: %DELIMITER('\"', '\\', '\"')
               %DELIMITER('\'', '\\', '\'')

INTEGERLITERAL: ['(-)?[1-9][0-9]+']
                ['(-)?0b[0-1]+']
                ['(-)?0x[0-9a-fA-F]+']

DECIMALLITERAL: ['(-)?([1-9][0-9]+)\\.([0-9]+)']

%DISCARD COMMENT: %DELIMITER('/*', %NONE, '*/')
                  ['//.*?(\r\n|\r|\n)']

%DISCARD WHITESPACE: ['[ \t\r\n]']