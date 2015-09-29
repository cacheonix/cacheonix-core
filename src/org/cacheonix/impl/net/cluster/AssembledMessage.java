package org.cacheonix.impl.net.cluster;

import org.cacheonix.impl.net.processor.Message;

/**
 * An immutable holder of an assembled message and metadata characterising the assembly process.
 */
interface AssembledMessage {

   Message getMessage();

   long getStartFrameNumber();
}
