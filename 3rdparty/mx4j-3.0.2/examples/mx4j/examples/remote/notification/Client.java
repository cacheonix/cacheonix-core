/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.notification;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.loading.MLet;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example shows how to setup a JSR 160 connector client, and how it is
 * possible to receive notifications emitted by a remote connector server.
 *
 * @version $Revision: 1.4 $
 * @see Server
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      // The address of the connector server
      JMXServiceURL url = new JMXServiceURL("rmi", "localhost", 0, "/jndi/jmx");

      // Create and connect the connector client
      JMXConnector cntor = JMXConnectorFactory.connect(url, null);

      // The connection represent, on client-side, the remote MBeanServer
      MBeanServerConnection connection = cntor.getMBeanServerConnection();

      // The listener that will receive notifications from a remote MBean
      NotificationListener listener = new NotificationListener()
      {
         public void handleNotification(Notification notification, Object handback)
         {
            System.out.println(notification);
         }
      };

      // The MBeanServerDelegate emits notifications about registration/unregistration of MBeans
      ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");

      connection.addNotificationListener(delegateName, listener, null, null);

      // Give chance to the notification machinery to setup
      Thread.sleep(1000);

      // Now register a remote MBean, for example an MLet, so that the MBeanServerDelegate
      // will emit notifications for its registration
      ObjectName name = ObjectName.getInstance("examples:mbean=mlet");
      // First notification
      connection.createMBean(MLet.class.getName(), name, null);
      // Second notification
      connection.unregisterMBean(name);
   }
}
