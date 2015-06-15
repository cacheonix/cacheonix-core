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

package org.cacheonix.impl.util.logging.varia;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.cacheonix.impl.util.logging.RollingFileAppender;
import org.cacheonix.impl.util.logging.helpers.LogLog;

/**
 * This appender listens on a socket on the port specified by the <b>Port</b> property for a "RollOver" message. When
 * such a message is received, the underlying log file is rolled over and an acknowledgment message is sent back to the
 * process initiating the roll over.
 * <p/>
 * <p>This method of triggering roll over has the advantage of being operating system independent, fast and reliable.
 * <p/>
 * <p>A simple application {@link Roller} is provided to initiate the roll over.
 * <p/>
 * <p>Note that the initiator is not authenticated. Anyone can trigger a rollover. In production environments, it is
 * recommended that you add some form of protection to prevent undesired rollovers.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since version 0.9.0
 */
public final class ExternallyRolledFileAppender extends RollingFileAppender {

   /**
    * The string constant sent to initiate a roll over.   Current value of this string constant is <b>RollOver</b>.
    */
   public static final String ROLL_OVER = "RollOver";

   /**
    * The string constant sent to acknowledge a roll over.   Current value of this string constant is <b>OK</b>.
    */
   public static final String OK = "OK";

   int port = 0;
   HUP hup = null;


   /**
    * The <b>Port</b> [roperty is used for setting the port for listening to external roll over messages.
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
    * Start listening on the port specified by a preceding call to {@link #setPort}.
    */
   public void activateOptions() {
      super.activateOptions();
      if (port != 0) {
         if (hup != null) {
            hup.interrupt();
         }
         hup = new HUP(this, port);
         hup.setDaemon(true);
         hup.start();
      }
   }
}


final class HUP extends Thread {

   final int port;
   final ExternallyRolledFileAppender er;


   HUP(final ExternallyRolledFileAppender er, final int port) {
      this.er = er;
      this.port = port;
   }


   public final void run() {
      while (!isInterrupted()) {
         try {
            final ServerSocket serverSocket = new ServerSocket(port);
            //noinspection InfiniteLoopStatement
            while (true) {
               final Socket socket = serverSocket.accept();
               LogLog.debug("Connected to client at " + socket.getInetAddress());
               new Thread(new HUPNode(socket, er)).start();
            }
         }
         catch (final Exception e) {
            e.printStackTrace();
         }
      }
   }
}

final class HUPNode implements Runnable {

   DataInputStream dis = null;
   DataOutputStream dos = null;
   final ExternallyRolledFileAppender er;


   HUPNode(final Socket socket, final ExternallyRolledFileAppender er) {
      this.er = er;
      try {
         dis = new DataInputStream(socket.getInputStream());
         dos = new DataOutputStream(socket.getOutputStream());
      }
      catch (final Exception e) {
         e.printStackTrace();
      }
   }


   public final void run() {
      try {
         final String line = dis.readUTF();
         LogLog.debug("Got external roll over signal.");
         if (ExternallyRolledFileAppender.ROLL_OVER.equals(line)) {
            synchronized (er) {
               er.rollOver();
            }
            dos.writeUTF(ExternallyRolledFileAppender.OK);
         } else {
            dos.writeUTF("Expecting [RollOver] string.");
         }
         dos.close();
      }
      catch (final Exception e) {
         LogLog.error("Unexpected exception. Exiting HUPNode.", e);
      }
   }
}

