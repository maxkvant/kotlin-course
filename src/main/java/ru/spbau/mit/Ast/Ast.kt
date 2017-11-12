package ru.spbau.mit.Ast

sealed class Node

sealed class Statement : Node()

sealed class Expression : Statement()

data class FunctionDef(val name: Identifier, val params: List<Identifier>, val block: Block) : Statement()

data class VariableDef(val name: Identifier, val value: Expression?) : Statement()

data class While(val loopExpr: Expression, val block: Block) : Statement()

data class If(val boolExpr: Expression, val blockTrue: Block, val blockFalse: Block) : Statement()

data class Block(val statements: List<Statement>) : Statement()

data class Return(val expr: Expression) : Statement()

data class Assignment(val identifier: Identifier, val expr: Expression) : Statement()

data class BinaryOp(val l: Expression, val operation: Operation, val r: Expression) : Expression()

data class FunctionCall(val name: Identifier, val args: List<Expression>) : Expression()

data class VariableCall(val name: Identifier) : Expression()

data class Identifier(val str: String) : Node()

data class Literal(val num: Long) : Expression()

object Ast {
    val printLn: FunctionDef = FunctionDef(Identifier("println"),
            emptyList<Identifier>(),
            Block(emptyList<Statement>()))
}

enum class Operation(val str: String) {
    Eq("=="),
    Neq("!="),
    Or("||"),
    And("&&"),
    Lt("<"),
    Le("<="),
    Gt(">"),
    Ge("=>"),
    Plus("+"),
    Minus("-"),
    Multiply("*"),
    Divide("/"),
    Rem("%")
}