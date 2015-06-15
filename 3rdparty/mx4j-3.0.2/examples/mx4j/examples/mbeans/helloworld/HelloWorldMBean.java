/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.helloworld;

import java.io.IOException;

/**
 * Management interface for the HelloWorld MBean
 *
 * @version $Revision: 1.3 $
 */
public interface HelloWorldMBean
{
   public void reloadConfiguration() throws IOException;

   public int getHowManyTimes();
}
