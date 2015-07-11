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
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import org.cacheonix.impl.util.logging.AppenderSkeleton;
import org.cacheonix.impl.util.logging.LogManager;
import org.cacheonix.impl.util.logging.NDC;
import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * Sends {@link LoggingEvent} objects to a set of remote log servers, usually a {@link SocketNode SocketNodes}.
 * <p/>
 * <p>Acts just like {@link SocketAppender} except that instead of connecting to a given remote log server,
 * <code>SocketHubAppender</code> accepts connections from the remote log servers as clients.  It can accept more than
 * one connection. When a log event is received, the event is sent to the set of currently connected remote log servers.
 * Implemented this way it does not require any update to the configuration file to send data to another remote log
 * server. The remote log server simply connects to the host and port the <code>SocketHubAppender</code> is running on.
 * <p/>
 * <p>The <code>SocketHubAppender</code> does not store events such that the remote side will events that arrived after
 * the establishment of its connection. Once connected, events arrive in order as guaranteed by the TCP protocol.
 * <p/>
 * <p>This implementation borrows heavily from the {@link SocketAppender}.
 * <p/>
 * <p>The SocketHubAppender has the following characteristics:
 * <p/>
 * <ul>
 * <p/>
 * <p><li>If sent to a {@link SocketNode}, logging is non-intrusive as far as the log event is concerned. In other
 * words, the event will be logged with the same time stamp, {@link NDC}, location info as if it were logged locally.
 * <p/>
 * <p><li><code>SocketHubAppender</code> does not use a layout. It ships a serialized {@link LoggingEvent} object to the
 * remote side.
 * <p/>
 * <p><li><code>SocketHubAppender</code> relies on the TCP protocol. Consequently, if the remote side is reachable, then
 * log events will eventually arrive at remote client.
 * <p/>
 * <p><li>If no remote clients are attached, the logging requests are simply dropped.
 * <p/>
 * <p><li>Logging events are automatically <em>buffered</em> by the native TCP implementation. This means that if the
 * link to remote client is slow but still faster than the rate of (log) event production, the application will not be
 * affected by the slow network connection. However, if the network connection is slower then the rate of event
 * production, then the local application can only progress at the network rate. In particular, if the network link to
 * the the remote client is down, the application will be blocked.
 * <p/>
 * <p>On the other hand, if the network link is up, but the remote client is down, the client will not be blocked when
 * making log requests but the log events will be lost due to client unavailability.
 * <p/>
 * <p>The single remote client case extends to multiple clients connections. The rate of logging will be determined by
 * the slowest link.
 * <p/>
 * <p><li>If the JVM hosting the <code>SocketHubAppender</code> exits before the <code>SocketHubAppender</code> is
 * closed either explicitly or subsequent to garbage collection, then there might be untransmitted data in the pipe
 * which might be lost. This is a common problem on Windows based systems.
 * <p/>
 * <p>To avoid lost data, it is usually sufficient to {@link #close} the <code>SocketHubAppender</code> either
 * explicitly or by calling the {@link LogManager#shutdown} method before exiting the application.
 * <p/>
 * </ul>
 *
 * @author Mark Womack
 */

public final class SocketHubAppender extends AppenderSkeleton {

   /**
    * The default port number of the ServerSocket will be created on.
    */
   static final int DEFAULT_PORT = 4560;

   private int port = DEFAULT_PORT;
   private final Vector oosList = new Vector(11);
   private ServerMonitor serverMonitor = null;
   private boolean locationInfo = false;


   public SocketHubAppender() {
   }


   /**
    * Connects to remote server at <code>address</code> and <code>port</code>.
    */
   public SocketHubAppender(final int _port) {
      port = _port;
      startServer();
   }


   /**
    * Set up the socket server on the specified port.
    */
   public void activateOptions() {
      startServer();
   }


   /**
    * Close this appender. <p>This will mark the appender as closed and call then {@link #cleanUp} method.
    */
   public synchronized void close() {
      if (closed) {
         return;
      }

      LogLog.debug("closing SocketHubAppender " + getName());
      this.closed = true;
      cleanUp();
      LogLog.debug("SocketHubAppender " + getName() + " closed");
   }


   /**
    * Release the underlying ServerMonitor thread, and drop the connections to all connected remote servers.
    */
   public final void cleanUp() {
      // stop the monitor thread
      LogLog.debug("stopping ServerSocket");
      serverMonitor.stopMonitor();
      serverMonitor = null;

      // close all of the connections
      LogLog.debug("closing client connections");
      while (!oosList.isEmpty()) {
         final ObjectOutputStream oos = (ObjectOutputStream) oosList.elementAt(0);
         if (oos != null) {
            try {
               oos.close();
            }
            catch (final IOException e) {
               LogLog.error("could not close oos.", e);
            }

            oosList.removeElementAt(0);
         }
      }
   }


   /**
    * Append an event to all of current connections.
    */
   public void append(final LoggingEvent event) {
      // if no event or no open connections, exit now
      if (event == null || oosList.isEmpty()) {
         return;
      }

      // set up location info if requested
      if (locationInfo) {
         event.getLocationInformation();
      }

      // loop through the current set of open connections, appending the event to each
      for (int streamCount = 0; streamCount < oosList.size(); streamCount++) {

         ObjectOutputStream oos = null;
         try {
            oos = (ObjectOutputStream) oosList.elementAt(streamCount);
         }
         catch (final ArrayIndexOutOfBoundsException e) {
            // catch this, but just don't assign a value
            // this should not really occur as this method is
            // the only one that can remove oos's (besides cleanUp).
         }

         // list size changed unexpectedly? Just exit the append.
         if (oos == null) {
            break;
         }

         try {
            oos.writeObject(event);
            oos.flush();
            // Failing to reset the object output stream every now and
            // then creates a serious memory leak.
            // right now we always reset. TODO - set up frequency counter per oos?
            oos.reset();
         }
         catch (final IOException e) {
            // there was an io exception so just drop the connection
            oosList.removeElementAt(streamCount);
            LogLog.debug("dropped connection");

            // decrement to keep the counter in place (for loop always increments)
            streamCount--;
         }
      }
   }


   /**
    * The SocketHubAppender does not use a layout. Hence, this method returns <code>false</code>.
    */
   public boolean requiresLayout() {
      return false;
   }


   /**
    * The <b>Port</b> option takes a positive integer representing the port where the server is waiting for
    * connections.
    */
   public void setPort(final int _port) {
      port = _port;
   }


   /**
    * Returns value of the <b>Port</b> option.
    */
   public int getPort() {
      return port;
   }


   /**
    * The <b>LocationInfo</b> option takes a boolean value. If true, the information sent to the remote host will
    * include location information. By default no location information is sent to the server.
    */
   public void setLocationInfo(final boolean _locationInfo) {
      locationInfo = _locationInfo;
   }


   /**
    * Returns value of the <b>LocationInfo</b> option.
    */
   public boolean getLocationInfo() {
      return locationInfo;
   }


   /**
    * Start the ServerMonitor thread.
    */
   private void startServer() {
      serverMonitor = new ServerMonitor(port, oosList);
   }


   /**
    * This class is used internally to monitor a ServerSocket and register new connections in a vector passed in the
    * constructor.
    */
   private static final class ServerMonitor implements Runnable {

      private final int port;
      private final Vector oosList;
      private boolean keepRunning;
      private Thread monitorThread;


      /**
       * Create a thread and start the monitor.
       */
      @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
      ServerMonitor(final int _port, final Vector _oosList) {
         port = _port;
         oosList = _oosList;
         keepRunning = true;
         monitorThread = new Thread(this);
         monitorThread.setDaemon(true);
         monitorThread.start();
      }


      /**
       * Stops the monitor. This method will not return until the thread has finished executing.
       */
      public
      final synchronized void stopMonitor() {
         if (keepRunning) {
            LogLog.debug("server monitor thread shutting down");
            keepRunning = false;
            try {
               monitorThread.join();
            }
            catch (final InterruptedException e) {
               // do nothing?
            }

            // release the thread
            monitorThread = null;
            LogLog.debug("server monitor thread shut down");
         }
      }


      /**
       * Method that runs, monitoring the ServerSocket and adding connections as they connect to the socket.
       */
      public final void run() {
         ServerSocket serverSocket = null;
         try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
         }
         catch (final Exception e) {
            LogLog.error("exception setting timeout, shutting down server socket.", e);
            keepRunning = false;
            return;
         }

         try {
            try {
               serverSocket.setSoTimeout(1000);
            }
            catch (final SocketException e) {
               LogLog.error("exception setting timeout, shutting down server socket.", e);
               return;
            }

            while (keepRunning) {
               Socket socket = null;
               try {
                  socket = serverSocket.accept();
               }
               catch (final InterruptedIOException e) {
                  // timeout occurred, so just loop
               }
               catch (final SocketException e) {
                  LogLog.error("exception accepting socket, shutting down server socket.", e);
                  keepRunning = false;
               }
               catch (final IOException e) {
                  LogLog.error("exception accepting socket.", e);
               }

               // if there was a socket accepted
               if (socket != null) {
                  try {
                     final InetAddress remoteAddress = socket.getInetAddress();
                     LogLog.debug("accepting connection from " + remoteAddress.getHostName()
                             + " (" + remoteAddress.getHostAddress() + ')');

                     // create an ObjectOutputStream
                     final ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                     // add it to the oosList.  OK since Vector is synchronized.
                     oosList.addElement(oos);
                  }
                  catch (final IOException e) {
                     LogLog.error("exception creating output stream on socket.", e);
                  }
               }
            }
         }
         finally {
            // close the socket
            try {
               serverSocket.close();
            }
            catch (final IOException e) {
               // do nothing with it?
            }
         }
      }
   }
}

