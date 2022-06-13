package parser

import kotlin.reflect.KProperty

fun <T : Any> jsonObject(target: T): String {
    val builder = buildString {
        target::class.members
            .filterIsInstance<KProperty<*>>()
            .joinTo(buffer = this, separator = ", ", prefix = "{", postfix = "}") {
                "${stringify(it.name)} : ${stringify(it.getter.call(target))}"
            }
    }

    return builder
}

private fun stringify(value: Any?) = when (value) {
    null -> "null"
    is String -> jsonString(value)
    is Boolean, is Number -> value.toString()
    is List<*> -> jsonList(value)
    else -> jsonObject(value)
}

fun jsonList(target: List<*>): String {
    val builder = buildString {
        target.joinTo(buffer = this, separator = ", ", prefix = "[", postfix = "]", transform = ::stringify)
    }

    return builder
}

private fun jsonString(v: String) = """"${v.replace("\"", "\\\"")}""""

class Json0(val a: Int, val b: String, val c: List<Int>)

fun main() {
    println(stringify(Json0(0, "roach", listOf(1, 2, 3))))
}
