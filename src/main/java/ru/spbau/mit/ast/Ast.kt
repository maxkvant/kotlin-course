package ru.spbau.mit.ast

sealed class Node

sealed class Statement : Node() {
    abstract val line: Int
}

sealed class Expression : Statement()

data class FunctionDef(
    val name: Identifier,
    val params: List<Identifier>,
    val block: Block,
    override val line: Int
) : Statement() {
    companion object {
        val printLn: FunctionDef =
                FunctionDef(Identifier("println"),
                            emptyList(),
                            Block(emptyList(), -1),
                            -1)
    }
}

data class VariableDef(
    val name: Identifier,
    val value: Expression?,
    override val line: Int
) : Statement()

data class While(
    val loopExpr: Expression,
    val block: Block,
    override val line: Int
) : Statement()

data class If(
        val boolExpr: Expression,
        val blockTrue: Block,
        val blockFalse: Block?,
        override val line: Int
) : Statement()

data class Block(
    val statements: List<Statement>,
    override val line: Int
) : Statement()

data class Return(
    val expr: Expression,
    override val line: Int
) : Statement()

data class Assignment(
    val identifier: Identifier,
    val expr: Expression,
    override val line: Int
) : Statement()

data class BinaryOp(
    val l: Expression,
    val operation: Operation,
    val r: Expression,
    override val line: Int
) : Expression()

data class FunctionCall(
    val name: Identifier,
    val args: List<Expression>,
    override val line: Int
) : Expression()

data class VariableCall(
    val name: Identifier,
    override val line: Int
) : Expression()

data class Identifier(val str: String) : Node()

data class Literal(
    val num: Long,
    override val line: Int
) : Expression()


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