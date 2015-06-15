/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.dynamic;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import mx4j.AbstractDynamicMBean;

/**
 * This is a DynamicMBean. Note how the usage of the {@link AbstractDynamicMBean}
 * class simplifies a lot the coding of DynamicMBeans.
 * The code itself is divided in two parts: the implementation part and the JMX part.
 *
 * @version $Revision: 1.4 $
 */
public class DynamicService extends AbstractDynamicMBean
{
   //
   // Implementation part.
   // This part gives the MBean the service functionality.
   //

   private boolean running;
   private int concurrent;

   public void start()
   {
      // Simulate the accept on incoming client requests
      // We will track how many requests we have, and if we pass a certain threshold,
      // we issue a notification.

      synchronized (this)
      {
         running = true;
      }

      Thread thread = new Thread(new Runnable()
      {
         public void run()
         {
            simulateClientRequests();
         }
      });
      thread.start();
   }

   public void stop()
   {
      synchronized (this)
      {
         running = false;
      }
   }

   private void simulateClientRequests()
   {
      while (isRunning())
      {
         // Pick a time in ms to simulate the interval between incoming client requests
         long interval = Math.round(Math.random() * 1000L) + 1;
         try
         {
            Thread.sleep(interval);
         }
         catch (InterruptedException ignored)
         {
         }

         // Spawn a new Thread to accept the client request
         Thread thread = new Thread(new Runnable()
         {
            public void run()
            {
               // Increase the number of concurrent clients
               synchronized (DynamicService.this)
               {
                  ++concurrent;
                  System.out.println("--DynamicService--" + Thread.currentThread() + "-- Incoming client request -- concurrent clients: " + concurrent);
               }

               // Pick a time in ms to simulate the processing of the client request
               long processing = Math.round(Math.random() * 5000L) + 1;
               try
               {
                  Thread.sleep(processing);
               }
               catch (InterruptedException ignored)
               {
               }

               // We're done with this client, decrease the number of concurrent clients
               synchronized (DynamicService.this)
               {
                  --concurrent;
               }
            }
         });
         thread.start();
      }
   }

   public synchronized boolean isRunning()
   {
      return running;
   }

   public synchronized int getConcurrentClients()
   {
      return concurrent;
   }


   //
   // JMX part.
   // Note how short is :)
   //

   protected MBeanAttributeInfo[] createMBeanAttributeInfo()
   {
      return new MBeanAttributeInfo[]
      {
         new MBeanAttributeInfo("Running", "boolean", "The running status of the DynamicService", true, false, true),
         new MBeanAttributeInfo("ConcurrentClients", "int", "The number of concurrent clients", true, false, false)
      };
   }

   protected MBeanOperationInfo[] createMBeanOperationInfo()
   {
      return new MBeanOperationInfo[]
      {
         new MBeanOperationInfo("start", "Starts the DynamicService", new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
         new MBeanOperationInfo("stop", "Stops the DynamicService", new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION)
      };
   }
}
