package week6

import java.util.concurrent.Executors

class FixedDispatcher(
    private val threads: Int
) : Dispatcher {
    private val executor = Executors.newFixedThreadPool(threads)

    override fun start(looper: EventLooper) {
        executor.execute(looper)
    }

    override fun join() {
        while (!executor.isShutdown) {}
    }
}
