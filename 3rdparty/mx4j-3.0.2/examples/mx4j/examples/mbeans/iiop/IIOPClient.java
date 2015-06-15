/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.mbeans.iiop;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * This example connects to an RMI over IIOP server and invoke the services it exposes,
 * in this simple example it just calls {@link Hello#sayHello}.
 *
 * @version $Revision: 1.4 $
 */
public class IIOPClient
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      Hello remoteInterface = (Hello)PortableRemoteObject.narrow(ctx.lookup(Hello.IIOP_JNDI_NAME), Hello.class);
      remoteInterface.sayHello("from the MX4J Team");
   }
}
