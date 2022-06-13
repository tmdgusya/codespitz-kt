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
    jsonValue(value, builder)
    return builder.toString()
}

private fun <T : Any> jsonObject(target: T, builder: StringBuilder) {
    builder.append("{")
    target::class.members
        .filterIsInstance<KProperty<*>>()
        .joinTo({ builder.append(", ") }) {
            jsonValue(it.name, builder)
            builder.append(" : ")
            jsonValue(it.getter.call(target), builder)
        }
    builder.append("}")
}

private fun jsonValue(value: Any?, builder: StringBuilder) {
    when (value) {
        null -> builder.append("null")
        is String -> jsonString(value, builder)
        is Boolean, is Number -> builder.append(value.toString())
        is List<*> -> jsonList(value, builder)
        else -> jsonObject(value, builder)
    }
}

private fun jsonList(target: List<*>, builder: StringBuilder) {
    target.joinTo(buffer = builder, separator = ", ", prefix = "[", postfix = "]", transform = ::stringify)
}

private fun jsonString(v: String, builder: StringBuilder) {
    builder.append(""""${v.replace("\"", "\\\"")}"""")
}

class Json0(val a: Int, val b: String, val c: List<Int>)

fun main() {
    println(stringify(Json0(0, "roach", listOf(1, 2, 3))))
}
