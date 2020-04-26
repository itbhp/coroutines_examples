package coroutines

import kotlin.concurrent.thread

fun main() {
    thread {
        Thread.sleep(1000L)
        println("World!") // print after delay
    }
    println("Hello,") // main thread continues while coroutine is delayed
    Thread.sleep(2000L) // block main thread for 2 seconds to keep JVM alive
}
