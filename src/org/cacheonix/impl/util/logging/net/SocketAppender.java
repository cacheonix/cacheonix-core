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

// Contributors: Dan MacDonald <dan@redknee.com>

package org.cacheonix.impl.util.logging.net;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import org.cacheonix.impl.util.logging.AppenderSkeleton;
import org.cacheonix.impl.util.logging.LogManager;
import org.cacheonix.impl.util.logging.NDC;
import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.spi.ErrorCode;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * Sends {@link LoggingEvent} objects to a remote a log server, usually a {@link SocketNode}.
 * <p/>
 * <p>The SocketAppender has the following properties:
 * <p/>
 * <ul>
 * <p/>
 * <p><li>If sent to a {@link SocketNode}, remote logging is non-intrusive as far as the log event is concerned. In
 * other words, the event will be logged with the same time stamp, {@link NDC}, location info as if it were logged
 * locally by the client.
 * <p/>
 * <p><li>SocketAppenders do not use a layout. They ship a serialized {@link LoggingEvent} object to the server side.
 * <p/>
 * <p><li>Remote logging uses the TCP protocol. Consequently, if the server is reachable, then log events will
 * eventually arrive at the server.
 * <p/>
 * <p><li>If the remote server is down, the logging requests are simply dropped. However, if and when the server comes
 * back up, then event transmission is resumed transparently. This transparent reconneciton is performed by a
 * <em>connector</em> thread which periodically attempts to connect to the server.
 * <p/>
 * <p><li>Logging events are automatically <em>buffered</em> by the native TCP implementation. This means that if the
 * link to server is slow but still faster than the rate of (log) event production by the client, the client will not be
 * affected by the slow network connection. However, if the network connection is slower then the rate of event
 * production, then the client can only progress at the network rate. In particular, if the network link to the the
 * server is down, the client will be blocked.
 * <p/>
 * <p>On the other hand, if the network link is up, but the server is down, the client will not be blocked when making
 * log requests but the log events will be lost due to server unavailability.
 * <p/>
 * <p><li>Even if a <code>SocketAppender</code> is no longer attached to any category, it will not be garbage collected
 * in the presence of a connector thread. A connector thread exists only if the connection to the server is down. To
 * avoid this garbage collection problem, you should {@link #close} the the <code>SocketAppender</code> explicitly. See
 * also next item.
 * <p/>
 * <p>Long lived applications which create/destroy many <code>SocketAppender</code> instances should be aware of this
 * garbage collection problem. Most other applications can safely ignore it.
 * <p/>
 * <p><li>If the JVM hosting the <code>SocketAppender</code> exits before the <code>SocketAppender</code> is closed
 * either explicitly or subsequent to garbage collection, then there might be untransmitted data in the pipe which might
 * be lost. This is a common problem on Windows based systems.
 * <p/>
 * <p>To avoid lost data, it is usually sufficient to {@link #close} the <code>SocketAppender</code> either explicitly
 * or by calling the {@link LogManager#shutdown} method before exiting the application.
 * <p/>
 * <p/>
 * </ul>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.8.4
 */

public final class SocketAppender extends AppenderSkeleton {

   /**
    * The default port number of remote logging server (4560).
    *
    * @since 1.2.15
    */
   public static final int DEFAULT_PORT = 4560;

   /**
    * The default reconnection delay (30000 milliseconds or 30 seconds).
    */
   static final int DEFAULT_RECONNECTION_DELAY = 30000;

   /**
    * We remember host name as String in addition to the resolved InetAddress so that it can be returned via
    * getOption().
    */
   String remoteHost = null;

   InetAddress address = null;

   int port = DEFAULT_PORT;

   ObjectOutputStream oos = null;

   int reconnectionDelay = DEFAULT_RECONNECTION_DELAY;

   boolean locationInfo = false;

   private String application = null;

   private Connector connector = null;

   int counter = 0;

   // reset the ObjectOutputStream every 70 calls
   //private static final int RESET_FREQUENCY = 70;
   private static final int RESET_FREQUENCY = 1;


   public SocketAppender() {

   }


   /**
    * Connects to remote server at <code>address</code> and <code>port</code>.
    */
   public SocketAppender(final InetAddress address, final int port) {

      this.address = address;
      this.remoteHost = address.getHostName();
      this.port = port;
      connect(address, port);
   }


   /**
    * Connects to remote server at <code>host</code> and <code>port</code>.
    */
   public SocketAppender(final String host, final int port) {

      this.port = port;
      this.address = getAddressByName(host);
      this.remoteHost = host;
      connect(address, port);
   }


   /**
    * Connect to the specified <b>RemoteHost</b> and <b>Port</b>.
    */
   public void activateOptions() {

      connect(address, port);
   }


   /**
    * Close this appender.
    * <p/>
    * <p>This will mark the appender as closed and call then {@link #cleanUp} method.
    */
   public synchronized void close() {

      if (closed) {
         return;
      }

      this.closed = true;
      cleanUp();
   }


   /**
    * Drop the connection to the remote host and release the underlying connector thread if it has been created
    */
   public final void cleanUp() {

      if (oos != null) {
         try {
            oos.close();
         } catch (final IOException e) {
            LogLog.error("Could not close oos.", e);
         }
         oos = null;
      }
      if (connector != null) {
         //LogLog.debug("Interrupting the connector.");
         connector.interrupted = true;
         connector = null;  // allow gc
      }
   }


   final void connect(final InetAddress address, final int port) {

      if (this.address == null) {
         return;
      }
      try {
         // First, close the previous connection if any.
         cleanUp();
         oos = new ObjectOutputStream(new Socket(address, port).getOutputStream());
      } catch (final IOException e) {

         String msg = "Could not connect to remote log4j server at ["
                 + address.getHostName() + "].";
         if (reconnectionDelay > 0) {
            msg += " We will try again later.";
            fireConnector(); // fire the connector thread
         } else {
            msg += " We are not retrying.";
            errorHandler.error(msg, e, ErrorCode.GENERIC_FAILURE);
         }
         LogLog.error(msg);
      }
   }


   public void append(final LoggingEvent event) {

      if (event == null) {
         return;
      }

      if (address == null) {
         errorHandler.error("No remote host is set for SocketAppender named \"" +
                 this.name + "\".");
         return;
      }

      if (oos != null) {
         try {

            if (locationInfo) {
               event.getLocationInformation();
            }
            if (application != null) {
               event.setProperty("application", application);
            }
            oos.writeObject(event);
            //LogLog.debug("=========Flushing.");
            oos.flush();
            if (++counter >= RESET_FREQUENCY) {
               counter = 0;
               // Failing to reset the object output stream every now and
               // then creates a serious memory leak.
               //System.err.println("Doing oos.reset()");
               oos.reset();
            }
         } catch (final IOException e) {
            oos = null;
            LogLog.warn("Detected problem with connection: " + e);
            if (reconnectionDelay > 0) {
               fireConnector();
            } else {
               errorHandler.error("Detected problem with connection, not reconnecting.", e,
                       ErrorCode.GENERIC_FAILURE);
            }
         }
      }
   }


   final void fireConnector() {

      if (connector == null) {
         LogLog.debug("Starting a new connector thread.");
         connector = new Connector();
         connector.setDaemon(true);
         connector.setPriority(Thread.MIN_PRIORITY);
         connector.start();
      }
   }


   static InetAddress getAddressByName(final String host) {

      try {
         return InetAddress.getByName(host);
      } catch (final Exception e) {
         LogLog.error("Could not find address of [" + host + "].", e);
         return null;
      }
   }


   /**
    * The SocketAppender does not use a layout. Hence, this method returns <code>false</code>.
    */
   public boolean requiresLayout() {

      return false;
   }


   /**
    * The <b>RemoteHost</b> option takes a string value which should be the host name of the server where a {@link
    * SocketNode} is running.
    */
   public void setRemoteHost(final String host) {

      address = getAddressByName(host);
      remoteHost = host;
   }


   /**
    * Returns value of the <b>RemoteHost</b> option.
    */
   public String getRemoteHost() {

      return remoteHost;
   }


   /**
    * The <b>Port</b> option takes a positive integer representing the port where the server is waiting for
    * connections.
    */
   public void setPort(final int port) {

      this.port = port;
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
   public void setLocationInfo(final boolean locationInfo) {

      this.locationInfo = locationInfo;
   }


   /**
    * Returns value of the <b>LocationInfo</b> option.
    */
   public boolean getLocationInfo() {

      return locationInfo;
   }


   /**
    * The <b>App</b> option takes a string value which should be the name of the application getting logged. If property
    * was already set (via system property), don't set here.
    */
   public void setApplication(final String lapp) {

      this.application = lapp;
   }


   /**
    * Returns value of the <b>Application</b> option.
    */
   public String getApplication() {

      return application;
   }


   /**
    * The <b>ReconnectionDelay</b> option takes a positive integer representing the number of milliseconds to wait
    * between each failed connection attempt to the server. The default value of this option is 30000 which corresponds
    * to 30 seconds.
    * <p/>
    * <p>Setting this option to zero turns off reconnection capability.
    */
   public void setReconnectionDelay(final int delay) {

      this.reconnectionDelay = delay;
   }


   /**
    * Returns value of the <b>ReconnectionDelay</b> option.
    */
   public int getReconnectionDelay() {

      return reconnectionDelay;
   }


   /**
    * The Connector will reconnect when the server becomes available again.  It does this by attempting to open a new
    * connection every <code>reconnectionDelay</code> milliseconds.
    * <p/>
    * <p>It stops trying whenever a connection is established. It will restart to try reconnect to the server when
    * previpously open connection is droppped.
    *
    * @author Ceki G&uuml;lc&uuml;
    * @since 0.8.4
    */
   final class Connector extends Thread {

      volatile boolean interrupted = false;


      public final void run() {

         while (!interrupted) {
            try {
               sleep((long) reconnectionDelay);
               LogLog.debug("Attempting connection to " + address.getHostName());
               final Socket socket = new Socket(address, port);
               synchronized (this) {
                  oos = new ObjectOutputStream(socket.getOutputStream());
                  connector = null;
                  LogLog.debug("Connection established. Exiting connector thread.");
                  break;
               }
            } catch (final InterruptedException e) {
               LogLog.debug("Connector interrupted. Leaving loop.");
               return;
            } catch (final ConnectException e) {
               LogLog.debug("Remote host " + address.getHostName()
                       + " refused connection.");
            } catch (final IOException e) {
               LogLog.debug("Could not connect to " + address.getHostName() +
                       ". Exception is " + e);
            }
         }
         //LogLog.debug("Exiting Connector.run() method.");
      }

      /**
       public
       void finalize() {
       LogLog.debug("Connector finalize() has been called.");
       }
       */
   }

}
