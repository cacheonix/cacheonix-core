/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.rmi;

/**
 * The management interface exposed by the service.
 * As you can see, the management operations consist of
 * starting and stopping the service along with seeing if the server is running.
 * Note that it does not contain the {@link MyRemoteService#sayHello} method, which is
 * considered in this example a business method and not a management method.
 *
 * @version $Revision: 1.4 $
 */
public interface MyRemoteServiceObjectMBean
{
   public void start() throws Exception;

   public void stop() throws Exception;

   public boolean isRunning();
}
