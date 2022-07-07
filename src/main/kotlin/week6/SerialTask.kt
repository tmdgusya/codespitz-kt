package week6

class SerialTask(
    private val dispatcher: Dispatcher,
    vararg blocks: (Controller) -> Unit
) : EventLooper {
    private var task: Task

    /**
     * 생성자에서 Block 으로 Task 를 구성해버림. linkedTask 가 오직 SerialTask 가 실행되는 시점에만 사용됨.
     */
    init {
        if (blocks.isEmpty()) throw Throwable("no blocks")
        var prev = Task(blocks[0])
        task = prev
        prev.isStarted = Stat.MARK
        for (i in 1..blocks.lastIndex) {
            val task = Task(blocks[i])
            prev.next = task
            prev = task
        }
    }

    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            Thread.sleep(5)
            if (task.isCompleted == Stat.MARK) {
                task.next?.let {
                    it.isStarted = Stat.MARK
                    task = it
                }
            }
            if (task.isStarted == Stat.MARK) {
                task.run(Controller(task))
                task.isStarted = Stat.CONFIRM
            }
        }
    }

    override fun launch() {
        dispatcher.start(this)
    }
}
