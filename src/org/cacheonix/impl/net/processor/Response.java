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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Response to cluster requests.
 */
@SuppressWarnings("RedundantIfStatement")
public abstract class Response extends Message {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Response.class); // NOPMD


   /**
    * Result is OK.
    */
   public static final int RESULT_SUCCESS = 1;

   /**
    * Destination is unavailable.
    */
   public static final int RESULT_INACCESSIBLE = 2;


   /**
    * Result is ERROR. The result field contains an exception or a String description of an error.
    */
   public static final int RESULT_ERROR = 3;

   /**
    * Retry request.
    */
   public static final int RESULT_RETRY = 4;


   /**
    * UUID of a request this response is send to
    */
   private UUID responseToUUID = null;


   /**
    * Result code.
    */
   private int resultCode = RESULT_SUCCESS;

   private Object result = null;

   private String responseToClass = null;


   public Response(final int wireableType) {

      super(wireableType);
      setRequiresSameCluster(false);
   }


   /**
    * Creates a RetryException while setting the exception message to the response's result if the response carries a
    * String result. A utility method.
    *
    * @param response the response to use.
    * @return RetryException with the exception message set to the response's result if the response carries a String
    *         result.
    */
   public RetryException createRetryException() {

      if (result instanceof String) {

         final String message = (String) result;
         return new RetryException(message);
      } else {

         final String resultType = result == null ? null : result.getClass().getName();
         return new RetryException("Result type was: " + resultType);
      }
   }


   /**
    * Returns UUID of the request this request is a response to.
    *
    * @return UUID of the request this request is a response to.
    */
   public final UUID getResponseToUUID() {

      return responseToUUID;
   }


   public final void setResponseToUUID(final UUID responseToUUID) {

      this.responseToUUID = responseToUUID;
   }


   public int getResultCode() {

      return resultCode;
   }


   /**
    * Sets the response's result code.
    *
    * @param resultCode the result code.
    * @see #RESULT_ERROR
    * @see #RESULT_INACCESSIBLE
    * @see #RESULT_SUCCESS
    * @see #RESULT_RETRY
    */
   public void setResultCode(final int resultCode) {

      this.resultCode = resultCode;
   }


   public Object getResult() {

      return result;
   }


   public void setResult(final Object result) {

      this.result = result;
   }


   public void setResponseToClass(final Class clazz) {

      this.responseToClass = clazz.getName();
   }


   public void execute() throws InterruptedException {

      final RequestProcessor processor = getProcessor();
      final WaiterList waiterList = processor.getWaiterList();
      waiterList.notifyReceived(this);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      responseToClass = SerializerUtils.readString(in);
      responseToUUID = SerializerUtils.readUuid(in);
      resultCode = in.readInt();
      result = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA).deserialize(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeString(responseToClass, out);
      SerializerUtils.writeUuid(responseToUUID, out);
      out.writeInt(resultCode);
      SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA).serialize(result, out);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final Response response = (Response) o;

      if (resultCode != response.resultCode) {
         return false;
      }
      if (responseToUUID != null ? !responseToUUID.equals(response.responseToUUID) : response.responseToUUID != null) {
         return false;
      }
      if (result != null ? !result.equals(response.result) : response.result != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result1 = super.hashCode();
      result1 = 31 * result1 + (responseToUUID != null ? responseToUUID.hashCode() : 0);
      result1 = 31 * result1 + resultCode;
      result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
      return result1;
   }


   private String resultToString() {

      if (result == null) {
         return "null";
      }
      if (result instanceof Collection) {
         return result.getClass().getSimpleName() + '(' + ((Collection) result).size() + ')';
      }
      return result.toString();
   }


   /**
    * Converts a result code to a human-readable string.
    *
    * @param resultCode the result code to convert to string.
    * @return the human-readable representation of the result code.
    */
   private static String resultCodeToString(final int resultCode) {

      switch (resultCode) {
         case RESULT_ERROR:
            return "ERROR";
         case RESULT_INACCESSIBLE:
            return "INACCESSIBLE";
         case RESULT_RETRY:
            return "RETRY";
         case RESULT_SUCCESS:
            return "SUCCESS";
         default:
            return Integer.toString(resultCode);
      }
   }


   public String toString() {

      return "Response{" +
              "sender=" + getSender() +
              ", resultCode=" + resultCodeToString(resultCode) +
              ", result=" + resultToString() +
              ", responseToClass='" + responseToClass + '\'' +
              ", responseToUUID=" + responseToUUID +
              "} " + super.toString();
   }
}