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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.spi.LoggerRepository;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

// Contributors:  Moses Hohman <mmhohman@rainbow.uchicago.edu>

/**
 * Read {@link LoggingEvent} objects sent from a remote client using Sockets (TCP). These logging events are logged
 * according to local policy, as if they were generated locally.
 * <p/>
 * <p>For example, the socket node might decide to log events to a local file and also resent them to a second socket
 * node.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.8.4
 */
public final class SocketNode implements Runnable {

   final Socket socket;
   final LoggerRepository hierarchy;
   ObjectInputStream ois = null;

   static final Logger logger = Logger.getLogger(SocketNode.class);


   public SocketNode(final Socket socket, final LoggerRepository hierarchy) {
      this.socket = socket;
      this.hierarchy = hierarchy;
      try {
         ois = new ObjectInputStream(
                 new BufferedInputStream(socket.getInputStream()));
      }
      catch (final Exception e) {
         logger.error("Could not open ObjectInputStream to " + socket, e);
      }
   }

   //public
   //void finalize() {
   //System.err.println("-------------------------Finalize called");
   // System.err.flush();
   //}


   public final void run() {

      try {
         if (ois != null) {
            //noinspection InfiniteLoopStatement
            while (true) {
               // read an event from the wire
               final LoggingEvent event = (LoggingEvent) ois.readObject();
               // get a logger from the hierarchy. The name of the logger is taken to be the name contained in the event.
               final Logger remoteLogger = hierarchy.getLogger(event.getLoggerName());
               //event.logger = remoteLogger;
               // apply the logger-level filter
               if (event.getLevel().isGreaterOrEqual(remoteLogger.getEffectiveLevel())) {
                  // finally log the event as if was generated locally
                  remoteLogger.callAppenders(event);
               }
            }
         }
      } catch (final EOFException ignored) {
         logger.info("Caught java.io.EOFException closing connection.");
      } catch (final SocketException ignored) {
         logger.info("Caught java.net.SocketException closing connection.");
      } catch (final IOException e) {
         logger.info("Caught java.io.IOException: " + e);
         logger.info("Closing connection.");
      } catch (final Exception e) {
         logger.error("Unexpected exception. Closing connection.", e);
      } finally {
         if (ois != null) {
            try {
               ois.close();
            } catch (final Exception e) {
               logger.info("Could not close connection.", e);
            }
         }
         if (socket != null) {
            try {
               socket.close();
            } catch (final IOException ignored) {
            }
         }
      }
   }
}
