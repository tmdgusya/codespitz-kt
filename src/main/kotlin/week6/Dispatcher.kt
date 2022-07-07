package week6

interface Dispatcher {
    fun start(looper: EventLooper)
    fun join()
}