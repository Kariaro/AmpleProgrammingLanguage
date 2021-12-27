/* Storage types are defined with the keyword 'specify'.
 *
 * A storage type is defined by either a group of types or the size
 * of the type.
 *
 * All values made with the keyword 'specify' are constant
 * and can't be changed during runtime.
 */
// specify void  {};  // empty
// specify byte  (1); // 1 byte
// specify bool  (1);
// specify char  (1);

// specify short (2); // 2 bytes
// specify int   (4); // 4 bytes
// specify long  (8); // 8 bytes

// A storage type can also be specified to hold a group of other types
/*
specify Group { // 15 bytes
	int   iField_0x0;
	byte  bField_0x4;
	short sField_0x5;
	long  lField_0x7;
};
*/

// A storage type can also be defined as another type and share the same size
#type WORD  short;
#type DWORD int;
#type QWORD long;

// A global value can be defined with the specify keyword.
#set TRUE  1;
#set FALSE 0;
#set NULL  0;
#set TEXT  "GLOBAL_TEXT";
#set PI    3.14159265359;