/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
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
package com.impl.examples.cache.invalidator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Date;


/**
 * Invoice object.
 * <p/>
 * This class also demonstrates how to implement {@link Externalizable}. See {@link #writeExternal(ObjectOutput)} and
 * {@link #readExternal(ObjectInput)} for details.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public final class Invoice implements Externalizable {

   private Date date = null;

   private int id;

   private int timeStamp;

   private int number = 0;


   public Invoice(final int id, final Date date, final int timeStamp) {

      this.id = id;
      this.date = (Date) date.clone();
      this.timeStamp = timeStamp;
   }


   /**
    * No-argument constructor required by {@link Externalizable}.
    */
   public Invoice() {

   }


   public int getTimeStamp() {

      return timeStamp;
   }


   public int getId() {

      return id;
   }


   /**
    * Returns invoice date.
    *
    * @return invoice date.
    */
   public Date getDate() {

      return (Date) date.clone();
   }


   /**
    * Sets invoice date.
    *
    * @param date date to set.
    */
   public void setDate(final Date date) {

      this.date = (Date) date.clone();
   }


   /**
    * Increments update time stamp.
    */
   public void incrementTimeStamp() {

      timeStamp++;
   }


   /**
    * Returns invoice number.
    *
    * @return invoice number.
    */
   public int getNumber() {

      return number;
   }


   /**
    * Sets invoice number.
    *
    * @param number invoice number.
    */
   public void setNumber(final int number) {

      this.number = number;
   }


   /**
    * {@inheritDoc}
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.writeObject(date);
      out.writeInt(id);
      out.writeInt(timeStamp);
      out.writeInt(number);
   }


   /**
    * {@inheritDoc}
    */
   public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

      date = (Date) in.readObject();
      id = in.readInt();
      timeStamp = in.readInt();
      number = in.readInt();
   }


   public String toString() {

      return "Invoice{" +
              "date=" + date +
              ", id=" + id +
              ", timeStamp=" + timeStamp +
              ", number=" + number +
              '}';
   }
}
