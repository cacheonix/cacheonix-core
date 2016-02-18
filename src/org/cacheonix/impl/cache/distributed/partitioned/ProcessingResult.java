package org.cacheonix.impl.cache.distributed.partitioned;


import org.cacheonix.impl.cache.item.Binary;

/**
 * The object holding result of the call to {@link KeyRequest#processKey(Bucket, Binary)}.
 */
final class ProcessingResult {

   /**
    * The result of execution.
    */
   private Object result = null;

   /**
    * Numbers of modified buckets or null or empty.
    */
   private Binary modifiedKey = null;


   /**
    * Creates new result.
    *
    * @param result      the result of execution.
    * @param modifiedKey a modified key, can be null if no modifications made.
    */
   ProcessingResult(final Object result, final Binary modifiedKey) {

      this.result = result;
      this.modifiedKey = modifiedKey;
   }


   /**
    * Returns true if has a non-null, non-empty set of modified bucket numbers.
    *
    * @return true if has a non-null, non-empty set of modified bucket numbers.
    */
   boolean hasModifiedKey() {

      return modifiedKey != null;
   }


   /**
    * Returns the execution result.
    *
    * @return execution result.
    */
   Object getResult() {

      return result;
   }


   /**
    * Returns a modified key or null if the key hasn't been modified.
    *
    * @return the modified key or null if the key hasn't been modified.
    */
   Binary getModifiedKey() {

      return modifiedKey;
   }


   public String toString() {

      return "Result{" +
              "modifiedKey=" + modifiedKey +
              ", result=" + result +
              '}';
   }
}
