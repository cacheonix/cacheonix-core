/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.tools.persister;

import java.io.Serializable;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * The MBean that gets to be stored Please note must be Serializable
 *
 * @version $Revision: 1.3 $
 */
interface MBeanTwoMBean
{
   public void storeIt(MBeanServer server, ObjectName name);

   public Object loadIt(MBeanServer server, ObjectName name);
}

public class MBeanTwo implements MBeanTwoMBean, Serializable
{
   private Integer number = null;

   public MBeanTwo(Integer amount)
   {
      number = amount;
   }

   public void storeIt(MBeanServer server, ObjectName name)
   {
      try
      {
         server.invoke(name, "store", new Object[]{this}, new String[]{"java.lang.Object"});
      }
      catch (Exception ex)
      {
         System.out.println("exception: MBeanTwo: storeIt");
         ex.printStackTrace();
      }
   }

   public Object loadIt(MBeanServer server, ObjectName name)
   {
      Object me = null;
      try
      {
         me = (MBeanTwo)server.invoke(name, "load", new Object[0], new String[0]);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return me;
   }
}
