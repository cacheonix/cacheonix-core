/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.legacy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import mx4j.AbstractDynamicMBean;

/**
 * The wrapper DynamicMBean for exposing the legacy service in a non-invasive way. <br>
 * This MBean emits notifications when the legacy service starts its activity and when it stops it.
 * Furthermore, when the legacy service is running, it displays the number of threads that the
 * legacy service is using to perform its activity. <br>
 * Note how the {@link LegacyService} is completely unaware of JMX, and even if it has private fields
 * and methods (the legacy service was designed without knowledge of JMX), it is possible to
 * expose them (via reflection) in JMX. <br>
 * This MBean is divided in two parts, the implementation one and the JMX one. Note how the JMX
 * part, thanks to {@link AbstractDynamicMBean}, is very simple even if it is a DynamicMBean.
 *
 * @version $Revision: 1.4 $
 */
public class DynamicLegacyService extends AbstractDynamicMBean implements NotificationBroadcaster
{
   //
   // Implementation Part
   //

   private LegacyService service;
   private Thread statusThread;

   public DynamicLegacyService(LegacyService service)
   {
      this.service = service;

      statusThread = new Thread(new Runnable()
      {
         public void run()
         {
            monitorStatus();
         }
      });
   }

   /**
    * Starts monitoring the legacy service, and starts as well the legacy service
    *
    * @see #isRunning
    */
   public void start()
   {
      // Start the thread that monitors the status of the service
      statusThread.start();

      // We remap the 'start' method as defined by JMX to the 'execute' method of the legacy service
      service.execute();
   }

   /**
    * Returns whether the legacy service has woken up and it is running or not.
    */
   public boolean isRunning()
   {
      // The method 'isRunning' is private in the legacy service, so here we use reflection tricks
      try
      {
         Class cls = service.getClass();
         Method method = cls.getDeclaredMethod("isRunning", new Class[0]);
         method.setAccessible(true);
         Boolean result = (Boolean)method.invoke(service, new Object[0]);
         return result.booleanValue();
      }
      catch (Exception ignored)
      {
         ignored.printStackTrace();
         return false;
      }
   }

   /**
    * Returns the number of threads that the legacy service is using to perform its job when it
    * wakes up.
    */
   public int getThreadCount()
   {
      // There is no a direct mapping of the thread count in the legacy service
      // We use again reflection tricks, calling LegacyService.group.activeCount()
      try
      {
         Class cls = service.getClass();
         Field field = cls.getDeclaredField("group");
         field.setAccessible(true);
         ThreadGroup group = (ThreadGroup)field.get(service);
         return group.activeCount();
      }
      catch (Exception ignored)
      {
         ignored.printStackTrace();
         return 0;
      }
   }

   /**
    * Monitors the status of the legacy service, every 50 ms, to see if it has woken up
    * and it is running. <br>
    * When the legacy service starts and stops its job, a notification is emitted.
    */
   private void monitorStatus()
   {
      boolean wasRunning = false;
      while (true)
      {
         boolean isRunning = isRunning();
         if (wasRunning ^ isRunning)
         {
            Notification notification = new Notification("legacy.status.running." + isRunning, this, 0, "Legacy Service Status: " + isRunning);
            broadcaster.sendNotification(notification);
            wasRunning = isRunning;
         }
         else
         {
            if (isRunning) System.out.println("Threads: " + getThreadCount());
         }

         // Monitor every 50 ms
         try
         {
            Thread.sleep(50);
         }
         catch (InterruptedException ignored)
         {
         }
      }
   }

   //
   // JMX Part
   //

   private NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

   protected MBeanAttributeInfo[] createMBeanAttributeInfo()
   {
      return new MBeanAttributeInfo[]
      {
         new MBeanAttributeInfo("Running", "boolean", "The running status of the Legacy Service", true, false, true),
         new MBeanAttributeInfo("ThreadCount", "int", "The number of running threads", true, false, false)
      };
   }

   protected MBeanOperationInfo[] createMBeanOperationInfo()
   {
      return new MBeanOperationInfo[]
      {
         new MBeanOperationInfo("start", "Start the Legacy Service", new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION)
      };
   }

   protected MBeanNotificationInfo[] createMBeanNotificationInfo()
   {
      return getNotificationInfo();
   }

   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
   {
      broadcaster.addNotificationListener(listener, filter, handback);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return new MBeanNotificationInfo[]
      {
         new MBeanNotificationInfo
                 (new String[]{"legacy.status.running.true", "legacy.status.running.false"},
                  Notification.class.getName(),
                  "Notifications on the status of the Legacy Service")
      };
   }

   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      broadcaster.removeNotificationListener(listener);
   }
}
