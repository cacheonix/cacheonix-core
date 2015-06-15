/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.helloworld;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Service class that reads a configuration file and returns information about the configuration. <p>
 * Its purpose is to show the difference between management methods and service methods. <br>
 * This class has 3 methods, but only 2 belong to the management interface, therefore only these 2
 * are accessible from the MBeanServer, so they're the management methods. <br>
 * The third method can be used by any other class but it is not accessible from the MBeanServer so
 * it's a service method (since it gives a service to callers), and not a management method.
 *
 * @version $Revision: 1.3 $
 */
public class HelloWorld implements HelloWorldMBean
{
   private int m_times;
   private Properties m_configuration;

   public String getInfoFromConfiguration(String key)
   {
      // Be sure to use the configuration while it is not changed.
      synchronized (this)
      {
         return m_configuration.getProperty(key);
      }
   }

   public void reloadConfiguration() throws IOException
   {
      // Lookup the configuration file in the classpath
      String configuration = "jndi.properties";
      InputStream is = getClass().getClassLoader().getResourceAsStream(configuration);
      if (is == null)
      {
         throw new FileNotFoundException("Cannot find " + configuration + " file in classpath");
      }

      // Load the new configuration from the file
      Properties p = new Properties();
      p.load(is);

      // Avoid that someone reads the configuration while we are changing it
      synchronized (this)
      {
         m_configuration = p;
         ++m_times;
      }
   }

   public int getHowManyTimes()
   {
      return m_times;
   }
}
