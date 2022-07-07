package week6

fun main() {
    val looper = EventLooper(FixedDispatcher(10))

    for (i in 0..5) {
        looper.linkedTask({
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        }, {
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        }, {
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        })
    }

    looper.launch()
    looper.join()
}

/**
 * Executed Result
0-0 24
1-0 18
2-0 23
3-0 21
4-0 16
5-0 17
0-0 22
1-0 18
2-0 24
3-0 18
4-0 22
5-0 23
0-0 15
1-0 17
2-0 18
3-0 20
4-0 19
5-0 22
 */