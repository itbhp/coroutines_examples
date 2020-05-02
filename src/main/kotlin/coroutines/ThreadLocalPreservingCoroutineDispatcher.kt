package coroutines

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class ThreadLocalPreservingCoroutineDispatcher(
    private val context: CoroutineContext,
    private val wrapperFactory: ThreadLocalWrapperFactory
) : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        val next = context[ContinuationInterceptor]
        val wrapped = wrapperFactory.newThreadLocalWrapper(continuation)
        
        return when (next) {
            null -> wrapped
            else -> next.interceptContinuation(wrapped)
        }
    }
}

interface ThreadLocalWrapperFactory{
    fun <T> newThreadLocalWrapper(continuation: Continuation<T>): ThreadLocalWrapper<T>
}

abstract class ThreadLocalWrapper<T>(
    private val continuation: Continuation<T>
) : Continuation<T> {
    override val context: CoroutineContext
        get() = continuation.context
    
    override fun resumeWith(result: Result<T>) {
        try {
            propagateThreadLocal()
            continuation.resumeWith(result)
        } finally {
            resetThreadLocal()
        }
    }
    
    abstract fun resetThreadLocal()
    
    abstract fun propagateThreadLocal()
    
}
