/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.tools.persister;


import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.loading.MLet;


/**
 * Our agent which instantiates 2 MLets to load the MBeans contained in their separate jars
 *
 * @version $Revision: 1.3 $
 */
public class PersisterTest
{
   private MBeanServer m_server = null;

   public PersisterTest()
   {

   }

   public void endExample()
   {
      System.out.println("----- example completed -----");
      MBeanServerFactory.releaseMBeanServer(m_server);
      System.exit(0);
   }

   public void doDemo(String[] args)
   {
      String jarPath1 = args[0];
      String jarPath2 = args[1];
      String storePath = args[2];
      String filename = args[3];

      m_server = MBeanServerFactory.createMBeanServer("test");
      try
      {
         // register the mlet used to load the MBeans MLet one
         ObjectName mName1 = new ObjectName("loading:test=mlet1");
         MLet mlet1 = new MLet();
         m_server.registerMBean(mlet1, mName1);
         mlet1.addURL(jarPath1);
//			mlet1.addURL(new File("one.jar").toURL());

         ObjectName mName2 = new ObjectName("loading:test=mlet2");
         MLet mlet2 = new MLet();
         m_server.registerMBean(mlet2, mName2);
         mlet2.addURL(jarPath2);
//			mlet2.addURL(new File("two.jar").toURL());

         String mbeanClass1 = "mx4j.examples.tools.persister.MBeanOne";
         ObjectName mbeanName1 = new ObjectName("test:name=MBeanOne");
         m_server.createMBean(mbeanClass1, mbeanName1, mName1,
                              new Object[]{storePath, filename}, new String[]{"java.lang.String", "java.lang.String"});

         String mbeanClass2 = "mx4j.examples.tools.persister.MBeanTwo";
         ObjectName mbeanName2 = new ObjectName("test:name=MBeanTwo");
         m_server.createMBean(mbeanClass2, mbeanName2, mName2, new Object[]{new Integer(15)},
                              new String[]{"java.lang.Integer"});

         m_server.invoke(mbeanName2, "storeIt", new Object[]{m_server, mbeanName1},
                         new String[]{"javax.management.MBeanServer", "javax.management.ObjectName"});

         Object a = m_server.invoke(mbeanName2, "loadIt", new Object[]{m_server, mbeanName1},
                                    new String[]{"javax.management.MBeanServer", "javax.management.ObjectName"});

         if (a.getClass().getName() == mbeanClass2) System.out.println("Objects are equal and the same");
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      finally
      {
         endExample();
      }
   }

   public static void usage()
   {
      System.out.println("Four arguments are needed to run this example:");
      System.out.println("arg[0] = <path to jar containing MBeanOne> eg: file:C:/dev/one.jar");
      System.out.println("arg[1] = <path to jar containing MBeanTwo> eg: file:C:/dev/two.jar");
      System.out.println("arg[2] = <path store file> eg: C:/dev");
      System.out.println("arg[3] = <name of file> eg: myMBean.ser");

      System.out.println("Program is exiting.......");
      System.exit(1);
   }

   public static void main(String[] args)
   {
      PersisterTest test = new PersisterTest();
      if (args.length < 4)
      {
         usage();
      }

      test.doDemo(args);
   }
}
