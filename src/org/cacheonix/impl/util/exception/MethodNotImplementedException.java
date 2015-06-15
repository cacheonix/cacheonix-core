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
package org.cacheonix.impl.util.exception;

import org.cacheonix.exceptions.CacheonixException;

/**
 * This runtime exception is thrown when a call is made to a method that has not been implemented yet.
 */
public final class MethodNotImplementedException extends CacheonixException {

   private static final String THIS_METHOD_IS_NOT_IMPLEMENTED_YET = "This method is not implemented yet";

   private static final long serialVersionUID = 0L;


   /**
    * Constructs a new runtime exception with <code>null</code> as its detail message.  The cause is not initialized,
    * and may subsequently be initialized by a call to {@link #initCause}.
    */
   public MethodNotImplementedException() {

      super(THIS_METHOD_IS_NOT_IMPLEMENTED_YET);
   }
}
