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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.Cookie;

import org.cacheonix.cache.Immutable;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.net.serializer.WireableFactory;
import org.cacheonix.impl.util.array.HashMap;

import static org.cacheonix.impl.cache.web.CookieSerializer.readCookie;
import static org.cacheonix.impl.cache.web.CookieSerializer.writeCookie;
import static org.cacheonix.impl.net.serializer.SerializerUtils.readString;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeString;

/**
 *
 */
@SuppressWarnings("NegatedConditionalExpression")
public final class CachedResponseKey implements Wireable, Serializable, Immutable {

   private static final long serialVersionUID = -3435499511258466883L;

   public static final WireableBuilder BUILDER = new WireableBuilder() {

      public Wireable create() {

         return new CachedResponseKey();
      }
   };

   private Map<String, List<String>> parameterMap;

   private List<Cookie> cookies;

   private String requestURI;


   public CachedResponseKey() {

   }


   public CachedResponseKey(final String requestURI, final Map<String, List<String>> parameterMap,
           final List<Cookie> cookies) {

      this.parameterMap = parameterMap == null ? null : new HashMap<String, List<String>>(parameterMap);
      this.cookies = cookies == null ? null : new ArrayList<Cookie>(cookies);
      this.requestURI = requestURI;
   }


   /**
    * Returns index of this object in the object registry.
    *
    * @return index of this object in the object registry.
    * @see WireableFactory
    */
   public int getWireableType() {

      return TYPE_CACHED_RESPONSE_KEY;
   }


   /**
    * Reads this wireable object from the stream.
    *
    * @param in a binary data input
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   public void readWire(final DataInputStream in) throws IOException {

      //
      requestURI = readString(in);

      //
      final boolean parametersNull = in.readBoolean();
      if (parametersNull) {

         parameterMap = null;
      } else {

         final int parameterMapSize = in.readInt();
         parameterMap = new HashMap<String, List<String>>(parameterMapSize);
         for (int i = 0; i < parameterMapSize; i++) {

            final String paramKey = readString(in);
            final int paramListSize = in.readInt();
            final List<String> paramList = new ArrayList<String>(paramListSize);
            for (int j = 0; j < paramListSize; j++) {

               paramList.add(readString(in));
            }
            parameterMap.put(paramKey, paramList);
         }
      }

      //
      final boolean cookiesAreNull = in.readBoolean();
      if (cookiesAreNull) {

         cookies = null;
      } else {

         final int cookiesSize = in.readInt();
         cookies = new ArrayList<Cookie>(cookiesSize);
         for (int i = 0; i < cookiesSize; i++) {

            cookies.add(readCookie(in));
         }
      }
   }


   /**
    * Writes this wireable object to the wire.
    *
    * @param out data output stream
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      //
      writeString(requestURI, out);

      //
      final boolean parametersNull = parameterMap == null;
      if (parametersNull) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);

         final int parameterMapSize = parameterMap.size();
         out.writeInt(parameterMapSize);

         for (final Entry<String, List<String>> entry : parameterMap.entrySet()) {

            final String paramKey = entry.getKey();
            writeString(paramKey, out);

            final List<String> paramList = entry.getValue();
            out.writeInt(paramList.size());
            for (final String paramValue : paramList) {

               writeString(paramValue, out);
            }
         }
      }

      //
      final boolean cookiesNull = cookies == null;
      if (cookiesNull) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);

         out.writeInt(cookies.size());
         for (final Cookie cookie : cookies) {

            writeCookie(cookie, out);
         }
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final CachedResponseKey that = (CachedResponseKey) o;

      if (parameterMap != null ? !parameterMap.equals(that.parameterMap) : that.parameterMap != null) {
         return false;
      }

      if (cookies == null && that.cookies != null) {
         return false;
      }
      if (cookies != null && that.cookies == null) {
         return false;
      }
      if (cookies != null) {
         if (cookies.size() != that.cookies.size()) {
            return false;
         }
         for (int i = 0; i < cookies.size(); i++) {
            final Cookie thisCookie = cookies.get(i);
            final Cookie thatCookie = that.cookies.get(i);
            if (thatCookie == null) {
               return false;
            }
            if (!CookieSerializer.equals(thisCookie, thatCookie)) {
               return false;
            }
         }
      }


      return !(requestURI != null ? !requestURI.equals(that.requestURI) : that.requestURI != null);

   }


   public int hashCode() {

      int result = parameterMap != null ? parameterMap.hashCode() : 0;
      result = 31 * result + (requestURI != null ? requestURI.hashCode() : 0);
      if (cookies != null) {
         for (final Cookie cookie : cookies) {
            final String cookieName = cookie.getName();
            result = 31 * result + (cookieName != null ? cookieName.hashCode() : 0);
         }
      }
      return result;
   }


   public String toString() {

      return "CachedResponseKey{" +
              "requestURI='" + requestURI + '\'' +
              ", parameterMap=" + parameterMap +
              '}';
   }
}
