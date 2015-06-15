/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.notification;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import mx4j.tools.naming.NamingService;

/**
 * This example shows how to setup a JSR 160 connector server.
 * The client counterpart of this example will register a remote NotificationListener
 * and receive notifications over the wire.
 * Nothing special is needed in the server side, if not registering an MBean
 * that implements {@link javax.management.NotificationEmitter}.
 * Every JMX implementation already has such an MBean registered, the MBeanServerDelegate.
 * The client will register a NotificationListener to the MBeanServerDelegate MBean,
 * that emits notifications when other MBeans are registered or unregistered.
 *
 * @version $Revision: 1.4 $
 * @see Client
 */
public class Server
{
   public static void main(String[] args) throws Exception
   {
      // The address of the connector server
      JMXServiceURL url = new JMXServiceURL("rmi", "localhost", 0, "/jndi/jmx");

      // No need of environment variables or the MBeanServer at this point
      JMXConnectorServer cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, null);
      ObjectName cntorServerName = ObjectName.getInstance(":service=" + JMXConnectorServer.class.getName() + ",protocol=" + url.getProtocol());

      MBeanServer server = MBeanServerFactory.createMBeanServer("remote.notification.example");
      // Register the connector server as MBean
      server.registerMBean(cntorServer, cntorServerName);

      // The rmiregistry needed to bind the RMI stub
      NamingService naming = new NamingService();
      ObjectName namingName = ObjectName.getInstance(":service=" + NamingService.class.getName());
      server.registerMBean(naming, namingName);
      naming.start();

      // Start the connector server
      cntorServer.start();

      System.out.println("Server up and running");
   }
}
