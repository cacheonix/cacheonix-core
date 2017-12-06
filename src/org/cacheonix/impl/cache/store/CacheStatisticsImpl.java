/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.org/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.cache.store;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.cache.CacheStatistics;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Defines cache statistics.
 */
@SuppressWarnings("RedundantIfStatement")
public final class CacheStatisticsImpl implements CacheStatistics, Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheStatisticsImpl.class); // NOPMD

   /**
    * Read hit count for this cache.
    */
   private long readHitCount = 0L;

   /**
    * Read miss count for this cache.
    */
   private long readMissCount = 0L;

   /**
    * Write hit count for this cache.
    */
   private long writeHitCount = 0L;

   /**
    * Write miss count for this cache.
    */
   private long writeMissCount = 0L;

   /**
    * Number of elements on disk.
    */
   private long elementsOnDiskCount = 0L;


   public CacheStatisticsImpl(final long readHitCount, final long readMissCount, final long writeHitCount,
                              final long writeMissCount, final long elementsOnDiskCount) {

      this.readHitCount = readHitCount;
      this.readMissCount = readMissCount;
      this.writeHitCount = writeHitCount;
      this.writeMissCount = writeMissCount;
      this.elementsOnDiskCount = elementsOnDiskCount;
   }


   /**
    * Required by Wireable.
    */
   public CacheStatisticsImpl() { // NOPMD

   }


   /**
    * Returns hit count for this cache.
    *
    * @return hit count for this cache.
    */
   public long getReadHitCount() {

      return readHitCount;
   }


   /**
    * Increments hit count for this cache.
    */
   public void incrementReadHitCount() {

      ++readHitCount;
   }


   /**
    * Returns hit ratio for this cache.
    *
    * @return hit ratio for this cache.
    */
   public float getReadHitRatio() {

      final long total = getReadTotalCount();
      if (total == 0L) {
         return 0.0F;
      }
      return (float) readHitCount / (float) total;
   }


   /**
    * Return number of elements on disk.
    */
   public long getElementsOnDiskCount() {

      return elementsOnDiskCount;
   }


   public long getReadTotalCount() {

      return readHitCount + readMissCount;
   }


   /**
    * Returns miss count for this cache.
    *
    * @return miss count for this cache.
    */
   public long getReadMissCount() {

      return readMissCount;
   }


   /**
    * Increment miss count for this cache.
    */
   public void incrementReadMissCount() {

      ++readMissCount;
   }


   /**
    * Returns miss ratio for this cache.
    *
    * @return miss ratio for this cache.
    */
   public float getReadMissRatio() {

      final long total = getReadTotalCount();
      if (total == 0L) {
         return 0.0F;
      }
      return (float) readMissCount / (float) total;
   }


   /**
    * Returns hit count for this cache.
    *
    * @return hit count for this cache.
    */
   public long getWriteHitCount() {

      return writeHitCount;
   }


   /**
    * Increments hit count for this cache.
    */
   public void incrementWriteHitCount() {

      ++writeHitCount;
   }


   /**
    * Increments number of elements on disk.
    */
   public void incrementElementsOnDiskCount() {

      ++elementsOnDiskCount;
   }


   /**
    * Returns hit ratio for this cache.
    *
    * @return hit ratio for this cache.
    */
   public float getWriteHitRatio() {

      final long total = getWriteTotalCount();
      if (total == 0L) {
         return 0.0F;
      }
      return (float) writeHitCount / (float) total;
   }


   public long getWriteTotalCount() {

      return writeHitCount + writeMissCount;
   }


   /**
    * Returns miss count for this cache.
    *
    * @return miss count for this cache.
    */
   public long getWriteMissCount() {

      return writeMissCount;
   }


   /**
    * Increment miss count for this cache.
    */
   public void incrementWriteMissCount() {

      ++writeMissCount;
   }


   /**
    * Returns miss ratio for this cache.
    *
    * @return miss ratio for this cache.
    */
   public float getWriteMissRatio() {

      final long total = getWriteTotalCount();
      if (total == 0L) {
         return 0.0F;
      }
      return (float) writeMissCount / (float) total;
   }


   /**
    * Decrements size on disk.
    */
   public void decrementCountOnDisk() {

      elementsOnDiskCount--;
   }


   /**
    * Clears all statistics.
    */
   public void reset() {

      readHitCount = 0L;
      readMissCount = 0L;
      writeHitCount = 0L;
      writeMissCount = 0L;
      elementsOnDiskCount = 0L;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.writeLong(readHitCount);
      out.writeLong(readMissCount);
      out.writeLong(writeHitCount);
      out.writeLong(writeMissCount);
      out.writeLong(elementsOnDiskCount);
   }


   public void readWire(final DataInputStream in) throws IOException {

      readHitCount = in.readLong();
      readMissCount = in.readLong();
      writeHitCount = in.readLong();
      writeMissCount = in.readLong();
      elementsOnDiskCount = in.readLong();
   }


   public int getWireableType() {

      return TYPE_CACHE_STATISTICS;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final CacheStatisticsImpl that = (CacheStatisticsImpl) o;

      if (readHitCount != that.readHitCount) {
         return false;
      }
      if (readMissCount != that.readMissCount) {
         return false;
      }
      if (elementsOnDiskCount != that.elementsOnDiskCount) {
         return false;
      }
      if (writeHitCount != that.writeHitCount) {
         return false;
      }
      if (writeMissCount != that.writeMissCount) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = (int) (readHitCount ^ readHitCount >>> 32);
      result = 31 * result + (int) (readMissCount ^ readMissCount >>> 32);
      result = 31 * result + (int) (writeHitCount ^ writeHitCount >>> 32);
      result = 31 * result + (int) (writeMissCount ^ writeMissCount >>> 32);
      result = 31 * result + (int) (elementsOnDiskCount ^ elementsOnDiskCount >>> 32);
      return result;
   }


   public String toString() {

      return "CacheStatisticsImpl{" +
              "readHitCount=" + readHitCount +
              ", readMissCount=" + readMissCount +
              ", writeHitCount=" + writeHitCount +
              ", writeMissCount=" + writeMissCount +
              ", elementsOnDiskCount=" + elementsOnDiskCount +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new CacheStatisticsImpl();
      }
   }
}

