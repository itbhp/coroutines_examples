package coroutines

import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("LogCoroutines")

fun main() {
    runBlocking {
        val a = async {
            log("I'm computing a piece of the answer")
            6
        }
        val b = async {
            log("I'm computing another piece of the answer")
            7
        }
        log("The answer is ${a.await() * b.await()}")
        
        log("My job is ${this.coroutineContext[Job]}")
    }
}

fun log(s: String) {
    logger.info(s)
}
