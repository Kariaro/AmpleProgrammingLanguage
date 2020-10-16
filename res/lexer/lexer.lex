# This is the lexer file for the programming language

TOKEN: ['.']

%DISCARD WHITESPACE: ['[ \t\r\n]']
%DISCARD COMMENT: %DELIMITER('/*', '', '*/')
                  ['//[^\r\n]*']

IDENTIFIER: ['[a-zA-Z_][a-zA-Z0-9_]*']

DELIMITER:	'+=' '-=' '*=' '/=' '%=' '^=' '>>=' '<<='
			'>>' '<<' '++' '--' '&=' '|='
			'||' '&&' '==' '>=' '<=' '!='

STRING: %DELIMITER('\"', '\\', '\"')
CHAR: %DELIMITER('\'', '\\', '\'')

FLOAT: ['[0-9]+[.][0-9]+[Ff]']
DOUBLE: ['[0-9]+[.][0-9]+[Dd]?']
LONG: ['0x[0-9a-fA-F]+L'] ['0b[0-1]+L'] ['[0-9]+L']
INT: ['0x[0-9a-fA-F]+'] ['0b[0-1]+'] ['[0-9]+']
BOOL: 'true' 'false'