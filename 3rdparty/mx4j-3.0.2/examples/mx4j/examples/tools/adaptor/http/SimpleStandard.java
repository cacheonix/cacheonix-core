/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.adaptor.http;

/**
 * @version $Revision: 1.3 $
 */
interface SimpleStandardMBean
{
   public void setName(String name);

   public String getName();
}

public class SimpleStandard implements SimpleStandardMBean
{
   private String m_name = "RelationAdaptor Example";

   public SimpleStandard()
   {
   }

   public void setName(String name)
   {
      m_name = name;
   }

   public String getName()
   {
      return m_name;
   }
}
