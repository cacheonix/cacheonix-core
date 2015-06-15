/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.remote.soap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example shows how to setup a JSR 160 connector server that uses SOAP as
 * communication protocol with the client.
 * MX4J's implementation of the SOAP provider requires Axis 1.1, that in turn requires
 * a servlet container to run. The default servlet container used is Jetty 4.2.x.
 * Incoming connections from a client will be accepted by Jetty, handed to the
 * Axis servlet that interpretes the SOAP invocation, and passed to MX4J's
 * connector server implementation, and finally routed the MBeanServer.
 * Remote notifications are delivered transparently.
 * To run this example, you need the following jars:
 * <ul>
 * <li>MX4J 2.x</li>
 * <ul>
 * <li>mx4j.jar</li>
 * <li>mx4j-remote.jar</li>
 * <li>mx4j-tools.jar</li>
 * <li>mx4j-examples.jar</li>
 * </ul>
 * <li>Jetty 4.2.x</li>
 * <ul>
 * <li>org.mortbay.jetty.jar</li>
 * <li>servlet.jar</li>
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
 * @version : $Revision: 1.6 $
 */
public class Server
{
   public static void main(String[] args) throws Exception
   {
      // The MBeanServer
      MBeanServer server = MBeanServerFactory.createMBeanServer();

      // Pass null as the host name to tell JMXServiceURL to default to InetAddress.getLocalHost().getHostName()
      JMXServiceURL url = new JMXServiceURL("soap", null, 8080, "/jmxconnector");

      // Create and start the connector server
      // Jetty will listen on port 8080
      JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
      connectorServer.start();

      System.out.println("Server up and running " + connectorServer);
   }
}
