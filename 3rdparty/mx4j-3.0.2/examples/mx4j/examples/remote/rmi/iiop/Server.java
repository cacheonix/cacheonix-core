/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.rmi.iiop;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example shows the how to setup a JSR 160 connector server over IIOP, the CORBA
 * protocol.
 * It is very similar to the simple example also present in these examples, except
 * that it uses the IIOP protocol instead of native RMI's one, called JRMP.
 *
 * @version $Revision: 1.3 $
 */
public class Server
{
   public static void main(String[] args) throws Exception
   {
      // The MBeanServer
      MBeanServer server = MBeanServerFactory.createMBeanServer();

      // Register and start the tnameserv MBean, needed by JSR 160 RMIConnectorServer over IIOP
      // You can also start the new JDK 1.4 'orbd' daemon, but you should do so externally
      // as there are no MBeans that wrap it.
      ObjectName namingName = ObjectName.getInstance("naming:type=tnameserv");
      server.createMBean("mx4j.tools.naming.CosNamingService", namingName, null);
      // Standard port for the COS naming service is 900, but that's a restricted port on Unix/Linux systems
      int namingPort = 1199;
      server.setAttribute(namingName, new Attribute("Port", new Integer(namingPort)));
      server.invoke(namingName, "start", null, null);

      String jndiPath = "/jmxconnector";
      // Note how the JMXServiceURL specifies 'iiop' as protocol for both the
      // JMXConnectorServer (the first), to indicate the protocol of the JMXConnectorServer,
      // and for the naming server (the second), to indicate that this is not the rmiregistry
      // but the COS naming service.
      JMXServiceURL url = new JMXServiceURL("service:jmx:iiop://localhost/jndi/iiop://localhost:" + namingPort + jndiPath);

      // Create and start the RMIConnectorServer over IIOP
      JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
      connectorServer.start();

      System.out.println("Server up and running");
   }
}
