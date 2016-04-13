package org.cacheonix.impl.cache.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.servlet.http.Cookie;

import static org.cacheonix.impl.net.serializer.SerializerUtils.readString;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeString;

/**
 * A Cookie serializer.
 */
final class CookieSerializer {

   private CookieSerializer() {

   }


   /**
    * Writes a Cookie to a <code>DataOutputStream</code>.
    *
    * @param cookie a Cookie to write.
    * @param out    the <code>DataOutputStream</code>.
    * @throws IOException if there was an error while writing to the <code>DataOutputStream</code>.
    */
   static void writeCookie(final Cookie cookie, final DataOutputStream out) throws IOException {

      writeString(cookie.getName(), out);
      writeString(cookie.getValue(), out);
      writeString(cookie.getComment(), out);
      writeString(cookie.getPath(), out);
      out.writeBoolean(cookie.getSecure());
      out.writeInt(cookie.getVersion());
      out.writeInt(cookie.getMaxAge());
      writeString(cookie.getDomain(), out);
   }


   /**
    * Reads a Cookie from a <code>DataInputStream</code>.
    *
    * @param in the <code>DataInputStream</code> to read from.
    * @throws IOException if there was an error while reading from the <code>DataInputStream</code>.
    */
   static Cookie readCookie(final DataInputStream in) throws IOException {

      final String name = readString(in);
      final String value = readString(in);
      assert name != null;
      final Cookie cookie = new Cookie(name, value);
      cookie.setComment(readString(in));
      cookie.setPath(readString(in));
      cookie.setSecure(in.readBoolean());
      cookie.setVersion(in.readInt());
      cookie.setMaxAge(in.readInt());
      final String domain = readString(in);
      if (domain != null) {
         cookie.setDomain(domain);
      }
      return cookie;
   }


   static boolean equals(final Cookie thisCookie, final Cookie thatCookie) {

      if (thisCookie.getMaxAge() != thatCookie.getMaxAge()) {
         return false;
      }
      if (thisCookie.getSecure() != thatCookie.getSecure()) {
         return false;
      }
      if (thisCookie.getVersion() != thatCookie.getVersion()) {
         return false;
      }
      if (thisCookie.getName() != null ? !thisCookie.getName().equals(
              thatCookie.getName()) : thatCookie.getName() != null) {
         return false;
      }
      if (thisCookie.getValue() != null ? !thisCookie.getValue().equals(
              thatCookie.getValue()) : thatCookie.getValue() != null) {
         return false;
      }
      if (thisCookie.getComment() != null ? !thisCookie.getComment().equals(
              thatCookie.getComment()) : thatCookie.getComment() != null) {
         return false;
      }
      if (thisCookie.getDomain() != null ? !thisCookie.getDomain().equals(
              thatCookie.getDomain()) : thatCookie.getDomain() != null) {
         return false;
      }
      if (thisCookie.getPath() != null ? !thisCookie.getPath().equals(
              thatCookie.getPath()) : thatCookie.getPath() != null) {
         return false;
      }
      return true;
   }
}
