open class Parent {
    protected open fun print() {
        println("parent")
    }

    open fun message() {
        println()
    }
}

class Child : Parent() {
    override fun message() {
        println("child message!!")
    }
    override fun print() {
        println("child")
    }
}

fun main() {
    val actor: Parent = Child() // 대체가능성
    actor.message() // 내적동질성
}
