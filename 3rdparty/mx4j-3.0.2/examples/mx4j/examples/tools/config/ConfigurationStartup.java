/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import mx4j.tools.config.ConfigurationLoader;

/**
 * This example shows how to use the XML configuration files to load MBeans into
 * an MBeanServer. <br />
 * The main class is {@link ConfigurationLoader}, that is able to read the XML
 * configuration format defined by the MX4J project (see the online documentation
 * for details on the format).
 * A <code>ConfigurationLoader</code> is an MBean itself, and loads information
 * from one XML file into one MBeanServer. <br />
 * This example runs by specifying the path of an XML configuration file as a
 * program argument, such as
 * <pre>
 * java -classpath ... mx4j.examples.tools.config.ConfigurationStartup ./config.xml
 * </pre>
 * Refer to the documentation about the ConfigurationLoader for further information.
 *
 * @version $Revision: 1.3 $
 * @see ConfigurationShutdown
 */
public class ConfigurationStartup
{
   public static void main(String[] args) throws Exception
   {
      // The MBeanServer
      MBeanServer server = MBeanServerFactory.newMBeanServer();

      // The configuration loader

      /* Choice 1: as an external object */
      // ConfigurationLoader loader = new ConfigurationLoader(server);

      /* Choice 2: as a created MBean */
      // server.createMBean(ConfigurationLoader.class.getName(), ObjectName.getInstance("config:service=loader"), null);

      /* Choice 3: as a registered MBean */
      ConfigurationLoader loader = new ConfigurationLoader();
      server.registerMBean(loader, ObjectName.getInstance("config:service=loader"));

      // The XML file

      /* Choice 1: read it from classpath using classloaders
         Note: the directory that contains the XML file must be in the classpath */
      // InputStream stream = ConfigurationStartup.class.getClassLoader().getResourceAsStream("config.xml");
      // Reader reader = new BufferedReader(new InputStreamReader(stream));

      /* Choice 2: read it from a file
         Note: requires file path to be passed as program argument */
      String path = args[0];
      Reader reader = new BufferedReader(new FileReader(path));

      // Read and execute the 'startup' section of the XML file
      loader.startup(reader);

      reader.close();

      System.out.println("Application configured successfully");
   }
}
