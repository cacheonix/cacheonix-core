/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.mbeans.iiop;

import java.rmi.RemoteException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * The Hello service implementation. <br />
 * It exposes two interfaces: the RMI Remote interface, invocable from remote clients -
 * represented by the {@link Hello} interface, and
 * the management interface - represented by the {@link HelloImplMBean} interface,
 * invocable from management applications that wants to manage the features of this
 * service.
 *
 * @version $Revision: 1.4 $
 */
public class HelloImpl implements Hello, HelloImplMBean
{
   private boolean m_isRunning;

   public HelloImpl() throws RemoteException
   {
   }

   public void sayHello(String name) throws RemoteException
   {
      String hello = "Hello";
      System.out.println(hello + " " + name);
   }

   public void start() throws Exception
   {
      if (!m_isRunning)
      {
         // export the remote object
         PortableRemoteObject.exportObject(this);
         // set up the initialContext
         Context ctx = new InitialContext();
         ctx.rebind(IIOP_JNDI_NAME, this);
         System.out.println("My Service servant started successfully");
         m_isRunning = true;
      }
   }

   public void stop() throws Exception
   {
      if (m_isRunning)
      {
         PortableRemoteObject.unexportObject(this);
         Context ctx = new InitialContext();
         ctx.unbind(IIOP_JNDI_NAME);
         m_isRunning = false;
         System.out.println("My Service Servant stopped successfully");
      }
   }

   public boolean isRunning()
   {
      return m_isRunning;
   }
}
