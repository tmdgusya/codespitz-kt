package week6
class ContinuationTaskObjcet internal constructor(
    val run: (Continuation) -> Unit,
) {
    internal var isStarted = Stat.READY
    internal var isCompleted = Stat.READY
    internal var continuation = Continuation(this)
    internal var env: MutableMap<String, Any?> = mutableMapOf()
}
