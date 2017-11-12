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
    ;

variable
    : 'var' identifier ( '=' expression)?
    ;

iff
    : blockWithBraces '(' expression ')' ( 'else' blockWithBraces )
    ;

whilee
    : 'while' '(' expression ')' blockWithBraces
    ;

assignment
    : identifier '=' expression
    ;

returnn
    : 'retrurn' expression
    ;

expression
    : simpleExpression
    | binaryExpression
    ;

functionCall
    : identifier '(' arguments ')'
    ;

arguments
    : expression (',' expression)*;

// +, -, *, /, %, >, <, >=, <=, ==, !=, ||, &&

simpleExpression
    : functionCall
    | identifier
    | literal
    | '(' simpleExpression ')'
    ;

binaryExpression
    : additiveExpression
    ;

multiplicativeExpression
    :   simpleExpression
    |   multiplicativeExpression multiplicativeOp expression
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

/*relationalExpression
    :   additiveExpression
    |   relationalExpression relationalOp additiveExpression
    ;

relationalOp
    : '<'
    | '>'
    | '<='
    | '>='
    ;

equalityExpression
    :   relationalExpression
    |   equalityExpression equalityOp relationalExpression
    ;

equalityOp
    : '=='
    | '!='
    ;

logicalAndExpression
    :   equalityExpression
    |   logicalAndExpression '&&' equalityExpression
    ;

logicalOrExpression
    :   logicalAndExpression
    |   logicalOrExpression '||' logicalAndExpression
    ;*/

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

WS
    : (' ' | '\t' | '\r'| '\n') -> skip
    ;