/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cacheonix.impl.util.logging.net;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.cacheonix.impl.util.logging.Hierarchy;
import org.cacheonix.impl.util.logging.Level;
import org.cacheonix.impl.util.logging.LogManager;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.PropertyConfigurator;
import org.cacheonix.impl.util.logging.spi.LoggerRepository;
import org.cacheonix.impl.util.logging.spi.RootLogger;


/**
 * A {@link SocketNode} based server that uses a different hierarchy for each client.
 * <p/>
 * <pre>
 * <b>Usage:</b> java org.cacheonix.impl.util.logging.net.SocketServer port configFile configDir
 * <p/>
 * where <b>port</b> is a part number where the server listens,
 * <b>configFile</b> is a configuration file fed to the {@link PropertyConfigurator} and
 * <b>configDir</b> is a path to a directory containing configuration files, possibly one for each
 * client host.
 * </pre>
 * <p/>
 * <p>The <code>configFile</code> is used to configure the log4j default hierarchy that the <code>SocketServer</code>
 * will use to report on its actions.
 * <p/>
 * <p>When a new connection is opened from a previously unknown host, say <code>foo.bar.net</code>, then the
 * <code>SocketServer</code> will search for a configuration file called <code>foo.bar.net.lcf</code> under the
 * directory <code>configDir</code> that was passed as the third argument. If the file can be found, then a new
 * hierarchy is instantiated and configured using the configuration file <code>foo.bar.net.lcf</code>. If and when the
 * host <code>foo.bar.net</code> opens another connection to the server, then the previously configured hierarchy is
 * used.
 * <p/>
 * <p>In case there is no file called <code>foo.bar.net.lcf</code> under the directory <code>configDir</code>, then the
 * <em>generic</em> hierarchy is used. The generic hierarchy is configured using a configuration file called
 * <code>generic.lcf</code> under the <code>configDir</code> directory. If no such file exists, then the generic
 * hierarchy will be identical to the log4j default hierarchy.
 * <p/>
 * <p>Having different client hosts log using different hierarchies ensures the total independence of the clients with
 * respect to their logging settings.
 * <p/>
 * <p>Currently, the hierarchy that will be used for a given request depends on the IP address of the client host. For
 * example, two separate applications running on the same host and logging to the same server will share the same
 * hierarchy. This is perfectly safe except that it might not provide the right amount of independence between
 * applications. The <code>SocketServer</code> is intended as an example to be enhanced in order to implement more
 * elaborate policies.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.0
 */

public final class SocketServer {

   static final String GENERIC = "generic";
   static final String CONFIG_FILE_EXT = ".lcf";

   static final Logger cat = Logger.getLogger(SocketServer.class);
   @SuppressWarnings("StaticNonFinalField")
   static SocketServer server;
   @SuppressWarnings("StaticNonFinalField")
   static int port;

   // key=inetAddress, value=hierarchy
   final Hashtable hierarchyMap;
   LoggerRepository genericHierarchy = null;
   final File dir;


   public
   static void main(final String[] argv) {
      if (argv.length == 3) {
         init(argv[0], argv[1], argv[2]);
      } else {
         usage("Wrong number of arguments.");
      }

      try {
         cat.info("Listening on port " + port);
         final ServerSocket serverSocket = new ServerSocket(port);
         //noinspection InfiniteLoopStatement
         while (true) {
            cat.info("Waiting to accept a new client.");
            final Socket socket = serverSocket.accept();
            final InetAddress inetAddress = socket.getInetAddress();
            cat.info("Connected to client at " + inetAddress);

            LoggerRepository h = (LoggerRepository) server.hierarchyMap.get(inetAddress);
            if (h == null) {
               h = server.configureHierarchy(inetAddress);
            }

            cat.info("Starting new socket node.");
            new Thread(new SocketNode(socket, h)).start();
         }
      }
      catch (final Exception e) {
         e.printStackTrace();
      }
   }


   static void usage(final String msg) {
      System.err.println(msg);
      System.err.println(
              "Usage: java " + SocketServer.class.getName() + " port configFile directory");
      System.exit(1);
   }


   static void init(final String portStr, final String configFile, final String dirStr) {
      try {
         port = Integer.parseInt(portStr);
      }
      catch (final NumberFormatException e) {
         e.printStackTrace();
         usage("Could not interpret port number [" + portStr + "].");
      }

      PropertyConfigurator.configure(configFile);

      final File dir = new File(dirStr);
      if (!dir.isDirectory()) {
         usage('[' + dirStr + "] is not a directory.");
      }
      server = new SocketServer(dir);
   }


   public SocketServer(final File directory) {
      this.dir = directory;
      hierarchyMap = new Hashtable(11);
   }


   // This method assumes that there is no hiearchy for inetAddress
   // yet. It will configure one and return it.
   final LoggerRepository configureHierarchy(final InetAddress inetAddress) {
      cat.info("Locating configuration file for " + inetAddress);
      // We assume that the toSting method of InetAddress returns is in
      // the format hostname/d1.d2.d3.d4 e.g. torino/192.168.1.1
      final String s = inetAddress.toString();
      final int i = s.indexOf('/');
      if (i == -1) {
         cat.warn("Could not parse the inetAddress [" + inetAddress +
                 "]. Using default hierarchy.");
         return genericHierarchy();
      } else {
         final String key = s.substring(0, i);

         final File configFile = new File(dir, key + CONFIG_FILE_EXT);
         if (configFile.exists()) {
            final Hierarchy h = new Hierarchy(new RootLogger(Level.DEBUG));
            hierarchyMap.put(inetAddress, h);

            new PropertyConfigurator().doConfigure(configFile.getAbsolutePath(), h);

            return h;
         } else {
            cat.warn("Could not find config file [" + configFile + "].");
            return genericHierarchy();
         }
      }
   }


   final LoggerRepository genericHierarchy() {
      if (genericHierarchy == null) {
         final File f = new File(dir, GENERIC + CONFIG_FILE_EXT);
         if (f.exists()) {
            genericHierarchy = new Hierarchy(new RootLogger(Level.DEBUG));
            new PropertyConfigurator().doConfigure(f.getAbsolutePath(), genericHierarchy);
         } else {
            cat.warn("Could not find config file [" + f +
                    "]. Will use the default hierarchy.");
            genericHierarchy = LogManager.getLoggerRepository();
         }
      }
      return genericHierarchy;
   }
}
