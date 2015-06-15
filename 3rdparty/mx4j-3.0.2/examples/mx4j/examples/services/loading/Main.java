/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.services.loading;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.ServiceNotFoundException;
import javax.management.loading.MLet;

/**
 * The starter class for loading MBeans via an MLET file. <br>
 * Modify at your wish.
 *
 * @version $Revision: 1.3 $
 */
public class Main
{
   public static void main(String[] args) throws Exception
   {
      // Create the MBeanServer
      MBeanServer server = MBeanServerFactory.createMBeanServer();

      // Register the MLet in the MBeanServer
      MLet mlet = new MLet();
      ObjectName mletName = new ObjectName("system:mbean=loader");
      server.registerMBean(mlet, mletName);

      // Set the MLet as context classloader
      // Can be useful for the loaded services that want to access this classloader.
      Thread.currentThread().setContextClassLoader(mlet);

      // Resolve the file to load MBeans from
      // If we got a program argument, we load it from there, otherwise
      // we assume we have a 'mbeans.mlet' file in this example's directory
      URL mbeansURL = null;
      if (args.length == 1)
      {
         String file = args[0];
         mbeansURL = new File(file).toURL();
      }
      else
      {
         mbeansURL = mlet.getResource("examples/services/loading/mbeans.mlet");
      }

      // If the URL is still null, abort
      if (mbeansURL == null) throw new ServiceNotFoundException("Could not find MBeans to load");

      // Load the MBeans
      Set mbeans = mlet.getMBeansFromURL(mbeansURL);

      System.out.println("MLet has now the following classpath: " + Arrays.asList(mlet.getURLs()));

      // Now let's check everything is ok.
      checkMBeansLoadedSuccessfully(mbeans);

      // Now the system is loaded, but maybe we should initialize and start them
      initializeMBeans(server, mbeans);
      startMBeans(server, mbeans);

      // Now the system is up and running
      System.out.println("System up and running !");

      // The program exits because none of the loaded MBeans in this example started a non-daemon thread.
   }

   private static void checkMBeansLoadedSuccessfully(Set mbeans) throws ServiceNotFoundException
   {
      // MLet.getMBeansFromURL returns a Set containing exceptions if an MBean could not be loaded
      boolean allLoaded = true;
      for (Iterator i = mbeans.iterator(); i.hasNext();)
      {
         Object mbean = i.next();
         if (mbean instanceof Throwable)
         {
            ((Throwable)mbean).printStackTrace();
            allLoaded = false;
            // And go on with the next
         }
         else
         {
            // Ok, the MBean was registered successfully
            System.out.println("Registered MBean: " + mbean);
         }
      }

      if (!allLoaded) throw new ServiceNotFoundException("Some MBean could not be loaded");
   }

   private static void initializeMBeans(MBeanServer server, Set mbeans)
   {
      for (Iterator i = mbeans.iterator(); i.hasNext();)
      {
         try
         {
            ObjectInstance instance = (ObjectInstance)i.next();
            if (server.isInstanceOf(instance.getObjectName(), "org.apache.avalon.framework.activity.Initializable"))
            {
               try
               {
                  server.invoke(instance.getObjectName(), "initialize", null, null);
               }
               catch (ReflectionException ignored)
               {
                  // The initialize method is not part of the management interface, ignore
               }
            }
         }
         catch (Exception x)
         {
            x.printStackTrace();
         }
      }
   }

   private static void startMBeans(MBeanServer server, Set mbeans)
   {
      for (Iterator i = mbeans.iterator(); i.hasNext();)
      {
         try
         {
            ObjectInstance instance = (ObjectInstance)i.next();
            if (server.isInstanceOf(instance.getObjectName(), "org.apache.avalon.framework.activity.Startable"))
            {
               try
               {
                  server.invoke(instance.getObjectName(), "start", null, null);
               }
               catch (ReflectionException ignored)
               {
                  // The start method is not part of the management interface, ignore
               }
            }
         }
         catch (Exception x)
         {
            x.printStackTrace();
         }
      }
   }
}
