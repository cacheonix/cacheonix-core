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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.PropertyConfigurator;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;
import org.cacheonix.impl.util.logging.xml.DOMConfigurator;

/**
 * A simple application that consumes logging events sent by a {@link JMSAppender}.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public final class JMSSink implements MessageListener {

   static final Logger logger = Logger.getLogger(JMSSink.class);


   public static void main(final String[] args) throws Exception {
      if (args.length != 5) {
         usage("Wrong number of arguments.");
      }

      final String tcfBindingName = args[0];
      final String topicBindingName = args[1];
      final String username = args[2];
      final String password = args[3];


      final String configFile = args[4];

      if (configFile.endsWith(".xml")) {
         DOMConfigurator.configure(configFile);
      } else {
         PropertyConfigurator.configure(configFile);
      }

      new JMSSink(tcfBindingName, topicBindingName, username, password);

      final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
      // Loop until the word "exit" is typed
      System.out.println("Type \"exit\" to quit JMSSink.");
      while (true) {
         final String s = stdin.readLine();
         if ("exit".equalsIgnoreCase(s)) {
            System.out.println("Exiting. Kill the application if it does not exit "
                    + "due to daemon threads.");
            return;
         }
      }
   }


   public JMSSink(final String tcfBindingName, final String topicBindingName, final String username,
                  final String password) {

      try {
         final Context ctx = new InitialContext();
         final TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) lookup(ctx,
                 tcfBindingName);

         final TopicConnection topicConnection =
                 topicConnectionFactory.createTopicConnection(username,
                         password);
         topicConnection.start();

         final TopicSession topicSession = topicConnection.createTopicSession(false,
                 Session.AUTO_ACKNOWLEDGE);

         final Topic topic = (Topic) ctx.lookup(topicBindingName);

         final TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);

         topicSubscriber.setMessageListener(this);

      } catch (final Exception e) {
         logger.error("Could not read JMS message.", e);
      }
   }


   public final void onMessage(final Message message) {

      try {
         if (message instanceof ObjectMessage) {
            final ObjectMessage objectMessage = (ObjectMessage) message;
            final LoggingEvent event = (LoggingEvent) objectMessage.getObject();
            final Logger remoteLogger = Logger.getLogger(event.getLoggerName());
            remoteLogger.callAppenders(event);
         } else {
            logger.warn("Received message is of type " + message.getJMSType()
                    + ", was expecting ObjectMessage.");
         }
      } catch (final JMSException e) {
         logger.error("Exception thrown while processing incoming message.",
                 e);
      }
   }


   private static Object lookup(final Context ctx, final String name) throws NamingException {
      try {
         return ctx.lookup(name);
      } catch (final NameNotFoundException e) {
         logger.error("Could not find name [" + name + "].");
         throw e;
      }
   }


   static void usage(final String msg) {
      System.err.println(msg);
      System.err.println("Usage: java " + JMSSink.class.getName()
              + " TopicConnectionFactoryBindingName TopicBindingName username password configFile");
      System.exit(1);
   }
}
