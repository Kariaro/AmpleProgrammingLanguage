%DISCARD WHITESPACE: ['[ \t\r\n]'] ['#[^\r\n]*']
SPECIAL: 'TOKEN'
DELIMITER: '(' ')' '[' ']' '{' '}' ':' '|'
ITEMNAME: ['[a-zA-Z0-9_]+([ \t\r\n]*)(?=:)']
NAME: ['[a-zA-Z0-9_]+']
LITERAL: ['\'[^\'\\]*(?:\\.[^\'\\]*)*\'']
         ['\"[^\"\\]*(?:\\.[^\"\\]*)*\"']