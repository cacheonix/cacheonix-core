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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.net.processor.Request;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.net.processor.WaiterUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * Request the local cache processor for max number of elements in memory.
 */
public final class GetMaxSizeRequest extends CacheRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();


   /**
    * Required by wireable.
    */
   @SuppressWarnings("UnusedDeclaration")
   public GetMaxSizeRequest() {

   }


   public GetMaxSizeRequest(final String cacheName) {

      super(TYPE_CACHE_GET_MAX_SIZE_REQUEST, cacheName);
   }


   public void executeOperational() {

      final long maxSize = getCacheProcessor().getMaxSize();
      final Response response = createResponse(Response.RESULT_SUCCESS);
      response.setResult(maxSize);
      getCacheProcessor().post(response);
   }


   protected void executeBlocked() {

      // This is a client request so it should respond with retry
      getProcessor().post(createResponse(Response.RESULT_RETRY));
   }


   protected Waiter createWaiter() {

      return new Waiter(this);
   }


   public String toString() {

      return "GetMaxSizeRequest{" +
              "} " + super.toString();
   }


   @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
   static final class Waiter extends org.cacheonix.impl.net.processor.Waiter {

      /**
       * Creates waiter.
       *
       * @param request request UUID
       */
      public Waiter(final Request request) {

         super(request);
      }


      /**
       * {@inheritDoc}
       * <p/>
       * GetMaxSizeRequest's implementation converts response to the waiter's result.
       */
      public void notifyResponseReceived(final Response response) throws InterruptedException {

         if (response instanceof CacheResponse) {
            final CacheResponse cacheResponse = (CacheResponse) response;
            final int resultCode = cacheResponse.getResultCode();
            final Object objectResult = cacheResponse.getResult();
            switch (resultCode) {
               case CacheResponse.RESULT_SUCCESS:
                  setResult(objectResult);
                  break;
               case CacheResponse.RESULT_ERROR:
                  setResult(WaiterUtils.resultToThrowable(objectResult));
                  break;
               case CacheResponse.RESULT_INACCESSIBLE:
               case CacheResponse.RESULT_RETRY:
                  setResult(new RetryException());
                  break;
               default:
                  setResult(WaiterUtils.unknownResultToThrowable(resultCode, objectResult));
            }
         }
         super.notifyResponseReceived(response);
      }


      protected void notifyFinished() {

         if (!isResponseReceived()) {
            setResult(new RetryException());
         }
         super.notifyFinished();
      }
   }

   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetMaxSizeRequest();
      }
   }
}
