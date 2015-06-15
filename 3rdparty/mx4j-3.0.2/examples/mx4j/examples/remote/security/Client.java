/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXPrincipal;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

/**
 * This example shows how to setup a JSR 160 connector client that connects to
 * a secured JSR 160 connector server, and that uses the subject delegation features
 * defined by JSR 160.
 * Refer to the MX4J documentation on how to run this example and on how it
 * works: this example is described in details.
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

      // The credentials are passed via the environment Map
      Map environment = new HashMap();
      String[] credentials = new String[]{"guest", "guest"};
      environment.put(JMXConnector.CREDENTIALS, credentials);

      // Connect to the server
      JMXConnector cntor = JMXConnectorFactory.connect(url, environment);

      // Create a subject to delegate to
      JMXPrincipal principal = new JMXPrincipal("anotherGuest");
      Set principals = new HashSet();
      principals.add(principal);
      Subject delegate = new Subject(true, principals, Collections.EMPTY_SET, Collections.EMPTY_SET);

      // Get two MBeanServerConnection: one that uses the 'guest' principal directly,
      // the second that uses the 'guest' user but delegates to another principal.
      MBeanServerConnection connection = cntor.getMBeanServerConnection();
      MBeanServerConnection delegateConnection = cntor.getMBeanServerConnection(delegate);

      // The example policy file provided allows both MBeanServerConnections to call
      // MBeanServerConnection.queryNames
      Set mbeans = connection.queryNames(null, null);
      System.out.println("MBeans retrieved by a connection without delegate subject:");
      System.out.println(mbeans);
      System.out.println();

      mbeans = delegateConnection.queryNames(null, null);
      System.out.println("MBeans retrieved by a connection with a delegate subject:");
      System.out.println(mbeans);
      System.out.println();

      // The example policy file forbids to call MBeanServerConnection.getObjectInstance
      try
      {
         connection.getObjectInstance(ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate"));
         throw new Error();
      }
      catch (SecurityException x)
      {
         System.out.println("No permission to call getObjectInstance for the MBeanServerDelegate");
      }
   }
}
