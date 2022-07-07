package week6

import java.util.LinkedList
import java.util.Queue

class EventLooper(
    private val dispatcher: Dispatcher
) : Runnable {
    private val tasks: Queue<Task> = LinkedList()
    private var currTask: Task? = null

    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            Thread.sleep(16)
            synchronized(this) {
                if (currTask != null) {
                    currTask?.let { curr ->
                        if (curr.isCompleted) {
                            curr.next?.let { tasks.add(it) }
                            currTask = null
                        }
                    }
                } else {
                    tasks.poll()?.let {
                        currTask = it
                        it.run(Controller(it))
                    }
                }
            }
        }
    }

    fun linkedTask(vararg blocks: (Controller) -> Unit) {
        if (blocks.isEmpty()) return
        synchronized(tasks) { // multi-Thread 에서 실행될 수 있으므로.
            var prev = Task(blocks[0])
            tasks.add(prev) // Add Task to TaskQueue
            for (i in 1..blocks.lastIndex) { // Linked Task
                val task = Task(blocks[i])
                prev.next = task
                prev = task
            }
        }
    }

    /**
     * Thread Pool 로 Run 시작.
     */
    fun launch() {
        dispatcher.start(this)
    }

    fun join() {
        dispatcher.join()
    }
}
