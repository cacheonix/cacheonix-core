package org.cacheonix.impl.configuration;

/**
 * A enumeration containing styles of element event notification.
 */
public final class ElementEventNotification {

   /**
    * Indicates that the notification about element events must be performed synchronously.
    */
   public static final ElementEventNotification SYNCHRONOUS = new ElementEventNotification(0, "Sync");

   /**
    * Indicates that the notification about element events must be performed asynchronously, in a separate event thread
    * provided by Cacheonix.
    */
   public static final ElementEventNotification ASYNCHRONOUS = new ElementEventNotification(1, "Async");

   /**
    * A type code.
    */
   private final int code;

   /**
    * A type name.
    */
   private final String name;


   private ElementEventNotification(final int code, final String name) {

      this.code = code;
      this.name = name;
   }


   @SuppressWarnings("RedundantIfStatement")
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ElementEventNotification that = (ElementEventNotification) o;

      if (code != that.code) {
         return false;
      }
      if (!name.equals(that.name)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = code;
      result = 31 * result + name.hashCode();
      return result;
   }


   public String toString() {

      return "ElementEventNotification{" +
              "code=" + code +
              ", name='" + name + '\'' +
              '}';
   }
}
