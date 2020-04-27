package coroutines

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors.newSingleThreadExecutor
import java.util.concurrent.ThreadFactory

private fun threadFactory(n: String): ThreadFactory = ThreadFactory { r -> Thread(r, n) }

fun main() = runBlocking {
    threadDispatcher("Ctx1").use { ctx1 ->
        threadDispatcher("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                log("Started in ctx1")
                withContext(ctx2) {
                    log("Working in ctx2")
                }
                log("Back to ctx1")
            }
        }
    }
}

private fun threadDispatcher(name: String) =
    newSingleThreadExecutor(threadFactory(name)).asCoroutineDispatcher()
