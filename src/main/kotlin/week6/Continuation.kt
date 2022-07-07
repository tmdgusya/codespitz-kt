package week6

class Continuation internal constructor(
    private val task: ContinuationTaskObjcet,
) {
    var step = 0
        private set

    operator fun get(key: String): Any? = task.env[key]
    operator fun set(key: String, value: Any?) { task.env[key] = value }
    internal var failed: Throwable? = null

    fun cancel(throwable: Throwable) {
        failed = Throwable("step: $step, env: ${task.env}", throwable)
        task.isCompleted = Stat.MARK
    }

    fun complete() {
        task.isCompleted = Stat.MARK
    }

    fun resume(step: Int) {
        this.step = step
        task.isStarted = Stat.READY
    }
}
