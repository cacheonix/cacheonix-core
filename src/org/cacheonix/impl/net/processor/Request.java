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
package org.cacheonix.impl.net.processor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.net.serializer.SerializerUtils;

/**
 */
public abstract class Request extends Message {

   /**
    * Request UUID.
    */
   private UUID uuid = null;


   /**
    * <code>true</code> if the receiver has to acknowledge the receiving by sending response with a result code.
    */
   private boolean responseRequired = true;


   /**
    * Timeout in nanos. Null if timeout is not set.
    */
   private long timeoutMillis = 0L;

   /**
    * Indicator that a timeout is set.
    */
   private boolean hasTimeout = false;


   /**
    * Waiter for a response to a request.
    *
    * @see #getWaiter()
    */
   private Waiter waiter = null;


   protected Request() {

   }


   protected Request(final int wireableType) {

      super(wireableType);
      this.uuid = UUID.randomUUID();
      this.responseRequired = true;
   }


   /**
    * Returns UUID of the request. UUID is used to match the requests that require results and that are waiting for them
    * with the results when they are available.
    *
    * @return UUID of the request.
    */
   public final UUID getUuid() {

      return uuid;
   }


   /**
    * @return <code>true</code> if the receiver has to acknowledge the receiving by sending response with a result code.
    */
   public final boolean isResponseRequired() {

      return responseRequired;
   }


   /**
    * Sets <code>responseRequired</code> flag. A message requiring response will be asked to produce a waiter for the
    * response. That will be placed in the waiter registry.
    *
    * @param responseRequired <code>true</code> if the receiver has to return a response message.
    * @see #createWaiter()
    */
   public final void setResponseRequired(final boolean responseRequired) {

      this.responseRequired = responseRequired;
   }


   public final long getTimeoutMillis() {

      return timeoutMillis;
   }


   public final void setTimeoutMillis(final long timeoutMillis) {

      this.timeoutMillis = timeoutMillis;
      this.hasTimeout = true;
   }


   /**
    * Returns true if timeout is set.
    *
    * @return true if timeout is set.
    */
   public boolean hasTimeout() {

      return hasTimeout;
   }


   /**
    * Casts message to a request that requires response.
    *
    * @param message message to cast.
    * @return a request or null if message is not an instance of Request or if the request does not require a response.
    */
   public static Request toRequest(final Message message) {

      if (!(message instanceof Request)) {
         return null;
      }
      final Request request = (Request) message;
      return request.responseRequired ? request : null;
   }


   /**
    * Creates a response.
    *
    * @param responseCode response
    * @return a response matching the request.
    */
   public abstract Response createResponse(final int responseCode);


   /**
    * Creates a response.
    *
    * @param responseCode response the response code.
    * @param result       the result, can be <code>null</code>
    * @return a response matching the request.
    */
   public final Response createResponse(final int responseCode, final Object result) {

      final Response response = createResponse(responseCode);
      response.setResult(result);
      return response;
   }


   /**
    * This factory method returns a new instance of a <code>Waiter<code>. Classes extending <code>Message<code> may
    * override this method to create a waiter instance that is specific to their purpose.
    *
    * @return returns a new instance of a <code>Waiter<code>
    * @see #getWaiter()
    */
   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   /**
    * Returns a waiter associated with this request. If there is no waiter, initialized it by calling {@link
    * #createWaiter()}.
    *
    * @return a waiter associated with this request.
    */
   public final Waiter getWaiter() {

      if (waiter == null) {
         waiter = createWaiter();
      }
      return waiter;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeUuid(uuid, out);
      out.writeBoolean(responseRequired);
      out.writeBoolean(hasTimeout);
      out.writeLong(timeoutMillis);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      uuid = SerializerUtils.readUuid(in);
      responseRequired = in.readBoolean();
      hasTimeout = in.readBoolean();
      timeoutMillis = in.readLong();
   }


   public String toString() {

      return "Request{" +
              "responseRequired=" + responseRequired +
              ", timeout=" + timeoutMillis +
              ", uuid=" + uuid +
              "} " + super.toString();
   }
}
