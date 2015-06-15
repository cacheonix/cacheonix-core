/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.remote.soap;

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
 * This example shows how to connect to a JMXConnectorServer over the SOAP protocol.
 * MX4J's implementation of the SOAP provider requires Axis 1.1, that in turn requires
 * a servlet container to run. The default servlet container used is Jetty 4.2.x.
 * To run this example, you need the following jars:
 * <ul>
 * <li>MX4J 2.x</li>
 * <ul>
 * <li>mx4j.jar</li>
 * <li>mx4j-remote.jar</li>
 * <li>mx4j-tools.jar</li>
 * <li>mx4j-examples.jar</li>
 * </ul>
 * <li>Axis 1.1</li>
 * <ul>
 * <li>axis.jar</li>
 * <li>jaxrpc.jar</li>
 * <li>commons-logging.jar</li>
 * <li>commons-discovery.jar</li>
 * <li>saaj.jar</li>
 * <li>wsdl4j.jar</li>
 * </ul>
 * </ul>
 *
 * @version $Revision: 1.4 $
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      // This JMXServiceURL works only if the connector server is in-VM with
      // the connector. If this is not the case, set the correct host name.
      JMXServiceURL address = new JMXServiceURL("soap", null, 8080, "/jmxconnector");

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

      // Register an MBean, and get notifications via the SOAP protocol
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
