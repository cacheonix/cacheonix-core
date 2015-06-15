/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.config;

import java.net.Socket;

/**
 * This class invokes the shutdown section of the XML configuration file bundled
 * with this example. <br />
 * Refer to the ConfigurationLoader documentation for further information.
 *
 * @version $Revision: 1.3 $
 * @see ConfigurationStartup
 */
public class ConfigurationShutdown
{
   public static void main(String[] args) throws Exception
   {
      String shutdownCommand = "shutdown";
      Socket socket = new Socket("127.0.0.1", 9876);
      socket.getOutputStream().write(shutdownCommand.getBytes());
      socket.close();
   }
}
