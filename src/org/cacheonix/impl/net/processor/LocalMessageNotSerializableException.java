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
package org.cacheonix.impl.net.processor;

import java.io.NotSerializableException;

/**
 * An exception that may be thrown by writeWire() and readWire() if the message is not supposed to be sent outside
 * (local).
 */
public final class LocalMessageNotSerializableException extends NotSerializableException {

   static final String MESSAGE = "Local message is not allowed to travel across the wire";


   /**
    * Creates <code>LocalMessageNotSerializableException</code>.
    */
   public LocalMessageNotSerializableException() {

      super(MESSAGE);
   }
}
