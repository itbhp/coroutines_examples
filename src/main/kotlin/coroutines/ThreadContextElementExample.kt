package coroutines

import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.coroutines.CoroutineContext

fun main() {
    val tomcatExecutor = Executors.newFixedThreadPool(10, MyThreadFactory.threadFactory("tomcat"))
    val appExecutor = Executors.newFixedThreadPool(10, MyThreadFactory.threadFactory("app"))
    
    (1..100).map { i ->
        tomcatExecutor.submit {
            SessionHolder.set(Session("session $i from ${Thread.currentThread().name}"))
            appLogic(appExecutor.asCoroutineDispatcher() + SessionThreadContextElement(SessionHolder.get()!!))
        }
    }.forEach { future: Future<*> -> future.get() }
    
    tomcatExecutor.shutdown()
    appExecutor.shutdown()
}

private fun appLogic(coroutineContext: CoroutineContext) {
    runBlocking(coroutineContext) {
        log("retrieving session ${SessionHolder.get()}")
    }
}

class SessionThreadContextElement(val session: Session): ThreadContextElement<Session> {
    companion object Key : CoroutineContext.Key<SessionThreadContextElement>
    
    override val key: CoroutineContext.Key<SessionThreadContextElement>
        get() = Key
    
    override fun restoreThreadContext(context: CoroutineContext, oldState: Session) {
        SessionHolder.set(oldState)
    }
    
    override fun updateThreadContext(context: CoroutineContext): Session {
        SessionHolder.set(session)
        return session
    }
}
