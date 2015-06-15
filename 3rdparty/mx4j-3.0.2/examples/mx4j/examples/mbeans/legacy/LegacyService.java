/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.legacy;

/**
 * This service wakes up every once in a while, and does an intensive job
 * spawning many threads to perform the given operation. <br>
 * We would like to be informed of this activity, and would like to expose functionality of
 * this service via JMX. To achieve these goals, we wrap it by means of a DynamicMBean,
 * {@link DynamicLegacyService}.
 *
 * @version $Revision: 1.3 $
 */
public class LegacyService
{
   private boolean running;
   private ThreadGroup group = new ThreadGroup("Legacy Thread Group");

   /**
    * This method is called 'execute', but we want to expose it in JMX with the name 'start'.
    * The magic is done in the DynamicMBean that wraps this service to expose it via JMX.
    */
   public void execute()
   {
      while (true)
      {
         // Wait for a while
         long wait = Math.round(Math.random() * 10000L) + 1;
         try
         {
            System.out.println("Waiting " + wait + " ms...");
            Thread.sleep(wait);
         }
         catch (InterruptedException ignored)
         {
         }
         // Ok, we've slept enough, time to do some job
         synchronized (this)
         {
            running = true;
         }

         Thread thread = new Thread(new Runnable()
         {
            public void run()
            {
               spawnThreads();
               // We're done now, not running anymore
               synchronized (this)
               {
                  running = false;
               }
            }
         });
         thread.start();
         try
         {
            thread.join();
         }
         catch (InterruptedException ignored)
         {
         }
      }
   }

   /**
    * This method is private in the legacy service. However, we want to expose it via JMX
    * without modifying this service. The magic is done in the DynamicMBean that wraps this
    * service to expose it via JMX.
    */
   private synchronized boolean isRunning()
   {
      return running;
   }

   private void spawnThreads()
   {
      Thread[] threads = new Thread[20];
      for (int i = 0; i < threads.length; ++i)
      {
         threads[i] = new Thread(group, new Runnable()
         {
            public void run()
            {
               // Simulate a job: sleep for a while :D
               long sleep = Math.round(Math.random() * 5000L) + 1;
               try
               {
                  Thread.sleep(sleep);
               }
               catch (InterruptedException ignored)
               {
               }
            }
         });
         threads[i].start();
      }

      // Now wait for everyone to complete:
      for (int i = 0; i < threads.length; ++i)
      {
         try
         {
            threads[i].join();
         }
         catch (InterruptedException ignored)
         {
         }
      }
   }
}
