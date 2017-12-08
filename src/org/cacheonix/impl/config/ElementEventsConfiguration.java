package org.cacheonix.impl.config;

import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * A configuration for element events.
 *
 * @see EntryModifiedSubscriber
 */
public final class ElementEventsConfiguration extends DocumentReader {

   private ElementEventNotification notification = null;


   public ElementEventNotification getNotification() {

      return notification;
   }


   protected void readNode(final String nodeName, final Node childNode) {

   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("notification".equals(attributeName)) {

         notification = toNotification(attributeValue);
      }
   }


   @Override
   void postProcessRead() {

   }


   /**
    * Converts an attribute value to ElementEventNotification.
    *
    * @param attributeValue the attribute value to convert.
    * @return a instance of ElementEventNotification that matches the attributeValue.
    * @throws IllegalArgumentException if the attributeValue cannot be converted to ElementEventNotification
    */
   private static ElementEventNotification toNotification(final String attributeValue) throws IllegalArgumentException {

      if ("synchronous".equals(attributeValue) || "sync".equals(attributeValue)) {

         return ElementEventNotification.SYNCHRONOUS;
      } else if ("asynchronous".equals(attributeValue) || "async".equals(attributeValue)) {

         return ElementEventNotification.ASYNCHRONOUS;
      } else {

         throw new IllegalArgumentException("Unsupported event notification: " + attributeValue);
      }
   }


   void configureDefaults() {

      notification = ElementEventNotification.SYNCHRONOUS;
   }
}
