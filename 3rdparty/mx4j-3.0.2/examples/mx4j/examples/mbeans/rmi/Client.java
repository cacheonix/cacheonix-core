/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.rmi;

import javax.naming.InitialContext;

/**
 * @version $Revision: 1.3 $
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      MyRemoteService service = (MyRemoteService)ctx.lookup(MyRemoteService.JNDI_NAME);

      service.sayHello("Simon");
   }
}
