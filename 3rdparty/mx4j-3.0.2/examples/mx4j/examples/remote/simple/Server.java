/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.simple;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example shows the simplest way to setup a JSR 160 connector server.
 * It uses the standard JSR 160 RMIConnectorServer, and if you're familiar with
 * RMI, you'll know that a JNDI server like the rmiregistry is needed
 * in order to register the server stub that will be looked up by the client.
 *
 * @version $Revision: 1.3 $
 */
public class Server
{
   public static void main(String[] args) throws Exception
   {
      // The MBeanServer
      MBeanServer server = MBeanServerFactory.createMBeanServer();

      // Register and start the rmiregistry MBean, needed by JSR 160 RMIConnectorServer
      ObjectName namingName = ObjectName.getInstance("naming:type=rmiregistry");
      server.createMBean("mx4j.tools.naming.NamingService", namingName, null);
      server.invoke(namingName, "start", null, null);
      int namingPort = ((Integer)server.getAttribute(namingName, "Port")).intValue();

      String jndiPath = "/jmxconnector";
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + namingPort + jndiPath);

      // Create and start the RMIConnectorServer
      JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
      connectorServer.start();

      System.out.println("Server up and running");
   }
}
