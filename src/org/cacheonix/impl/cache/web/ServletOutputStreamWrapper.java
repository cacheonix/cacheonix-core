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
package org.cacheonix.impl.cache.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;

/**
 * A wrapper for {@link ServletOutputStream} to create a byte copy of the data written to the ServletOutputStream.
 */
final class ServletOutputStreamWrapper extends ServletOutputStream {

   private final ServletOutputStream delegate;

   private final ByteArrayOutputStream baos;


   ServletOutputStreamWrapper(final ServletOutputStream delegate, final int bufferSize) {

      this.baos = new ByteArrayOutputStream(bufferSize);
      this.delegate = delegate;
   }


   public void write(final int value) throws IOException {

      delegate.write(value);
      baos.write(value);
   }


   byte[] getByteOutput() {

      return baos.toByteArray();
   }


   @SuppressWarnings("ObjectToString")
   public String toString() {

      return "ServletOutputStreamWrapper{" +
              "delegate=" + delegate +
              ", baos=" + baos +
              "} " + super.toString();
   }
}
