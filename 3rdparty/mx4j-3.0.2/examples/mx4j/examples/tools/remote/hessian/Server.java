/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.remote.hessian;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example shows how to setup a JSR 160 connector server that uses
 * Caucho's Hessian protocol.
 * <br />
 * MX4J's implementation requires the hessian library version 3.0.8 and
 * a servlet container to run. The default servlet container used is Jetty.
 * To run this example, you need the following jars:
 * <ul>
 * <li>MX4J</li>
 * <ul>
 * <li>mx4j.jar</li>
 * <li>mx4j-remote.jar</li>
 * <li>mx4j-tools.jar</li>
 * <li>mx4j-examples.jar</li>
 * </ul>
 * <li>Jetty 4.2.x or later</li>
 * <ul>
 * <li>org.mortbay.jetty.jar</li>
 * <li>servlet.jar</li>
 * <li>commons-logging.jar</li>
 * </ul>
 * <li>Hessian 3.0.8</li>
 * <ul>
 * <li>hessian-3.0.8.jar</li>
 * </ul>
 * </ul>
 *
 * @version : $Revision: 1.1 $
 */
public class Server
{
   public static void main(String[] args) throws Exception
   {
      // The MBeanServer
      MBeanServer server = MBeanServerFactory.createMBeanServer();

      // Pass null as the host name to tell JMXServiceURL to default to InetAddress.getLocalHost().getHostName()
      JMXServiceURL url = new JMXServiceURL("hessian", null, 8080, "/hessian");

      JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
      connectorServer.start();

      System.out.println("Server up and running " + connectorServer + " on " + url);
   }
}
