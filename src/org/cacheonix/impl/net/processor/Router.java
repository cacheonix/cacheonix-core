package org.cacheonix.impl.net.processor;

/**
 * Created by vimeshev on 11/3/18.
 */
public interface Router {

   void setOutput(Processor output);

   void setClusterUUID(UUID clusterUUID);

   void register(ProcessorKey processorKey, RequestProcessor processor);

   void unregister(ProcessorKey processorKey);

   /**
    * When this method is called, routing-wise the message can be in one of the states:
    * <p/>
    * 1. Fully routed: It is a point-to-point message and the destination is set.
    * <p/>
    * 2. It is a multicast message.
    * <p/>
    * 3. It is a point-to-point message and it is not routed.
    *
    * @param message message to route.
    * @return returns a waiter for this message.
    */
   ResponseWaiter route(Message message);
}
