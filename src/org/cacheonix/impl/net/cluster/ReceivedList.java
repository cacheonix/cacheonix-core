package org.cacheonix.impl.net.cluster;

import org.cacheonix.impl.net.processor.Frame;

/**
 * Created by simeshev on 3/16/16.
 */
public interface ReceivedList {

   /**
    * Adds a packet to the receive list.
    * <p/>
    * The received list may contain gaps.
    *
    * @param frame the frame to add.
    */
   void add(Frame frame);

   /**
    * Highest continuous sequence number received by this protocol stub, or Ri
    *
    * @return continuous sequence number received by this protocol stub, or Ri
    */
   Long getHighestContinuousNumberReceived();

   /**
    * Returns a highest sequence number received, or L/highSeqNum.
    *
    * @return the highest sequence number received, or L/highSeqNum.
    */
   Long getHighestSequenceNumberReceived();

   /**
    * Returns a message with a given number or null if not present.
    *
    * @param messageSequenceNumber a number to get.
    * @return the message with a given number or null if not present.
    */
   Frame getMessage(long messageSequenceNumber);

   /**
    * Takes a continuous message from the end of the queue.
    * <p/>
    * Return <code>null</code> if does not exist ot this is not a continuous item.
    *
    * @param messageNumToDeliver a message number to get.
    * @return the message with a given number or <code>null</code> if does not exist ot this is not a continuous item.
    */
   Frame poll(long messageNumToDeliver);

   /**
    * Sets the highest sequence number received.
    *
    * @param sequenceNumber the highest sequence number received.
    * @see MulticastMarker#processBlocked()
    */
   void setHighestContinuousNumberReceived(Long sequenceNumber);

   void setHighestSequenceNumberReceived(Long sequenceNumber);

   /**
    * Returns the smallest number received but undelivered.
    *
    * @return the smallest number received but undelivered or <code>null</code> if there are no undelivered messages.
    */
   Long getSmallestNumberReceivedButNotDelivered();

   /**
    * Returns the highest continuous number that is received but undelivered.
    * <p/>
    * This number is such that all messages between smallest number received but undelivered and this number are also
    * received.
    *
    * @return the highest continuous number that is received but undelivered.
    */
   Long getHighestContinuousNumberReceivedButNotDelivered();

   /**
    * Returns the highest number received but not delivered or <code>null</code> if received list is empty.
    *
    * @return the highest number received but not delivered or <code>null</code> if received list is empty.
    */
   Long getHighestNumberReceivedButNotDelivered();

   /**
    * Returns <code>true</code> if this received list does not have undelivered messages.
    *
    * @return <code>true</code> if this received list does not have undelivered messages.
    */
   boolean isEmpty();

   void clear();
}
