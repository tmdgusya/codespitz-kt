package parser

import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY)
annotation class Ex

@Target(AnnotationTarget.PROPERTY)
annotation class Name(val name: String)

/**
 * over loading
 */
fun <T> Iterable<T>.joinTo(
    sep: () -> Unit,
    transform: (T) -> Unit,
) {
    forEachIndexed { count, element ->
        if (count != 0) sep() // 첫번째가 아닐때만 separator 를 부르면 됨.
        transform(element)
    }
}

fun stringify(value: Any?): String = buildString {
    jsonValue(value)
}

private fun <T : Any> StringBuilder.jsonObject(target: T) {
    wrap(begin = '{', end = '}') {
        target::class.members
            .filterIsInstance<KProperty<*>>()
            .filter { it.findAnnotation<Ex>() == null }
            .joinTo(::comma) {
                jsonValue(it.findAnnotation<Name>()?.name ?: it.name)
                append(" : ")
                jsonValue(it.getter.call(target))
            }
    }
}

private fun StringBuilder.comma() {
    append(", ")
}

private fun StringBuilder.wrap(begin: Char, end: Char, block: StringBuilder.() -> Unit) {
    append(begin)
    block()
    append(end)
}

private fun StringBuilder.jsonValue(value: Any?) {
    when (value) {
        null -> append("null")
        is String -> jsonString(value)
        is Boolean, is Number -> append(value.toString())
        is List<*> -> jsonList(value)
        else -> jsonObject(value)
    }
}

private fun StringBuilder.jsonList(target: List<*>) {
    wrap(begin = '[', end = ']') {
        target.joinTo(::comma) {
            jsonValue(it)
        }
    }
}

private fun StringBuilder.jsonString(v: String) {
    append(""""${v.replace("\"", "\\\"")}"""")
}

class Json0(val a: Int, @Name("name") val b: String, val c: List<Int>)

fun main() {
    println(stringify(Json0(0, "roach", listOf(1, 2, 3))))
}
