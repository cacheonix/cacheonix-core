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

package org.cacheonix.impl.util.logging.spi;

import org.cacheonix.impl.util.logging.AppenderSkeleton;
import org.cacheonix.impl.util.logging.PropertyConfigurator;
import org.cacheonix.impl.util.logging.xml.DOMConfigurator;


/**
 * Users should extend this class to implement customized logging event filtering. Note that {@link Category} and {@link
 * AppenderSkeleton}, the parent class of all standard appenders, have built-in filtering rules. It is suggested that
 * you first use and understand the built-in rules before rushing to write your own custom filters.
 * <p/>
 * <p>This abstract class assumes and also imposes that filters be organized in a linear chain. The {@link #decide
 * decide(LoggingEvent)} method of each filter is called sequentially, in the order of their addition to the chain.
 * <p/>
 * <p>The {@link #decide decide(LoggingEvent)} method must return one of the integer constants {@link #DENY}, {@link
 * #NEUTRAL} or {@link #ACCEPT}.
 * <p/>
 * <p>If the value {@link #DENY} is returned, then the log event is dropped immediately without consulting with the
 * remaining filters.
 * <p/>
 * <p>If the value {@link #NEUTRAL} is returned, then the next filter in the chain is consulted. If there are no more
 * filters in the chain, then the log event is logged. Thus, in the presence of no filters, the default behaviour is to
 * log all logging events.
 * <p/>
 * <p>If the value {@link #ACCEPT} is returned, then the log event is logged without consulting the remaining filters.
 * <p/>
 * <p>The philosophy of log4j filters is largely inspired from the Linux ipchains.
 * <p/>
 * <p>Note that filtering is only supported by the {@link DOMConfigurator DOMConfigurator}. The {@link
 * PropertyConfigurator PropertyConfigurator} does not support filters.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.9.0
 */
public abstract class Filter implements OptionHandler {

   /**
    * Points to the next filter in the filter chain.
    */
   public Filter next = null;

   /**
    * The log event must be dropped immediately without consulting with the remaining filters, if any, in the chain.
    */
   public static final int DENY = -1;

   /**
    * This filter is neutral with respect to the log event. The remaining filters, if any, should be consulted for a
    * final decision.
    */
   public static final int NEUTRAL = 0;

   /**
    * The log event must be logged immediately without consulting with the remaining filters, if any, in the chain.
    */
   public static final int ACCEPT = 1;


   /**
    * Usually filters options become active when set. We provide a default do-nothing implementation for convenience.
    */
   public void activateOptions() {
   }


   /**
    * <p>If the decision is <code>DENY</code>, then the event will be dropped. If the decision is <code>NEUTRAL</code>,
    * then the next filter, if any, will be invoked. If the decision is ACCEPT then the event will be logged without
    * consulting with other filters in the chain.
    *
    * @param event The LoggingEvent to decide upon.
    * @return decision The decision of the filter.
    */
   public abstract int decide(LoggingEvent event);


   /**
    * Set the next filter pointer.
    */
   public final void setNext(final Filter next) {
      this.next = next;
   }


   /**
    * Return the pointer to the next filter;
    */
   public final Filter getNext() {
      return next;
   }

}
