/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.tools.persister;


/**
 * An MBean that extends FilePersister to demonstrate the usage of the mx4j.persist.FilePersister
 * @version $Revision: 1.3 $
 */

import java.io.Serializable;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;

import mx4j.persist.FilePersister;

public class MBeanOne extends FilePersister implements Serializable
{
   private String m_location;
   private String m_name;

   public MBeanOne(String location, String name) throws MBeanException
   {
      super(location, name);
      m_location = location;
      m_name = name;
   }

   // ask FilePersister to store the Object
   public void store(Object mbean) throws MBeanException, InstanceNotFoundException
   {
      store(mbean);
   }

   // return the Object
   public Object load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException
   {
      return load();
   }
}
