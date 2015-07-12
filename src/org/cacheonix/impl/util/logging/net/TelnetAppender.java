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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import org.cacheonix.impl.util.logging.AppenderSkeleton;
import org.cacheonix.impl.util.logging.Layout;
import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * <p>The TelnetAppender is a log4j appender that specializes in writing to a read-only socket.  The output is provided
 * in a telnet-friendly way so that a log can be monitored over TCP/IP. Clients using telnet connect to the socket and
 * receive log data. This is handy for remote monitoring, especially when monitoring a servlet.
 * <p/>
 * <p>Here is a list of the available configuration options:
 * <p/>
 * <table border=1> <tr> <th>Name</th> <th>Requirement</th> <th>Description</th> <th>Sample Value</th> </tr>
 * <p/>
 * <tr> <td>Port</td> <td>optional</td> <td>This parameter determines the port to use for announcing log events.  The
 * default port is 23 (telnet).</td> <td>5875</td> </table>
 *
 * @author <a HREF="mailto:jay@v-wave.com">Jay Funnell</a>
 */

public final class TelnetAppender extends AppenderSkeleton {

   private SocketHandler sh = null;

   private int port = 23;


   /**
    * This appender requires a layout to format the text to the attached client(s).
    */
   public boolean requiresLayout() {

      return true;
   }


   /**
    * all of the options have been set, create the socket handler and wait for connections.
    */
   public void activateOptions() {

      try {
         sh = new SocketHandler(port);
         sh.start();
      } catch (final Exception e) {
         e.printStackTrace();
      }
      super.activateOptions();
   }


   public int getPort() {

      return port;
   }


   public void setPort(final int port) {

      this.port = port;
   }


   /**
    * shuts down the appender.
    */
   public void close() {

      if (sh != null) {
         sh.close();
         try {
            sh.join();
         } catch (final InterruptedException ex) {
         }
      }
   }


   /**
    * Handles a log event.  For this appender, that means writing the message to each connected client.
    */
   protected void append(final LoggingEvent event) {

      sh.send(this.layout.format(event));
      if (layout.ignoresThrowable()) {
         final String[] s = event.getThrowableStrRep();
         if (s != null) {
            final int len = s.length;
            for (final String value : s) {
               sh.send(value);
               sh.send(Layout.LINE_SEP);
            }
         }
      }
   }

   //---------------------------------------------------------- SocketHandler:

   /**
    * The SocketHandler class is used to accept connections from clients.  It is threaded so that clients can
    * connect/disconnect asynchronously.
    */
   protected static final class SocketHandler extends Thread {

      private final Vector writers = new Vector(3);

      private final Vector connections = new Vector(3);

      private final ServerSocket serverSocket;

      private static final int MAX_CONNECTIONS = 20;


      protected final void finalize() throws Throwable {

         close();
         super.finalize();
      }


      /**
       * make sure we close all network connections when this handler is destroyed.
       */
      public final void close() {

         for (final Enumeration e = connections.elements(); e.hasMoreElements();) {
            try {
               ((Socket) e.nextElement()).close();
            } catch (final Exception ex) {
            }
         }

         try {
            serverSocket.close();
         } catch (final Exception ex) {
         }
      }


      /**
       * sends a message to each of the clients in telnet-friendly output.
       */
      public final void send(final String message) {

         final Enumeration ce = connections.elements();
         for (final Enumeration e = writers.elements(); e.hasMoreElements();) {
            final Socket sock = (Socket) ce.nextElement();
            final PrintWriter writer = (PrintWriter) e.nextElement();
            writer.print(message);
            if (writer.checkError()) {
               // The client has closed the connection, remove it from our list:
               connections.remove(sock);
               writers.remove(writer);
            }
         }
      }


      /**
       * Continually accepts client connections.  Client connections are refused when MAX_CONNECTIONS is reached.
       */
      public final void run() {

         while (!serverSocket.isClosed()) {
            try {
               final Socket newClient = serverSocket.accept();
               final PrintWriter pw = new PrintWriter(newClient.getOutputStream());
               if (connections.size() < MAX_CONNECTIONS) {
                  connections.addElement(newClient);
                  writers.addElement(pw);
                  pw.print("TelnetAppender v1.0 (" + connections.size()
                          + " active connections)\r\n\r\n");
                  pw.flush();
               } else {
                  pw.print("Too many connections.\r\n");
                  pw.flush();
                  newClient.close();
               }
            } catch (final Exception e) {
               if (!serverSocket.isClosed()) {
                  LogLog.error("Encountered error while in SocketHandler loop.", e);
               }
               break;
            }
         }

         try {
            serverSocket.close();
         } catch (final IOException ex) {
         }
      }


      SocketHandler(final int port) throws IOException {

         serverSocket = new ServerSocket(port);
         setName("TelnetAppender-" + getName() + '-' + port);
      }

   }
}
