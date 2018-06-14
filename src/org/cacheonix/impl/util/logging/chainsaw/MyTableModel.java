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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.Priority;

/**
 * Represents a list of <code>EventDetails</code> objects that are sorted on logging time. Methods are provided to
 * filter the events that are visible.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 */
@SuppressWarnings("deprecation")
final class MyTableModel
        extends AbstractTableModel {

   /**
    * used to log messages *
    */
   private static final Logger LOG = Logger.getLogger(MyTableModel.class);

   /**
    * use the compare logging events *
    */
   private static final Comparator MY_COMP = new Comparator() {

      /**
       * @see Comparator *
       */
      public int compare(final Object aObj1, final Object aObj2) {

         if (aObj1 == null && aObj2 == null) {
            return 0; // treat as equal
         }

         if (aObj1 == null) {
            return -1; // null less than everything
         }

         if (aObj2 == null) {
            return 1; // think about it. :->
         }

         // will assume only have LoggingEvent
         final EventDetails le1 = (EventDetails) aObj1;
         final EventDetails le2 = (EventDetails) aObj2;

         if (le1.getTimeStamp() < le2.getTimeStamp()) {
            return 1;
         }
         // assume not two events are logged at exactly the same time
         return -1;
      }
   };

   private static final long serialVersionUID = 0L;

   /**
    * A helper constant used to typify conversation from a list to an array.
    */
   private static final EventDetails[] EVENT_DETAILS_ARRAY_TEMPLATE = new EventDetails[0];

   /**
    * Helper that actually processes incoming events.
    *
    * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
    */
   private final class Processor
           implements Runnable {

      /**
       * loops getting the events *
       */
      public final void run() {
         //noinspection InfiniteLoopStatement
         while (true) {
            try {
               Thread.sleep(1000L);
            } catch (final InterruptedException ignored) {
               // ignore
            }

            synchronized (mLock) {
               if (mPaused) {
                  continue;
               }

               boolean toHead = true; // were events added to head
               boolean needUpdate = false;
               for (final Object mPendingEvent : mPendingEvents) {
                  final EventDetails event = (EventDetails) mPendingEvent;
                  mAllEvents.add(event);
                  toHead = toHead && event.equals(mAllEvents.first());
                  needUpdate = needUpdate || matchFilter(event);
               }
               mPendingEvents.clear();

               if (needUpdate) {
                  updateFilteredEvents(toHead);
               }
            }
         }

      }
   }


   /**
    * names of the columns in the table *
    */
   private static final String[] COL_NAMES = {
           "Time", "Priority", "Trace", "Category", "NDC", "Message"};

   /**
    * definition of an empty list *
    */
   private static final EventDetails[] EMPTY_LIST = {};

   /**
    * used to format dates *
    */
   private final DateFormat DATE_FORMATTER =
           DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

   /**
    * the lock to control access *
    */
   private final Object mLock = new Object();

   /**
    * set of all logged events - not filtered *
    */
   private final SortedSet mAllEvents = new TreeSet(MY_COMP);

   /**
    * events that are visible after filtering *
    */
   private EventDetails[] mFilteredEvents = EMPTY_LIST;

   /**
    * list of events that are buffered for processing *
    */
   private final List mPendingEvents = new ArrayList(5);

   /**
    * indicates whether event collection is paused to the UI *
    */
   private boolean mPaused = false;

   /**
    * filter for the thread *
    */
   private String mThreadFilter = "";

   /**
    * filter for the message *
    */
   private String mMessageFilter = "";

   /**
    * filter for the NDC *
    */
   private String mNDCFilter = "";

   /**
    * filter for the category *
    */
   private String mCategoryFilter = "";

   /**
    * filter for the priority *
    */
   private Priority mPriorityFilter = Priority.DEBUG;


   /**
    * Creates a new <code>MyTableModel</code> instance.
    */
   MyTableModel() {

      final Thread t = new Thread(new Processor());
      t.setDaemon(true);
      t.start();
   }

   ////////////////////////////////////////////////////////////////////////////
   // Table Methods
   ////////////////////////////////////////////////////////////////////////////


   /**
    * @see TableModel *
    */
   public final int getRowCount() {

      synchronized (mLock) {
         return mFilteredEvents.length;
      }
   }


   /**
    * @see TableModel *
    */
   public final int getColumnCount() {
      // does not need to be synchronized
      return COL_NAMES.length;
   }


   /**
    * @see TableModel *
    */
   public final String getColumnName(final int aCol) {
      // does not need to be synchronized
      return COL_NAMES[aCol];
   }


   /**
    * @see TableModel *
    */
   public final Class getColumnClass(final int aCol) {
      // does not need to be synchronized
      return aCol == 2 ? Boolean.class : Object.class;
   }


   /**
    * @see TableModel *
    */
   public final Object getValueAt(final int aRow, final int aCol) {

      synchronized (mLock) {
         final EventDetails event = mFilteredEvents[aRow];

         if (aCol == 0) {
            return DATE_FORMATTER.format(new Date(event.getTimeStamp()));
         }

         if (aCol == 1) {
            return event.getPriority();
         }

         if (aCol == 2) {
            return event.getThrowableStrRep() == null
                    ? Boolean.FALSE : Boolean.TRUE;
         }

         if (aCol == 3) {
            return event.getCategoryName();
         }

         if (aCol == 4) {
            return event.getNDC();
         }
         return event.getMessage();
      }
   }

   ////////////////////////////////////////////////////////////////////////////
   // Public Methods
   ////////////////////////////////////////////////////////////////////////////


   /**
    * Sets the priority to filter events on. Only events of equal or higher property are now displayed.
    *
    * @param aPriority the priority to filter on
    */
   public final void setPriorityFilter(final Priority aPriority) {

      synchronized (mLock) {
         mPriorityFilter = aPriority;
         updateFilteredEvents(false);
      }
   }


   /**
    * Set the filter for the thread field.
    *
    * @param aStr the string to match
    */
   public final void setThreadFilter(final String aStr) {

      synchronized (mLock) {
         mThreadFilter = aStr.trim();
         updateFilteredEvents(false);
      }
   }


   /**
    * Set the filter for the message field.
    *
    * @param aStr the string to match
    */
   public final void setMessageFilter(final String aStr) {

      synchronized (mLock) {
         mMessageFilter = aStr.trim();
         updateFilteredEvents(false);
      }
   }


   /**
    * Set the filter for the NDC field.
    *
    * @param aStr the string to match
    */
   public final void setNDCFilter(final String aStr) {

      synchronized (mLock) {
         mNDCFilter = aStr.trim();
         updateFilteredEvents(false);
      }
   }


   /**
    * Set the filter for the category field.
    *
    * @param aStr the string to match
    */
   public final void setCategoryFilter(final String aStr) {

      synchronized (mLock) {
         mCategoryFilter = aStr.trim();
         updateFilteredEvents(false);
      }
   }


   /**
    * Add an event to the list.
    *
    * @param aEvent a <code>EventDetails</code> value
    */
   public final void addEvent(final EventDetails aEvent) {

      synchronized (mLock) {
         mPendingEvents.add(aEvent);
      }
   }


   /**
    * Clear the list of all events.
    */
   public final void clear() {

      synchronized (mLock) {
         mAllEvents.clear();
         mFilteredEvents = EVENT_DETAILS_ARRAY_TEMPLATE;
         mPendingEvents.clear();
         fireTableDataChanged();
      }
   }


   /**
    * Toggle whether collecting events *
    */
   public final void toggle() {

      synchronized (mLock) {
         mPaused = !mPaused;
      }
   }


   /**
    * @return whether currently paused collecting events *
    */
   public final boolean isPaused() {

      synchronized (mLock) {
         return mPaused;
      }
   }


   /**
    * Get the throwable information at a specified row in the filtered events.
    *
    * @param aRow the row index of the event
    * @return the throwable information
    */
   public final EventDetails getEventDetails(final int aRow) {

      synchronized (mLock) {
         return mFilteredEvents[aRow];
      }
   }

   ////////////////////////////////////////////////////////////////////////////
   // Private methods
   ////////////////////////////////////////////////////////////////////////////


   /**
    * Update the filtered events data structure.
    *
    * @param aInsertedToFront indicates whether events were added to front of the events. If true, then the current
    *                         first event must still exist in the list after the filter is applied.
    */
   private void updateFilteredEvents(final boolean aInsertedToFront) {

      final long start = System.currentTimeMillis();
      final int size = mAllEvents.size();
      final List filtered = new ArrayList(size);

      for (final Object mAllEvent : mAllEvents) {
         final EventDetails event = (EventDetails) mAllEvent;
         if (matchFilter(event)) {
            filtered.add(event);
         }
      }

      final EventDetails lastFirst = mFilteredEvents.length == 0
              ? null
              : mFilteredEvents[0];
      mFilteredEvents = (EventDetails[]) filtered.toArray(EVENT_DETAILS_ARRAY_TEMPLATE);

      if (aInsertedToFront && lastFirst != null) {
         final int index = filtered.indexOf(lastFirst);
         if (index < 1) {
            LOG.warn("In strange state");
            fireTableDataChanged();
         } else {
            fireTableRowsInserted(0, index - 1);
         }
      } else {
         fireTableDataChanged();
      }

      final long end = System.currentTimeMillis();
      LOG.debug("Total time [ms]: " + (end - start)
              + " in update, size: " + size);
   }


   /**
    * Returns whether an event matches the filters.
    *
    * @param aEvent the event to check for a match
    * @return whether the event matches
    */
   private boolean matchFilter(final EventDetails aEvent) {

      if (aEvent.getPriority().isGreaterOrEqual(mPriorityFilter) &&
              aEvent.getThreadName().contains(mThreadFilter) &&
              aEvent.getCategoryName().contains(mCategoryFilter) &&
              (mNDCFilter.isEmpty() ||
                      aEvent.getNDC() != null && aEvent.getNDC().contains(mNDCFilter))) {
         final String rm = aEvent.getMessage();
         if (rm == null) {
            // only match if we have not filtering in place
            return mMessageFilter.isEmpty();
         } else {
            return rm.contains(mMessageFilter);
         }
      }

      return false; // by default not match
   }
}
