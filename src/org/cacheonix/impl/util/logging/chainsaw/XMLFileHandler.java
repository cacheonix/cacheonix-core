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

import java.util.StringTokenizer;

import org.cacheonix.impl.util.logging.Level;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A content handler for document containing Log4J events logged using the XMLLayout class. It will create events and
 * add them to a supplied model.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 * @version 1.0
 */
final class XMLFileHandler
        extends DefaultHandler {

   /**
    * represents the event tag *
    */
   private static final String TAG_EVENT = "log4j:event";

   /**
    * represents the message tag *
    */
   private static final String TAG_MESSAGE = "log4j:message";

   /**
    * represents the ndc tag *
    */
   private static final String TAG_NDC = "log4j:NDC";

   /**
    * represents the throwable tag *
    */
   private static final String TAG_THROWABLE = "log4j:throwable";

   /**
    * represents the location info tag *
    */
   private static final String TAG_LOCATION_INFO = "log4j:locationInfo";

   /**
    * where to put the events *
    */
   private final MyTableModel mModel;

   /**
    * the number of events in the document *
    */
   private int mNumEvents = 0;

   /**
    * the time of the event *
    */
   private long mTimeStamp = 0L;

   /**
    * the priority (level) of the event *
    */
   private Level mLevel = null;

   /**
    * the category of the event *
    */
   private String mCategoryName = null;

   /**
    * the NDC for the event *
    */
   private String mNDC = null;

   /**
    * the thread for the event *
    */
   private String mThreadName = null;

   /**
    * the msg for the event *
    */
   private String mMessage = null;

   /**
    * the throwable details the event *
    */
   private String[] mThrowableStrRep = null;

   /**
    * the location details for the event *
    */
   private String mLocationDetails = null;

   /**
    * buffer for collecting text *
    */
   private final StringBuffer mBuf = new StringBuffer(100);


   /**
    * Creates a new <code>XMLFileHandler</code> instance.
    *
    * @param aModel where to add the events
    */
   XMLFileHandler(final MyTableModel aModel) {

      mModel = aModel;
   }


   /**
    * @see DefaultHandler *
    */
   public final void startDocument() {

      mNumEvents = 0;
   }


   /**
    * @see DefaultHandler *
    */
   public final void characters(final char[] aChars, final int aStart, final int aLength) {

      mBuf.append(String.valueOf(aChars, aStart, aLength));
   }


   /**
    * @see DefaultHandler *
    */
   public final void endElement(final String aNamespaceURI,
                                final String aLocalName,
                                final String aQName) {

      if (TAG_EVENT.equals(aQName)) {
         addEvent();
         resetData();
      } else if (TAG_NDC.equals(aQName)) {
         mNDC = mBuf.toString();
      } else if (TAG_MESSAGE.equals(aQName)) {
         mMessage = mBuf.toString();
      } else if (TAG_THROWABLE.equals(aQName)) {
         final StringTokenizer st =
                 new StringTokenizer(mBuf.toString(), "\n\t");
         mThrowableStrRep = new String[st.countTokens()];
         if (mThrowableStrRep.length > 0) {
            mThrowableStrRep[0] = st.nextToken();
            for (int i = 1; i < mThrowableStrRep.length; i++) {
               mThrowableStrRep[i] = '\t' + st.nextToken();
            }
         }
      }
   }


   /**
    * @see DefaultHandler *
    */
   public final void startElement(final String aNamespaceURI,
                                  final String aLocalName,
                                  final String aQName,
                                  final Attributes aAtts) {

      mBuf.setLength(0);

      if (TAG_EVENT.equals(aQName)) {
         mThreadName = aAtts.getValue("thread");
         mTimeStamp = Long.parseLong(aAtts.getValue("timestamp"));
         mCategoryName = aAtts.getValue("logger");
         mLevel = Level.toLevel(aAtts.getValue("level"));
      } else if (TAG_LOCATION_INFO.equals(aQName)) {
         mLocationDetails = aAtts.getValue("class") + '.'
                 + aAtts.getValue("method")
                 + '(' + aAtts.getValue("file") + ':' + aAtts.getValue("line")
                 + ')';
      }
   }


   /**
    * @return the number of events in the document *
    */
   final int getNumEvents() {

      return mNumEvents;
   }

   ////////////////////////////////////////////////////////////////////////////
   // Private methods
   ////////////////////////////////////////////////////////////////////////////


   /**
    * Add an event to the model *
    */
   private void addEvent() {

      mModel.addEvent(new EventDetails(mTimeStamp,
              mLevel,
              mCategoryName,
              mNDC,
              mThreadName,
              mMessage,
              mThrowableStrRep,
              mLocationDetails));
      mNumEvents++;
   }


   /**
    * Reset the data for an event *
    */
   private void resetData() {

      mTimeStamp = 0L;
      mLevel = null;
      mCategoryName = null;
      mNDC = null;
      mThreadName = null;
      mMessage = null;
      mThrowableStrRep = null;
      mLocationDetails = null;
   }
}
