/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.remote.hessian.ssl;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import mx4j.tools.remote.http.HTTPConnectorServer;

/**
 * This example shows how to setup a JSR 160 connector server that uses Caucho's Hessian
 * protocol over HTTPS as communication protocol with the client.
 * <br />
 * MX4J's implementation requires the hessian library version 3.0.8 and
 * a servlet container to run. The default servlet container used is Jetty.
 * To run this example, you need the following jars:
 * <ul>
 * <li>MX4J 3.x</li>
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
 * Furthermore, you need a Jetty configuration file and a keystore
 * (see the MX4J documentation on how to create these 2 files).
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
      JMXServiceURL url = new JMXServiceURL("hessian+ssl", null, 8443, "/hessianssl");

      // Replace the value of the configuration with the file path of the configuration file
      Map serverEnv = new HashMap();
      serverEnv.put(HTTPConnectorServer.WEB_CONTAINER_CONFIGURATION, "<your-web-container-configuration>");
      JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, serverEnv, server);
      connectorServer.start();

      System.out.println("Server up and running " + connectorServer + " on " + url);
   }
}
