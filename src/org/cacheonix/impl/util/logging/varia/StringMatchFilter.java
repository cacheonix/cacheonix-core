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

import org.cacheonix.impl.util.logging.helpers.OptionConverter;
import org.cacheonix.impl.util.logging.spi.Filter;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * This is a very simple filter based on string matching.
 * <p/>
 * <p>The filter admits two options <b>StringToMatch</b> and <b>AcceptOnMatch</b>. If there is a match between the value
 * of the StringToMatch option and the message of the {@link LoggingEvent}, then the {@link #decide(LoggingEvent)}
 * method returns {@link Filter#ACCEPT} if the <b>AcceptOnMatch</b> option value is true, if it is <code>false</code>
 * then {@link Filter#DENY} is returned. If there is no match, {@link Filter#NEUTRAL} is returned.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.9.0
 */
public final class StringMatchFilter extends Filter {

   /**
    * @deprecated Options are now handled using the JavaBeans paradigm. This constant is not longer needed and will be
    *             removed in the <em>near</em> term.
    */
   public static final String STRING_TO_MATCH_OPTION = "StringToMatch";

   /**
    * @deprecated Options are now handled using the JavaBeans paradigm. This constant is not longer needed and will be
    *             removed in the <em>near</em> term.
    */
   public static final String ACCEPT_ON_MATCH_OPTION = "AcceptOnMatch";

   boolean acceptOnMatch = true;

   String stringToMatch = null;


   /**
    * @deprecated We now use JavaBeans introspection to configure components. Options strings are no longer needed.
    */
   public String[] getOptionStrings() {

      return new String[]{STRING_TO_MATCH_OPTION, ACCEPT_ON_MATCH_OPTION};
   }


   /**
    * @deprecated Use the setter method for the option directly instead of the generic <code>setOption</code> method.
    */
   public void setOption(final String key, final String value) {

      if (key.equalsIgnoreCase(STRING_TO_MATCH_OPTION)) {
         stringToMatch = value;
      } else if (key.equalsIgnoreCase(ACCEPT_ON_MATCH_OPTION)) {
         acceptOnMatch = OptionConverter.toBoolean(value, acceptOnMatch);
      }
   }


   public void setStringToMatch(final String s) {

      stringToMatch = s;
   }


   public String getStringToMatch() {

      return stringToMatch;
   }


   public void setAcceptOnMatch(final boolean acceptOnMatch) {

      this.acceptOnMatch = acceptOnMatch;
   }


   public boolean getAcceptOnMatch() {

      return acceptOnMatch;
   }


   /**
    * Returns {@link Filter#NEUTRAL} is there is no string match.
    */
   public int decide(final LoggingEvent event) {

      final String msg = event.getRenderedMessage();

      if (msg == null || stringToMatch == null) {
         return Filter.NEUTRAL;
      }


      if (msg.contains(stringToMatch)) { // we've got a match
         if (acceptOnMatch) {
            return Filter.ACCEPT;
         } else {
            return Filter.DENY;
         }
      } else {
         return Filter.NEUTRAL;
      }
   }
}
