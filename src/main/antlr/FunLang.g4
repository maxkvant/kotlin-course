grammar FunLang;

file
    : block
    ;

block
    : statement*
    ;

statement
    :   function
    |   variable
    |   expression
    |   whilee
    |   iff
    |   assignment
    |   returnn
    ;

blockWithBraces
    : '{' block '}'
    ;

function
    : 'fun' identifier '(' parameterNames ')' blockWithBraces
    ;

parameterNames
    : identifier (',' identifier)*
    |
    ;

variable
    : 'var' identifier ( '=' expression)?
    ;

iff
    : 'if' '(' expression ')' blockWithBraces ( 'else' blockWithBraces )?
    ;

whilee
    : 'while' '(' expression ')' blockWithBraces
    ;

assignment
    : identifier '=' expression
    ;

returnn
    : 'return' expression
    ;

expression
    : arifmeticExpression
    ;

functionCall
    : identifier '(' arguments ')'
    ;

arguments
    : expression (',' expression)*
    |
    ;

// +, -, *, /, %, >, <, >=, <=, ==, !=, ||, &&

simpleExpression
    : functionCall
    | identifier
    | literal
    | '(' expression ')'
    ;

arifmeticExpression
    : logicalOrExpression
    ;

multiplicativeExpression
    :   simpleExpression
    |   multiplicativeExpression multiplicativeOp simpleExpression
    ;

multiplicativeOp
    : '*'
    | '/'
    | '%'
    ;

additiveExpression
    :   multiplicativeExpression
    |   additiveExpression additiveOp multiplicativeExpression
    ;

additiveOp
    : '+'
    | '-'
    ;

relationalExpression
    :   additiveExpression
    |   additiveExpression relationalOp additiveExpression
    ;

relationalOp
    : '<'
    | '>'
    | '<='
    | '>='
    | '=='
    | '!='
    ;

logicalAndExpression
    :   relationalExpression
    |   logicalAndExpression '&&' relationalExpression
    ;

logicalOrExpression
    :   logicalAndExpression
    |   logicalOrExpression '||' logicalAndExpression
    ;

// --------------------------------------------

identifier
    : Identifier
    ;

Identifier
    : IdentifierNondigit (IdentifierNondigit | Digit)*
    ;

fragment
IdentifierNondigit
    : Nondigit
    ;

fragment
Nondigit
    : [a-zA-Z_]
    ;

fragment
Digit
    : [0-9]
    ;

literal
    : Literal
    ;

Literal
    : (NonzeroDigit Digit*) | Digit
    ;

fragment
NonzeroDigit
    : [1-9]
    ;


COMMENT
    :   '//' ~[\r\n]*
        -> skip
    ;

WS
    : (' ' | '\t' | '\r'| '\n') -> skip
    ;