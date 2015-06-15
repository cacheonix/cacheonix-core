/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.interception;

import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example shows how to setup a JSR 160 connector client that connects to
 * a JSR 160 connector server that intercepts calls directed to it.
 *
 * @version $Revision: 1.3 $
 * @see Server
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      // The address of the connector server
      JMXServiceURL url = new JMXServiceURL("rmi", "localhost", 0, "/jndi/jmx");

      // The credentials are passed via the environment Map
      Map environment = new HashMap();
      String[] credentials = new String[]{"guest", "guest"};
      environment.put(JMXConnector.CREDENTIALS, credentials);

      // Connect to the server
      JMXConnector cntor = JMXConnectorFactory.connect(url, environment);

      MBeanServerConnection connection = cntor.getMBeanServerConnection();

      // On the server's console, this call will be intercepted
      String domain = connection.getDefaultDomain();
      System.out.println("Default domain = " + domain);
   }
}
