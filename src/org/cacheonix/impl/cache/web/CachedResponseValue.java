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
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.servlet.http.Cookie;

import org.cacheonix.cache.Immutable;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.cacheonix.impl.net.serializer.SerializerUtils.readByteArray;
import static org.cacheonix.impl.net.serializer.SerializerUtils.readObject;
import static org.cacheonix.impl.net.serializer.SerializerUtils.readString;
import static org.cacheonix.impl.net.serializer.SerializerUtils.readWireable;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeByteArray;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeObject;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeString;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeWireable;
import static org.cacheonix.impl.util.ArrayUtils.copy;

/**
 * A cached response value.
 */
public final class CachedResponseValue implements Wireable, Serializable, Immutable {

   public static final WireableBuilder BUILDER = new WireableBuilder() {

      public Wireable create() {

         return new CachedResponseValue();
      }
   };

   private static final long serialVersionUID = 4434736416582794562L;

   static final String CONTENT_TYPE = "Content-Type";

   static final String ENCODING = "Encoding";

   static final String COMPRESS = "compress";

   private static final String GZIP = "gzip";

   private final Map<String, Cookie> cookies = new TreeMap<String, Cookie>(CASE_INSENSITIVE_ORDER);

   private final Map<String, Collection<Header>> headers = new TreeMap<String, Collection<Header>>(
           CASE_INSENSITIVE_ORDER);

   private String statusMessage = null;

   private byte[] byteResponse = null;

   private String redirectUrl = null;

   private String contentType = null;

   private int contentLength = 0;

   private int statusCode = 0;

   private Locale locale = null;


   public CachedResponseValue() {

   }


   public CachedResponseValue(final String statusMessage, final byte[] byteResponse,
           final String redirectUrl, final String contentType, final int contentLength, final int statusCode,
           final Locale locale, final Map<String, Cookie> cookies, final Map<String, Collection<Header>> headers) {

      this.contentLength = contentLength;
      this.statusMessage = statusMessage;
      this.byteResponse = copy(byteResponse);
      this.redirectUrl = redirectUrl;
      this.contentType = contentType;
      this.statusCode = statusCode;
      this.cookies.putAll(cookies);
      this.headers.putAll(headers);
      this.locale = locale;
   }


   public String getContentType() {

      return contentType;
   }


   public int getContentLength() {

      return contentLength;
   }


   public Locale getLocale() {

      return locale;
   }


   public String getStatusMessage() {

      return statusMessage;
   }


   public String getRedirectUrl() {

      return redirectUrl;
   }


   public int getStatusCode() {

      return statusCode;
   }


   public byte[] getByteResponse() {

      return byteResponse;
   }


   public Map<String, Cookie> getCookies() {

      return new HashMap<String, Cookie>(cookies);
   }


   /**
    * Returns index of this object in the object registry.
    *
    * @return index of this object in the object registry.
    */
   public int getWireableType() {

      return TYPE_CACHED_RESPONSE_VALUE;
   }


   public Map<String, Collection<Header>> getHeaders() {

      return new HashMap<String, Collection<Header>>(headers);
   }


   /**
    * Returns true if the cached content is text.
    *
    * @return true if the cached content is text.
    */
   boolean isTextContentType() {

      if (StringUtils.isBlank(contentType)) {

         return false;
      }

      return contentType.startsWith("text/");
   }


   /**
    * Returns true if the cached content is compressed.
    *
    * @return true if the cached content is compressed.
    */
   boolean isCompressed() {

      if (CollectionUtils.isEmpty(headers)) {

         return false;
      }

      final Collection<Header> encoding = headers.get(ENCODING);
      if (CollectionUtils.isEmpty(encoding)) {
         return false;
      }

      for (final Header encodingHeader : encoding) {

         if (encodingHeader.containsString(GZIP) || encodingHeader.containsString(COMPRESS)) {
            return true;
         }
      }

      return false;
   }


   /**
    * Writes this wireable object to the wire.
    *
    * @param out data output stream
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      writeByteArray(out, byteResponse);
      writeString(statusMessage, out);
      writeString(redirectUrl, out);
      writeString(contentType, out);
      writeObject(out, locale);
      out.writeInt(contentLength);
      out.writeInt(statusCode);

      // Cookies
      out.writeInt(cookies.size());
      for (final Entry<String, Cookie> cookieEntry : cookies.entrySet()) {

         final Cookie cookie = cookieEntry.getValue();
         CookieSerializer.writeCookie(cookie, out);
      }

      // Headers

      out.writeInt(headers.size());
      for (final Entry<String, Collection<Header>> headerEntry : headers.entrySet()) {

         final String headerName = headerEntry.getKey();
         final Collection<Header> headerValues = headerEntry.getValue();
         final int headerValuesSize = headerValues.size();

         out.writeInt(headerValuesSize);
         writeString(headerName, out);
         for (final Header header : headerValues) {
            writeWireable(out, header);
         }
      }
   }


   /**
    * Reads this wireable object from the stream.
    *
    * @param in a binary data input
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   @SuppressWarnings("ConstantConditions")
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      byteResponse = readByteArray(in);
      statusMessage = readString(in);
      redirectUrl = readString(in);
      contentType = readString(in);
      locale = readObject(in);
      contentLength = in.readInt();
      statusCode = in.readInt();

      // Cookies
      final int cookiesSize = in.readInt();
      for (int i = 0; i < cookiesSize; i++) {

         final Cookie cookie = CookieSerializer.readCookie(in);
         cookies.put(cookie.getName(), cookie);
      }

      // Headers
      final int headerSize = in.readInt();
      for (int i = 0; i < headerSize; i++) {

         final int headerValuesSize = in.readInt();
         final String headerName = readString(in);
         final Collection<Header> headerValues = new ArrayList<Header>(headerValuesSize);
         for (int j = 0; j < headerValuesSize; j++) {

            final Header header = readWireable(in);
            headerValues.add(header);
         }
         headers.put(headerName, headerValues);
      }
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final CachedResponseValue that = (CachedResponseValue) o;

      if (contentLength != that.contentLength) {
         return false;
      }
      if (statusCode != that.statusCode) {
         return false;
      }
      if (headers != null ? !headers.equals(that.headers) : that.headers != null) {
         return false;
      }
      if (statusMessage != null ? !statusMessage.equals(that.statusMessage) : that.statusMessage != null) {
         return false;
      }
      if (redirectUrl != null ? !redirectUrl.equals(that.redirectUrl) : that.redirectUrl != null) {
         return false;
      }
      if (contentType != null ? !contentType.equals(that.contentType) : that.contentType != null) {
         return false;
      }
      if (!Arrays.equals(byteResponse, that.byteResponse)) {
         return false;
      }
      if (locale != null ? !locale.equals(that.locale) : that.locale != null) {
         return true;
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
         for (final Entry<String, Cookie> cookieEntry : cookies.entrySet()) {
            final Cookie thisCookie = cookieEntry.getValue();
            final String cookieName = cookieEntry.getKey();
            final Cookie thatCookie = that.cookies.get(cookieName);
            if (thatCookie == null) {
               return false;
            }
            if (!CookieSerializer.equals(thisCookie, thatCookie)) {
               return false;
            }
         }
      }
      return true;
   }


   public int hashCode() {

      int result = headers != null ? headers.hashCode() : 0;
      result = 31 * result + (statusMessage != null ? statusMessage.hashCode() : 0);
      result = 31 * result + (byteResponse != null ? Arrays.hashCode(byteResponse) : 0);
      result = 31 * result + (redirectUrl != null ? redirectUrl.hashCode() : 0);
      result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
      result = 31 * result + contentLength;
      result = 31 * result + statusCode;
      result = 31 * result + (locale != null ? locale.hashCode() : 0);
      if (cookies != null) {
         for (final Entry<String, Cookie> cookieEntry : cookies.entrySet()) {
            final String cookieName = cookieEntry.getValue().getName();
            result = 31 * result + (cookieName != null ? cookieName.hashCode() : 0);
         }
      }
      return result;
   }


   public String toString() {

      return "CachedResponseValue{" +
              "cookies=" + cookies +
              ", headers=" + headers +
              ", statusMessage='" + statusMessage + '\'' +
              ", byteResponse=" + Arrays.toString(byteResponse) +
              ", redirectUrl='" + redirectUrl + '\'' +
              ", contentType='" + contentType + '\'' +
              ", contentLength=" + contentLength +
              ", statusCode=" + statusCode +
              ", locale=" + locale +
              '}';
   }
}
