/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.remote.hessian;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegateMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.timer.Timer;

/**
 * This example shows how to connect to a Hessian JMXConnectorServer.
 * To run this example, you need the following jars:
 * <ul>
 * <li>MX4J 3.x</li>
 * <ul>
 * <li>mx4j.jar</li>
 * <li>mx4j-remote.jar</li>
 * <li>mx4j-tools.jar</li>
 * <li>mx4j-examples.jar</li>
 * </ul>
 * <li>Hessian 3.0.8</li>
 * <ul>
 * <li>hessian-3.0.8.jar</li>
 * </ul>
 * </ul>
 *
 * @version $Revision: 1.1 $
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      // This JMXServiceURL works only if the connector server is on the same host of
      // the connector. If this is not the case, set the correct host name.
      JMXServiceURL address = new JMXServiceURL("hessian", null, 8080, "/hessian");

      // Connect a JSR 160 JMXConnector to the server side
      JMXConnector connector = JMXConnectorFactory.connect(address);

      // Retrieve an MBeanServerConnection that represent the MBeanServer
      // the remote connector server is bound to
      MBeanServerConnection connection = connector.getMBeanServerConnection();

      // Call the server side as if it is a local MBeanServer
      ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");
      Object proxy = MBeanServerInvocationHandler.newProxyInstance(connection, delegateName, MBeanServerDelegateMBean.class, true);
      MBeanServerDelegateMBean delegate = (MBeanServerDelegateMBean)proxy;

      System.out.println(delegate.getImplementationVendor() + " is cool !");

      // Register an MBean, and get notifications via the Hessian protocol
      connection.addNotificationListener(delegateName, new NotificationListener()
      {
         public void handleNotification(Notification notification, Object handback)
         {
            System.out.println("Got the following notification: " + notification);
         }
      }, null, null);

      ObjectName timerName = ObjectName.getInstance("services:type=Timer");
      connection.createMBean(Timer.class.getName(), timerName, null);

      // Unregistering the MBean to get another notification
      connection.unregisterMBean(timerName);

      // Allow the unregistration notification to arrive before killing this JVM
      Thread.sleep(1000);

      connector.close();
   }
}
