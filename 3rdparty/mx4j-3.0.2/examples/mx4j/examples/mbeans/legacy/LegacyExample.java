/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.legacy;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * This example aims to show how it is possible, with JMX, to write a non-invasive
 * wrapper for an existing legacy service in order to expose the functionality
 * of the legacy service with JMX.
 *
 * @version $Revision: 1.3 $
 */
public class LegacyExample
{
   public static void main(String[] args) throws Exception
   {
      // Create the service
      LegacyService legacyService = new LegacyService();

      // Create the JMX MBeanServer and register the service wrapper
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName serviceName = new ObjectName("examples", "mbean", "legacy");
      DynamicLegacyService dynamicService = new DynamicLegacyService(legacyService);
      server.registerMBean(dynamicService, serviceName);

      // Now register a listener: we want to be able to know when the service starts and stops
      server.addNotificationListener(serviceName, new NotificationListener()
      {
         public void handleNotification(Notification notification, Object handback)
         {
            System.out.println(notification);
         }
      }, null, null);

      // Now start the service, using the new method name: 'start' instead of 'execute'
      server.invoke(serviceName, "start", null, null);
   }

   /**
    * This is the old main routine that started the service.
    * In this example we had the possibility to modify the starter of the service
    * by renaming the main method and by writing a new one that uses JMX.
    * However, it is also possible to write another starter leaving the legacy part
    * totally unchanged.
    */
   public static void oldMain(String[] args)
   {
      LegacyService service = new LegacyService();
      service.execute();
   }
}
