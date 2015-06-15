/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import mx4j.tools.naming.NamingService;
import mx4j.tools.remote.PasswordAuthenticator;

/**
 * This example shows how to setup a JSR 160 connector server that runs under
 * security manager and that forbids access to non-authorized users.
 * Refer to the MX4J documentation on how to run this example and on how to tune
 * the policy files: this example is described in details.
 *
 * @version $Revision: 1.5 $
 * @see Client
 */
public class Server
{
   private static final String PASSWORD_FILE = "users.properties";

   public static void main(String[] args) throws Exception
   {
      prepareUsersFile();

      // The address of the connector server
      JMXServiceURL url = new JMXServiceURL("rmi", "localhost", 0, "/jndi/jmx");

      // Specify the authenticator in the environment Map, using the
      // standard property JMXConnector.AUTHENTICATOR
      Map environment = new HashMap();
      JMXAuthenticator authenticator = new PasswordAuthenticator(new File(PASSWORD_FILE));
      environment.put(JMXConnectorServer.AUTHENTICATOR, authenticator);

      // Create and register the connector server
      JMXConnectorServer cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, null);
      ObjectName cntorServerName = ObjectName.getInstance(":service=" + JMXConnectorServer.class.getName() + ",protocol=" + url.getProtocol());
      MBeanServer server = MBeanServerFactory.createMBeanServer("remote.security.example");
      server.registerMBean(cntorServer, cntorServerName);

      // Setup the rmiregistry to bind in JNDI the RMIConnectorServer stub.
      NamingService naming = new NamingService();
      ObjectName namingName = ObjectName.getInstance(":service=" + NamingService.class.getName());
      server.registerMBean(naming, namingName);
      naming.start();

      // Start the connector server
      cntorServer.start();

      System.out.println("Server up and running");
   }

   /**
    * Writes a user/password file in the filesystem, with two hardcoded users:
    * 'admin' and 'guest'.
    * Normally this file is provided externally, not created by a program.
    * Purpose of this method is to show how to obfuscate passwords using
    * {@link PasswordAuthenticator}.
    */
   private static void prepareUsersFile() throws IOException
   {
      Properties properties = new Properties();

      String user = "admin";
      String password = PasswordAuthenticator.obfuscatePassword("admin");
      properties.setProperty(user, password);

      user = "guest";
      password = PasswordAuthenticator.obfuscatePassword("guest");
      properties.setProperty(user, password);

      FileOutputStream fos = new FileOutputStream(new File(PASSWORD_FILE));
      properties.store(fos, null);
   }
}
