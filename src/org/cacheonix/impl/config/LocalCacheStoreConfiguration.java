package org.cacheonix.impl.config;

import org.w3c.dom.Node;

/**
 * A configuration of a local cache store.
 */
public class LocalCacheStoreConfiguration extends CacheStoreConfiguration {

   private ElementEventsConfiguration elementEvents = null;


   public ElementEventsConfiguration getElementEvents() {

      return elementEvents;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("elementEvents".equals(nodeName)) {

         elementEvents = new ElementEventsConfiguration();
         elementEvents.read(childNode);
      } else {

         super.readNode(nodeName, childNode);
      }
   }


   protected void postProcessRead() {

      super.postProcessRead();

      // Set replication to no replication
      if (elementEvents == null) {

         elementEvents = new ElementEventsConfiguration();
         elementEvents.configureDefaults();
      }
   }
}
