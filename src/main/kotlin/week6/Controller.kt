package week6

class Controller internal constructor(
    private val task: Task
) {
    val data get() = task.result

    fun cancel(throwable: Throwable) {
        task.next?.result = Result.failure(throwable)
        task.isCompleted = true
    }

    fun resume(data: Any? = null) {
        task.next?.result = Result.success(data)
        task.isCompleted = true
    }
}
