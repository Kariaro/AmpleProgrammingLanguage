%DISCARD WHITESPACE: ['[ \t\r\n]'] ['#[^\r\n]*']
SPECIAL: '%DISCARD' '%DELIMITER'
DELIMITER: '[' ']' '(' ')' ',' ':'
ITEMNAME: ['[a-zA-Z0-9_]+([ \t\r\n]*)(?=:)']
LITERAL: ['\'[^\'\\]*(?:\\.[^\'\\]*)*\'']
         ['\"[^\"\\]*(?:\\.[^\"\\]*)*\"']