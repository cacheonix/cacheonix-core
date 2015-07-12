package org.cacheonix.impl.net.processor;

import org.cacheonix.ShutdownException;
import org.cacheonix.impl.util.Shutdownable;

/**
 * A {@link Command} processor.
 */
public interface Processor extends Shutdownable {

   /**
    * Enqueues the command for execution by this processor.
    *
    * @param command the command to enqueue.
    * @throws InterruptedException if the caller thread has been interrupted.
    * @throws ShutdownException    if the processor is shutdown.
    */
   void enqueue(Command command) throws InterruptedException, ShutdownException;

   /**
    * Starts
    */
   void startup();

   /**
    * Shuts down this processor by sending interrupt to its worker thread. This method must be called before discarding
    * this instance of the Processor. Failure to call shutdown may lead to resources such as open files left hanging
    * around.
    */
   void shutdown();

   /**
    * Returns <code>true</code> if the current thread is a processor thread. Returns <code>false</code> if the current
    * thread is not a processor thread.
    *
    * @return <code>true</code> if the current thread is a processor thread. <code>false</code> if the current thread is
    *         not a processor thread.
    */
   boolean isProcessorThread();

   /**
    * Returns <code>true</code> if the connection was closed.
    *
    * @return <code>true</code> if the connection was closed.
    */
   boolean isShutdown();

   /**
    * Returns <code>true</code> if the processor is alive and ready to process commands. Returns <code>false</code> if
    * the processor has not been started yet or if the processor has been shutdown.
    *
    * @return <code>true</code> if the processor is alive and ready to process commands. Returns <code>false</code> if
    *         the processor has not been started yet or if the processor has been shutdown.
    */
   boolean isAlive();

   /**
    * Waits for this processor to shutdown.
    *
    * @param timeoutMillis time to wait.
    * @return <code>true</code> if the processor was shutdown when this method returned control to the caller.
    */
   boolean waitForShutdown(long timeoutMillis);
}
