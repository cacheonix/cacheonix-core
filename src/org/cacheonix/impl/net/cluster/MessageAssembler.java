package org.cacheonix.impl.net.cluster;

import java.io.IOException;
import java.util.List;

import org.cacheonix.impl.net.processor.Frame;

/**
 * Delivery queue is a queue that receives message parts and assembles a whole message.
 * <p/>
 * Once the whole message is assembled, the queue pushes it out by calling a listener.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 30, 2008 5:45:43 PM
 */
interface MessageAssembler {

   /**
    * Adds a multicast packet to the end of the assembler.
    *
    * @param frame, possible partial.
    * @throws IOException if there was a problem adding a frame.
    */
   void add(Frame frame) throws IOException;

   /**
    * Retrieves and removes the head of this queue, or returns <tt>null</tt> if this queue is empty.
    *
    * @return the head of this queue, or <tt>null</tt> if this queue is empty
    */
   AssembledMessage poll();

   /**
    * Clears the queue.
    */
   void clear();

   /**
    * Returns an unmodifiable list of frames present in the message assembler.
    *
    * @return frames present in the message assembler.
    */
   List<Frame> getParts();

   /**
    * Sets an initial list of frames present in the message assembler.
    *
    * @param messageAssemblerParts the initial list of frames present in the message assembler.
    */
   void setParts(List<Frame> messageAssemblerParts);
}
