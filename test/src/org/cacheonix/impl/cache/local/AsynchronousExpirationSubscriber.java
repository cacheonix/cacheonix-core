package org.cacheonix.impl.cache.local;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;

/**
 * A test expiration subscriber.
 */
final class AsynchronousExpirationSubscriber implements EntryModifiedSubscriber {

   /**
    * An event accumulator.
    *
    * @see #notifyKeysUpdated(List)
    * @see #getEvents()
    */
   private final List<EntryModifiedEvent> events = new ArrayList<EntryModifiedEvent>(1);

   private final Lock lock = new ReentrantLock();

   private final Condition eventsAvailable = lock.newCondition();


   /**
    * {@inheritDoc}
    */
   public void notifyKeysUpdated(final List<EntryModifiedEvent> events) {

      lock.lock();
      try {

         this.events.addAll(events);
         eventsAvailable.signalAll();
      } finally {

         lock.unlock();
      }
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

      return Collections.singletonList(EntryModifiedEventContentFlag.NEED_ALL);
   }


   /**
    * Returns a list of accumulated events.
    *
    * @return the list of accumulated events.
    */
   List<EntryModifiedEvent> getEvents() throws InterruptedException {

      lock.lock();
      try {

         while (events.isEmpty()) {

            eventsAvailable.await();
         }

         return new ArrayList<EntryModifiedEvent>(events);
      } finally {

         lock.unlock();
      }
   }


   public String toString() {

      return "AsynchronousExpirationSubscriber{" +
              "events=" + events +
              '}';
   }
}


