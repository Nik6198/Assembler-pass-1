# Assembler-pass-1

To run the program first compile Assember.java<br/>
javac Assembler.java<br/>

and then comiple and run Pass1.java<br/>
javac Pass1.java<br/>
java Pass1<br/>

There should be input file in current directory and output is written into output file.<br/>

The opcode table and register table in files "opcodetab" and "register" respectively are used for creating the Intermediate code and Data Structures used for pass2.<br/>

Algorithm used : <br/>

1. Set LC=0.
2. While next statement is not an END then
a). If symbol definition is found then make an entry in ST.
b). If START then LC= Value given after start. Generate intermediate code
(10,01) (C, Address specified )
c). If EQU statement then add symbol to ST which is on RHS of EQU and
assign address of it as address of symbol on LHS of EQU.
d) If declaration statement then find size of memory area required and code
associated with the statement and generate intermediate code (01,CODE)
and increment LC =LC + size.
e). IF an imperative statement
1. LC=LC + length
2. Generate intermediate code (00, CODE)
3. If operand is Literal then enter it into LT. IF IT is symbol then enter it into
ST.
4. If END statement generate intermediate code (10, 02) and goto pass-II.


