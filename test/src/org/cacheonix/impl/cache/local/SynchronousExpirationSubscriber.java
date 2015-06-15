package org.cacheonix.impl.cache.local;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;

/**
 * A test expiration subscriber.
 */
final class SynchronousExpirationSubscriber implements EntryModifiedSubscriber {

   /**
    * An event accumulator.
    *
    * @see #notifyKeysUpdated(List)
    * @see #getEvents()
    */
   private final List<EntryModifiedEvent> events = new ArrayList<EntryModifiedEvent>(1);


   /**
    * {@inheritDoc}
    */
   public void notifyKeysUpdated(final List<EntryModifiedEvent> events) {

      this.events.addAll(events);
   }


   /**
    * {@inheritDoc}
    */
   public EntryModifiedNotificationMode getNotificationMode() {

      return EntryModifiedNotificationMode.SINGLE;
   }


   /**
    * {@inheritDoc}
    */
   public Set<EntryModifiedEventType> getModificationTypes() {

      return Collections.singleton(EntryModifiedEventType.EXPIRE);
   }


   /**
    * {@inheritDoc}
    */
   public List<EntryModifiedEventContentFlag> getEventContentFlags() {

      return Arrays.asList(EntryModifiedEventContentFlag.NEED_ALL);
   }


   /**
    * Returns a list of accumulated events.
    *
    * @return the list of accumulated events.
    */
   List<EntryModifiedEvent> getEvents() {

      return new ArrayList<EntryModifiedEvent>(events);
   }


   public String toString() {

      return "SynchronousExpirationSubscriber{" +
              "events=" + events +
              '}';
   }
}


