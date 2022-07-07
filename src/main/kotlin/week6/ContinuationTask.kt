package week6

class ContinuationTask(
    private val dispatcher: Dispatcher,
    isLazy: Boolean,
    block: (Continuation) -> Unit
) : EventLooper {
    private val task = ContinuationTaskObjcet(block)
    init {
        if (!isLazy) launch()
    }
    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            Thread.sleep(5)
            if (task.isCompleted == Stat.MARK) break
            if (task.isStarted == Stat.READY) {
                task.isStarted = Stat.CONFIRM
                task.run(task.continuation)
            }
            task.continuation.failed?.let { throw it }
        }
    }

    override fun launch() {
        dispatcher.start(this)
    }
}
