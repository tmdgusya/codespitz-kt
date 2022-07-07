package week6

fun main() {
    val dispatcher = FixedDispatcher(10)

    for (i in 0..5) {
        val looper = SerialTask(dispatcher, {
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        }, {
            println("$i-1 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        }, {
            println("$i-2 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        })
        looper.launch()
    }
    dispatcher.join()
}
