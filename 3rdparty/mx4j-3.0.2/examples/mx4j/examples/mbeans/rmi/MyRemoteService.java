/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The RMI Remote interface of the service.
 *
 * @version $Revision: 1.4 $
 */
public interface MyRemoteService extends Remote
{
   public static final String JNDI_NAME = "rmi://localhost:1099/my-service";

   public void sayHello(String name) throws RemoteException;
}
