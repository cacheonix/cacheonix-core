/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.tools.xdoclet;

/**
 * Sample MBean implementation.
 *
 * @version $Revision: 1.3 $
 * @jmx:mbean name="mx4j:name=My MBean" description="My wonderful service."
 * @jmx:mlet-entry archive="MyApp.jar" codebase="../lib"
 */
public class MyService implements MyServiceMBean
{
   protected int status = 0;
   protected String m_dummy = null;

   /**
    * Default constructor.
    *
    * @jmx:managed-constructor description="Default constructor."
    */
   public MyService()
   {
   }

   /**
    * Constructor.
    *
    * @param		type		the type,
    * @param		status	the status.
    * @jmx:managed-constructor description="Build the service."
    * @jmx:managed-constructor-parameter name="type" position="0" description="The type."
    * @jmx:managed-constructor-parameter name="status" position="1" description="The status."
    */
   public MyService(String type, int status)
   {
   }

   /**
    * Start my service.
    *
    * @jmx:managed-operation description="Starts the service."
    */
   public void start()
   {
   }

   /**
    * Method that is not an JMX managed operation.
    */
   public void stop()
   {
   }

   /**
    * Echos a string.
    *
    * @jmx:managed-operation description="Echoes the string given as a parameter."
    * @jmx:managed-operation-parameter name="str" position="0" description="The string to echo."
    */
   public void echo(String str)
   {
   }

   /**
    * Does some crazy stuff.
    *
    * @jmx:managed-operation description="Do some crazy stuff."
    * @jmx:managed-operation-parameter name="firstObject" position="0" description="My first object."
    * @jmx:managed-operation-parameter name="secondObject" position="1" description="My second object."
    */
   public int doSomeCrazyStuff(Object firstObject, Object secondObject)
   {
      return -1;
   }

   /**
    * Sets the status.
    *
    * @jmx:managed-attribute description="My Status."
    */
   public void setStatus(int status)
   {
      this.status = status;
   }

   /**
    * Gets the dummy variable.
    *
    * @jmx:managed-attribute description="My dummy attribute."
    */
   public String getDummy()
   {
      return "";
   }

   /**
    * Sets the dummy variable.
    *
    * @jmx:managed-attribute description="This description should be ignored because of the getter."
    */
   public void setDummy(String dummy)
   {
   }

}
