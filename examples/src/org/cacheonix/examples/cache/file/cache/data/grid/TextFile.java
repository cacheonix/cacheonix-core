/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.examples.cache.file.cache.data.grid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A value object containing a cached file and file attributes.
 */
public final class TextFile implements Externalizable {

   /**
    * A time when the file was modified last time.
    */
   private long lastModified;

   /**
    * A content of the text file.
    */
   private String content;


   public TextFile() {

   }


   /**
    * Returns file's last modification time stamp.
    *
    * @return file's last modification time stamp.
    */
   public long getLastModified() {

      return lastModified;
   }


   /**
    * Returns file's content.
    *
    * @return file's content.
    */
   public String getContent() {

      return content;
   }


   /**
    * Sets the content of the file.
    *
    * @param content file's content.
    */
   public void setContent(final String content) {

      this.content = content;
   }


   /**
    * Sets file's last modification time stamp.
    *
    * @param lastModified file's last modification time stamp.
    */
   public void setLastModified(final long lastModified) {

      this.lastModified = lastModified;
   }


   /**
    * Saves this object's content by calling the methods of DataOutput.
    *
    * @param out the stream to write the object to
    * @throws IOException Includes any I/O exceptions that may occur
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.writeLong(lastModified);
      out.writeUTF(content);
   }


   /**
    * Restore this object contents by calling the methods of DataInput.  The readExternal method must read the values in
    * the same sequence and with the same types as were written by {@link #writeExternal(ObjectOutput)} .
    *
    * @param in the stream to read data from in order to restore the object
    * @throws IOException if I/O errors occur
    */
   public void readExternal(final ObjectInput in) throws IOException {

      lastModified = in.readLong();
      content = in.readUTF();
   }


   public String toString() {

      return "TextFile{" +
              "lastModified=" + lastModified +
              ", content='" + content + '\'' +
              '}';
   }
}
