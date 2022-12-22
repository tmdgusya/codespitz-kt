package calculator

val trim = """[^.\d-+*/]""".toRegex() // white list 만 trim
val groupMD = """((?:\+|\+-)?[.\d]+)([*/])((?:\+|\+-)?[.\d]+)""".toRegex()

fun trim(v: String): String {
    return v.replace(trim, "")
}

fun repMtoPM(v: String): String = v.replace("-", "+-") // kotlin replace -> replaceAll 과 동일

fun foldGroup(v: String): Double = groupMD
    .findAll(v)
    .fold(0.0) { acc, curr ->
        val (_, left, op, right) = curr.groupValues // capture 된 group 의 배열이 담겨있음.
        acc + (left.replace("+", "").toDouble() to right.replace("+", "").toDouble()).let {
            when (op) {
                "*" -> it.first * it.second
                "/" -> it.first / it.second
                else -> throw Throwable("Invalid operator $op")
            }
        }
    }

fun calc(v: String) = foldGroup(repMtoPM(trim(v)))
