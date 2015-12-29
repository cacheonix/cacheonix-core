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
package org.cacheonix.impl.util.logging.chainsaw;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * A daemon thread the processes connections from a <code>org.cacheonix.impl.util.logging.net.SocketAppender.html</code>.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 */
final class LoggingReceiver extends Thread {

   /**
    * used to log messages *
    */
   private static final Logger LOG = Logger.getLogger(LoggingReceiver.class);

   /**
    * Helper that actually processes a client connection. It receives events and adds them to the supplied model.
    *
    * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
    */
   private final class Slurper implements Runnable {

      /**
       * socket connection to read events from *
       */
      private final Socket mClient;


      /**
       * Creates a new <code>Slurper</code> instance.
       *
       * @param aClient socket to receive events from
       */
      Slurper(final Socket aClient) {
         mClient = aClient;
      }


      /**
       * loops getting the events *
       */
      public final void run() {
         LOG.debug("Starting to get data");
         try {
            final ObjectInputStream ois =
                    new ObjectInputStream(mClient.getInputStream());
            //noinspection InfiniteLoopStatement
            while (true) {
               final LoggingEvent event = (LoggingEvent) ois.readObject();
               mModel.addEvent(new EventDetails(event));
            }
         } catch (final EOFException ignored) {
            LOG.info("Reached EOF, closing connection");
         } catch (final SocketException ignored) {
            LOG.info("Caught SocketException, closing connection");
         } catch (final IOException e) {
            LOG.warn("Got IOException, closing connection", e);
         } catch (final ClassNotFoundException e) {
            LOG.warn("Got ClassNotFoundException, closing connection", e);
         }

         try {
            mClient.close();
         } catch (final IOException e) {
            LOG.warn("Error closing connection", e);
         }
      }
   }

   /**
    * where to put the events *
    */
   private final MyTableModel mModel;

   /**
    * server for listening for connections *
    */
   private final ServerSocket mSvrSock;


   /**
    * Creates a new <code>LoggingReceiver</code> instance.
    *
    * @param aModel model to place put received into
    * @param aPort  port to listen on
    * @throws IOException if an error occurs
    * @noinspection SocketOpenedButNotSafelyClosed
    */
   LoggingReceiver(final MyTableModel aModel, final int aPort) throws IOException {
      setDaemon(true);
      mModel = aModel;
      mSvrSock = new ServerSocket(aPort);
   }


   /**
    * Listens for client connections *
    *
    * @noinspection SocketOpenedButNotSafelyClosed
    */
   public final void run() {
      LOG.info("Thread started");
      try {
         //noinspection InfiniteLoopStatement
         while (true) {
            LOG.debug("Waiting for a connection");
            final Socket client = mSvrSock.accept();
            LOG.debug("Got a connection from " +
                    client.getInetAddress().getHostName());
            final Thread t = new Thread(new Slurper(client));
            t.setDaemon(true);
            t.start();
         }
      } catch (final IOException e) {
         LOG.error("Error in accepting connections, stopping.", e);
      }
   }
}
