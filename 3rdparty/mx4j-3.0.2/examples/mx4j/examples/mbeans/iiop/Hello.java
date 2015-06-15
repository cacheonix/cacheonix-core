/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.mbeans.iiop;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The RMI Remote interface exposed by the Hello service.
 *
 * @version $Revision: 1.4 $
 */
public interface Hello extends Remote
{
   public static final String IIOP_JNDI_NAME = "iiop://localhost:1900/iiop_service";

   public void sayHello(String name) throws RemoteException;
}
