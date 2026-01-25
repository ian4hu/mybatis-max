package com.github.ian4hu.mybatis.exposed.dsl

/**
 * @author ian
 * @date 2026/01/16
 */
class SampleDSL {
    private var root : ExprNode? = null


    operator fun String.unaryPlus() : SampleDSL {
        val newRoot = StrNode(this)
        root = newRoot
        return this@SampleDSL
    }

    infix fun eq(right: SampleDSL) : SampleDSL {
        val newRoot = OperatorNode(Op.EQ, root!!, right.root!!)
        root = newRoot
        return this@SampleDSL
    }

    infix fun and(right: SampleDSL) : SampleDSL {
        val newRoot = OperatorNode(Op.AND, root!!, right.root!!)
        root = newRoot
        return this@SampleDSL
    }

    infix fun ExprNode.eq(right: ExprNode): SampleDSL {
        val newRoot = OperatorNode(Op.EQ, this, right)
        root = newRoot
        return this@SampleDSL
    }

}

fun expr(init: SampleDSL.() -> Unit) : SampleDSL {
    return SampleDSL().apply(init)
}

enum class Op {
    EQ,
    AND,
    NOOP_RIGHT,
}
interface ExprNode

class NoopNode : ExprNode

data class StrNode(val value: String) : ExprNode
data class OperatorNode(val op: Op, val left: ExprNode, val right: ExprNode) : ExprNode


fun main() {
    val dsl = expr {
        +"a" eq +"b" and +"c" eq +"d"
    }
}
