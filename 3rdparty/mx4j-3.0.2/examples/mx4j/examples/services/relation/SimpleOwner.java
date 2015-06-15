/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.services.relation;

/**
 * @version $Revision: 1.3 $
 */
interface SimpleOwnerMBean
{
   public void setOwnerName(String ownerName);

   public String getOwnerName();
}

public class SimpleOwner implements SimpleOwnerMBean
{
   private String m_name = null;

   public SimpleOwner(String name)
   {
      m_name = name;
   }

   public void setOwnerName(String name)
   {
      m_name = name;
   }

   public String getOwnerName()
   {
      return m_name;
   }
}