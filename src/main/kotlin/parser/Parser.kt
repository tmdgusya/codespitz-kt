package parser

import kotlin.reflect.KProperty

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

fun stringify(value: Any?): String {
    val builder = StringBuilder()
    builder.jsonValue(value)
    return builder.toString()
}

private fun <T : Any> StringBuilder.jsonObject(target: T) {
    append("{")
    target::class.members
        .filterIsInstance<KProperty<*>>()
        .joinTo({ append(", ") }) {
            jsonValue(it.name)
            append(" : ")
            jsonValue(it.getter.call(target))
        }
    append("}")
}

fun StringBuilder.jsonValue(value: Any?) {
    when (value) {
        null -> append("null")
        is String -> jsonString(value)
        is Boolean, is Number -> append(value.toString())
        is List<*> -> jsonList(value)
        else -> jsonObject(value)
    }
}

fun StringBuilder.jsonList(target: List<*>) {
    target.joinTo(buffer = this, separator = ", ", prefix = "[", postfix = "]", transform = ::stringify)
}

fun StringBuilder.jsonString(v: String) {
    append(""""${v.replace("\"", "\\\"")}"""")
}

class Json0(val a: Int, val b: String, val c: List<Int>)

fun main() {
    println(stringify(Json0(0, "roach", listOf(1, 2, 3))))
}
