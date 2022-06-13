package parser

import kotlin.reflect.KProperty

fun <T : Any> stringify(target: T): String {
    return jsonObject(target)
}

fun <T : Any> jsonObject(target: T): String {
    val builder = buildString {
        target::class.members
            .filterIsInstance<KProperty<*>>()
            .joinTo(buffer = this, separator = ", ", prefix = "{", postfix = "}") {
                val value = it.getter.call(target) // call 은 java reflection 의 invoke 를 생각하면 됨.
                "${jsonString(it.name)} : ${when (value) {
                    null -> "null"
                    is String -> jsonString(value)
                    is Boolean, is Number -> value.toString()
                    is List<*> -> jsonList(value)
                    else -> jsonObject(value)
                }}"
            }
    }

    return builder
}

fun jsonList(target: List<*>): String {
    return ""
}

private fun jsonString(v: String) = """"${v.replace("\"", "\\\"")}""""

class Json0(val a: Int, val b: String)

fun main() {
    println(stringify(Json0(0, "roach")))
}
