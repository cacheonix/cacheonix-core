package org.cacheonix.impl;

import org.cacheonix.impl.cache.distributed.partitioned.AssignBucketCommand;
import org.cacheonix.impl.cache.distributed.partitioned.AssignBucketMessage;
import org.cacheonix.impl.cache.distributed.partitioned.BeginBucketTransferCommand;
import org.cacheonix.impl.cache.distributed.partitioned.BeginBucketTransferMessage;
import org.cacheonix.impl.cache.distributed.partitioned.BucketEventListener;
import org.cacheonix.impl.cache.distributed.partitioned.BucketTransferRejectedAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.CancelBucketTransferCommand;
import org.cacheonix.impl.cache.distributed.partitioned.CancelBucketTransferMessage;
import org.cacheonix.impl.cache.distributed.partitioned.FinishBucketTransferCommand;
import org.cacheonix.impl.cache.distributed.partitioned.FinishBucketTransferMessage;
import org.cacheonix.impl.cache.distributed.partitioned.OrphanBucketCommand;
import org.cacheonix.impl.cache.distributed.partitioned.OrphanBucketMessage;
import org.cacheonix.impl.cache.distributed.partitioned.RestoreBucketCommand;
import org.cacheonix.impl.cache.distributed.partitioned.RestoreBucketMessage;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.processor.Message;

/**
 * Receives bucket events and puts them to the serializer queue.
 */
final class BucketEventDispatcher implements BucketEventListener {

   /**
    * The network
    */
   private final ClusterProcessor clusterProcessor;

   /**
    * The address of this cluster node.
    */
   private final ClusterNodeAddress nodeAddress;


   /**
    * Creates a dispatcher of bucket commands received by this cluster node.
    *
    * @param nodeAddress      the address of this cluster node.
    * @param clusterProcessor the network.
    */
   BucketEventDispatcher(final ClusterNodeAddress nodeAddress, final ClusterProcessor clusterProcessor) {

      this.clusterProcessor = clusterProcessor;
      this.nodeAddress = nodeAddress;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation converts the commands to <code>TransferBucketRequest</code> and posts the message to the
    * execution queue.
    *
    * @see BeginBucketTransferMessage
    */
   public void execute(final BeginBucketTransferCommand command) {

      if (nodeAddress.equals(command.getCurrentOwner())) {

         final BeginBucketTransferMessage message = new BeginBucketTransferMessage(command.getCacheName());

         message.setBucketNumbers(command.getBucketNumbers());
         message.setSourceStorageNumber(command.getSourceStorageNumber());
         message.setDestinationStorageNumber(command.getDestinationStorageNumber());
         message.setCurrentOwner(command.getCurrentOwner());
         message.setNewOwner(command.getNewOwner());
         message.setReceiver(nodeAddress);
         clusterProcessor.post(message);
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation posts FinishBucketTransferMessage to the execution queue.
    */
   public void execute(final FinishBucketTransferCommand command) {

      // Check if we are a previous owner. A previous owner holds the bucket and the transfer lock.
      final ClusterNodeAddress previousOwner = command.getPreviousOwner();
      final ClusterNodeAddress newOwner = command.getNewOwner();
      if (!nodeAddress.equals(previousOwner) && !nodeAddress.equals(newOwner)) {
         return;
      }

      // Create message
      final FinishBucketTransferMessage message = new FinishBucketTransferMessage(command.getCacheName());
      message.getBucketNumbers().addAll(command.getBucketNumbers());

      message.setSourceStorageNumber(command.getSourceStorageNumber());
      message.setDestinationStorageNumber(command.getDestinationStorageNumber());
      message.setNewOwner(newOwner);
      message.setPreviousOwner(previousOwner);
      message.setReceiver(nodeAddress);

      // Post
      clusterProcessor.post(message);
   }


   /**
    * This command comes as a result of the replicated state executing <code>BucketTransferRejectedAnnouncement</code>.
    * Also, it can be sent to cancel pending transfers as a result of the replicated processing a notification about
    * node leaving the group.
    *
    * @param command command
    * @see BucketTransferRejectedAnnouncement
    */
   public void execute(final CancelBucketTransferCommand command) {

      // Check if we are a previous owner. A previous owner holds the bucket and the transfer lock.
      final ClusterNodeAddress previousOwnerAddress = command.getPreviousOwner();
      final ClusterNodeAddress newOwnerAddress = command.getNewOwner();

      if (nodeAddress.equals(previousOwnerAddress) || nodeAddress.equals(newOwnerAddress)) {

         final CancelBucketTransferMessage message = new CancelBucketTransferMessage(command.getCacheName());

         message.setBucketNumbers(command.getBucketNumbers());
         message.setSourceStorageNumber(command.getSourceStorageNumber());
         message.setDestinationStorageNumber(command.getDestinationStorageNumber());
         message.setPreviousOwner(previousOwnerAddress);
         message.setNewOwner(newOwnerAddress);
         message.setReceiver(nodeAddress);
         clusterProcessor.post(message);
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation converts the commands to a local <code>RestoreBucketMessage</code> and posts it to the async
    * execution queue.
    *
    * @see RestoreBucketMessage
    */
   public void execute(final RestoreBucketCommand command) {

      // Check if ours
      if (nodeAddress.equals(command.getAddress())) {
         // Create async message
         final Message message = new RestoreBucketMessage(command.getCacheName(),
                 command.getBucketNumbers(), command.getFromStorageNumber(), nodeAddress);

         clusterProcessor.post(message);
      }
   }


   public void execute(final OrphanBucketCommand command) {

      if (!nodeAddress.equals(command.getOwnerAddress())) {

         return;
      }

      //
      final Message message = new OrphanBucketMessage(command.getCacheName(), command.getStorageNumber(),
              command.getBucketNumber());

      message.setReceiver(nodeAddress);
      clusterProcessor.post(message);
   }


   public void execute(final AssignBucketCommand command) {

      if (!nodeAddress.equals(command.getOwnerAddress())) {

         return;
      }

      //
      final Message message = new AssignBucketMessage(command.getCacheName(), command.getStorageNumber(),
              command.getBucketNumber());

      message.setReceiver(nodeAddress);
      clusterProcessor.post(message);
   }
}
