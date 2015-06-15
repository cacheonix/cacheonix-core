/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.rmi;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import javax.naming.InitialContext;

/**
 * The service implementation. <br />
 * It exposes two interfaces: the RMI Remote interface, invocable from remote clients -
 * represented by the {@link MyRemoteService} interface, and
 * the management interface - represented by the {@link MyRemoteServiceObjectMBean} interface,
 * invocable from management applications that wants to manage the features of this
 * service.
 *
 * @version $Revision: 1.4 $
 */
public class MyRemoteServiceObject extends RemoteServer implements MyRemoteService, MyRemoteServiceObjectMBean
{
   private boolean m_running;

   public MyRemoteServiceObject() throws RemoteException
   {
   }

   public void sayHello(String name) throws RemoteException
   {
      System.out.println("Hello, " + name);
   }

   public void start() throws Exception
   {
      if (!m_running)
      {
         UnicastRemoteObject.exportObject(this);
         InitialContext ctx = new InitialContext();
         ctx.rebind(JNDI_NAME, this);
         m_running = true;
         System.out.println("My remote service started successfully");
      }
   }

   public void stop() throws Exception
   {
      if (m_running)
      {
         InitialContext ctx = new InitialContext();
         ctx.unbind(JNDI_NAME);
         UnicastRemoteObject.unexportObject(this, false);
         m_running = false;
         System.out.println("My remote service stopped successfully");
      }
   }

   public boolean isRunning()
   {
      return m_running;
   }
}
