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
package org.cacheonix.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cacheonix.impl.util.array.Hash;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntArrayList;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Common string utilities
 */
public final class StringUtils {


   /**
    * Logger.
    */
   private static final Logger LOG = Logger.getLogger(StringUtils.class);


   private static final char[] HEX_DIGITS = {
           '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
   };

   private static final char CHAR_SINGLE_QUOTE = '\'';

   private static final char CHAR_DOUBLE_QUOTE = '\"';


   private static final int BASELENGTH = 255;

   private static final int LOOKUPLENGTH = 64;

   private static final int TWENTYFOURBITGROUP = 24;

   private static final int EIGHTBIT = 8;

   private static final int SIXTEENBIT = 16;

   private static final int FOURBYTE = 4;

   private static final int SIGN = -128;

   private static final byte PAD = (byte) '=';

   private static final byte[] base64Alphabet = new byte[BASELENGTH];

   private static final byte[] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];

   private static final String STRING_TRUE = "true";

   private static final String STRING_YES = "yes";

   private static final String STRING_ENABLED = "enabled";

   private static final String NO = "No";

   private static final String YES = "Yes";

   private static final String NULL_AS_STRING = "null";

   private static final int KILO_BYTE = 1024;

   private static final int MEGA_BYTE = KILO_BYTE << 10;

   private static final int GIGA_BYTE = 1073741824;

   /**
    * A pattern used to parse time.
    */
   private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)(milliseconds|millis|ms|seconds|secs|s|minutes|min)");

   /**
    * A pattern used to parse bytes.
    */
   private static final Pattern BYTES_PATTERN = Pattern.compile("([0-9]+)(bytes|kilobytes|k|kb|megabytes|mb|m|gigabytes|gb|g|%)");


   private StringUtils() {

   }


   static {
      for (int i = 0; i < BASELENGTH; i++) {
         base64Alphabet[i] = (byte) -1;
      }
      for (int i = (int) 'Z'; i >= (int) 'A'; i--) {
         base64Alphabet[i] = (byte) (i - (int) 'A');
      }
      for (int i = (int) 'z'; i >= (int) 'a'; i--) {
         base64Alphabet[i] = (byte) (i - (int) 'a' + 26);
      }
      for (int i = (int) '9'; i >= (int) '0'; i--) {
         base64Alphabet[i] = (byte) (i - (int) '0' + 52);
      }

      base64Alphabet[((int) '+')] = (byte) 62;
      base64Alphabet[((int) '/')] = (byte) 63;

      for (int i = 0; i <= 25; i++) {
         lookUpBase64Alphabet[i] = (byte) ((int) 'A' + i);
      }

      for (int i = 26, j = 0; i <= 51; i++, j++) {
         lookUpBase64Alphabet[i] = (byte) ((int) 'a' + j);
      }

      for (int i = 52, j = 0; i <= 61; i++, j++) {
         lookUpBase64Alphabet[i] = (byte) ((int) '0' + j);
      }

      lookUpBase64Alphabet[62] = (byte) '+';
      lookUpBase64Alphabet[63] = (byte) '/';
   }


   /**
    * Returns <code>true</code> if string is null
    */
   public static boolean isNull(final String string) {

      return string == null;
   }


   /**
    * Returns <code>true</code> if string is blank
    *
    * @throws NullPointerException if string is null
    */
   public static boolean isBlank(final String string) {

      if (isNull(string)) {
         return true;
      }
      final int length = string.length();
      if (length == 0) {
         return true;
      }
      for (int i = 0; i < length; i++) {
         if (string.charAt(i) > ' ') {
            return false;
         }
      }
      return true;
   }


   /**
    * Makes word duration.
    */
   public static StringBuffer durationToString(final long seconds, final boolean fullWords) {

      final StringBuffer result = new StringBuffer(30);
      long secondsLeft = seconds;
      final long durationDays = secondsLeft / 86400L;
      if (durationDays > 0L) {
         result.append(Long.toString(durationDays)).append(fullWords ? " days " : "d ");
         secondsLeft %= 86400L;
      }

      final long durationHours = secondsLeft / 3600L;
      if (durationHours > 0L) {
         result.append(durationHours < 10L ? "0" : "").append(Long.toString(durationHours)).append(
                 fullWords ? " hours " : "h ");
         secondsLeft %= 3600L;
      }
      if (durationDays > 0L) {
         return result;
      }

      final long durationMinutes = secondsLeft / 60L;
      if (durationMinutes > 0L) {
         result.append(durationMinutes < 10L ? "0" : "").append(
                 Long.toString(durationMinutes)).append(fullWords ? " minutes " : "m ");
         secondsLeft %= 60L;
      }
      if (durationHours > 0L) {
         return result;
      }

      if (secondsLeft < 10L) {
         result.append('0');
      }
      return result.append(Long.toString(secondsLeft)).append(fullWords ? " seconds " : "s ");
   }


   /**
    * Cleans up exception from the exception name.
    *
    * @param e the exception which message must be cleanup.
    * @return exception message without the prefixing exception name.
    */
   public static String toString(final Throwable e) {

      final String message = e.toString();
      final int i = message.indexOf("ion: ");
      String result = null;
      if (i >= 0) {
         result = message.substring(i + 5).trim();
      } else {
         result = message.trim();
      }
      if (isBlank(result)) {
         result = e.getMessage();
      }
      return result;
   }


   /**
    * Returns <code>true</code> if parameter is a valid string representation of integer value
    */
   public static boolean isValidInteger(final String s) {

      if (isBlank(s)) {
         return false;
      }
      try {
         Integer.parseInt(s);
         return true;
      } catch (final Exception e) {
         return false;
      }
   }


   /**
    * Returns <code>true</code> if parameter is a valid string representation of long value
    */
   public static boolean isValidLong(final String s) {

      if (isBlank(s)) {
         return false;
      }
      try {
         Long.parseLong(s);
         return true;
      } catch (final Exception e) {
         return false;
      }
   }


   /**
    * Validates if the name is a strict name
    */
   public static boolean isValidStrictName(final String name) {

      return Pattern.compile("[a-zA-Z][-a-zA-Z_0-9]*").matcher(name).matches();
   }


   /**
    * Extracts a stack trace from Throwable to a String
    */
   public static String stackTraceToString(final Throwable th) {

      ByteArrayOutputStream baos = null;
      try {
         baos = new ByteArrayOutputStream(1000);
         final PrintStream ps = new PrintStream(baos);
         th.printStackTrace(ps);
         return baos.toString();
      } finally {
         if (baos != null) {
            try {
               baos.flush();
               baos.close();
            } catch (final IOException ignore) {
               ignoreException(ignore);
            }
         }
      }
   }


   /**
    * Empty exception ingnorer.
    */
   private static void ignoreException(final Exception ignore) {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Ignored exception", ignore);
      }
   }


   /**
    * Formats date in accordance with given format
    *
    * @param date   Date to format
    * @param format String format used to format Date
    * @return String with formatted date
    */
   public static String formatDate(final Date date, final String format) {

      return new SimpleDateFormat(format, Locale.US).format(date);
   }


   /**
    * Gets file name out of file path
    */
   public static String extractNameFromFilePath(final String filePath) {

      final int lastSlash = filePath.lastIndexOf((int) '/');
      return filePath.substring(lastSlash + 1);
   }


   /**
    * Gets file name out of file path
    */
   public static String extractPathFromFilePath(final String filePath) {

      final int lastSlash = filePath.lastIndexOf((int) '/');
      if (lastSlash == -1) {
         return "";
      } else {
         return filePath.substring(0, lastSlash);
      }
   }


   /**
    * Converts array of int representing an int-encoded string to a String
    */
   public static String intArrayToString(final int[] array) {

      final StringBuilder result = new StringBuilder(array.length);
      for (final int anArray : array) {
         result.append((char) anArray);
      }
      return result.toString();
   }


   /**
    * Returns <code>true</code> if first character of the string is a letter
    */
   public static boolean isFirstLetter(final String s) {

      return !isBlank(s) && Pattern.compile("[a-zA-Z]").matcher(s.substring(0, 1)).matches();
   }


   /**
    * @return <code>true</code> if system property is set and equals given value.
    */
   public static boolean systemPropertyEquals(final String name, final String value) {

      final String property = System.getProperty(name);
      return property != null && property.equalsIgnoreCase(value);
   }


   /**
    * @return <code>true</code> this string pattern is empty
    */
   public static boolean patternIsEmpty(final String pattern) {

      return isBlank(pattern) || !new StringTokenizer(pattern, "\n \r", false).hasMoreTokens();
   }


   /**
    * Breaks a possibly multiline string to a list of lines. Empty lines are excluded from the list.
    */
   public static List multilineStringToList(final String multilineString) {

      final List result = new ArrayList(3);
      if (isBlank(multilineString)) {
         return result;
      }
      for (final StringTokenizer st = new StringTokenizer(multilineString, "\n\r",
              false); st.hasMoreTokens(); ) {
         final String s = st.nextToken();
         if (isBlank(s)) {
            continue;
         }
         result.add(s.trim());
      }
      return result;
   }


   /**
    * Converts a list of Strings to a String containing items of the list as lines separated by "\n".
    */
   public static String linesToString(final List stringList) {

      final StringBuilder result = new StringBuilder(300);
      for (final Object aStringList : stringList) {
         result.append((String) aStringList).append('\n');
      }
      return result.toString();
   }


   public static String[] toStringArray(final List stringList) {

      return (String[]) stringList.toArray(new String[stringList.size()]);
   }


   /**
    * Returns a byte array from a string of hexadecimal digits.
    */
   public static byte[] decodeFromHex(final String hex) {

      final int len = hex.length();
      final byte[] buf = new byte[(len + 1 >> 1)];

      int i = 0;
      int j = 0;
      if (len % 2 == 1) {
         buf[j++] = (byte) fromDigit(hex.charAt(i++));
      }

      while (i < len) {
         buf[j++] = (byte) (fromDigit(hex.charAt(i++)) << 4 |
                 fromDigit(hex.charAt(i++)));
      }
      return buf;
   }


   /**
    * Returns the number from 0 to 15 corresponding to the hex digit <i>ch</i>.
    */
   private static int fromDigit(final char ch) {

      if (ch >= '0' && ch <= '9') {
         return (int) ch - (int) '0';
      }
      if (ch >= 'A' && ch <= 'F') {
         return (int) ch - (int) 'A' + 10;
      }
      if (ch >= 'a' && ch <= 'f') {
         return (int) ch - (int) 'a' + 10;
      }

      throw new IllegalArgumentException("invalid hex digit '" + ch + '\'');
   }


   /**
    * Returns a string of hexadecimal digits from a byte array. Each byte is converted to 2 hex symbols.
    */
   public static String encodeToHex(
           final byte[] ba) { // NOPMD - "A user given array is stored directly"  - we do NOT store anything.
      final int length = ba.length;
      final char[] buf = new char[(length << 1)];
      for (int i = 0, j = 0; i < length; ) {
         final int k = ba[i++];
         buf[j++] = HEX_DIGITS[k >>> 4 & 0x0F];
         buf[j++] = HEX_DIGITS[k & 0x0F];
      }
      return String.valueOf(buf);
   }


   /**
    * Digests password with MD5 and encodes it as a hex String.
    *
    * @param password to digest.
    * @return hex encoded password digest.
    */
   public static String digest(final String password) throws NoSuchAlgorithmException {

      final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.reset();
      messageDigest.update(password.trim().toLowerCase().getBytes());
      return encodeToHex(messageDigest.digest());
   }


   public static long daysToMillis(final int days) {

      return (long) days * 24L * 60L * 60L * 1000L;
   }


   /**
    * Truncates string to maxLen.
    *
    * @param in     input string
    * @param maxLen max length
    */
   public static String truncate(final String in, final int maxLen) {

      if (in == null || in.length() <= maxLen) {
         return in;
      } else {
         return in.substring(0, maxLen);
      }
   }


   public static void appendWithNewLineIfNotNull(final StringBuffer sb, final String label,
                                                 final String value) {

      if (!isBlank(value)) {
         sb.append(label).append(' ').append(value);
         sb.append("/n");
      }
   }


   /**
    * Puts string into double quotes. Other leading and trailing quotes are removed first.
    *
    * @param stringToProcess String to quote, trimmed before quoting.
    * @return double-quoted string.
    */
   public static String putIntoDoubleQuotes(final String stringToProcess) {

      final char[] dd = stringToProcess.trim().toCharArray();
      final int len = dd.length;
      int st = 0;
      for (; st < len; st++) {
         final char c = dd[st];
         if (c != CHAR_DOUBLE_QUOTE && c != CHAR_SINGLE_QUOTE) {
            break;
         }
      }

      int en = len;
      for (; en > st; en--) {
         final char c = dd[en - 1];
         if (c != CHAR_DOUBLE_QUOTE && c != CHAR_SINGLE_QUOTE) {
            break;
         }
      }

      //if (log.isDebugEnabled()) log.debug("st: " + st);
      //if (log.isDebugEnabled()) log.debug("en: " + en);
      //if (log.isDebugEnabled()) log.debug("stringToProcess: " + "\"" + stringToProcess + "\"");
      return '\"' + stringToProcess.substring(st, en) + '\"';
   }


   public static String removeDoubleQuotes(final String stringToProcess) {

      if (stringToProcess.startsWith("\"") && stringToProcess.endsWith("\"")) {
         return stringToProcess.substring(1, stringToProcess.length() - 1);
      } else {
         return stringToProcess;
      }
   }


   public static List split(final String value, final int maxLength) {

      final List parts = new ArrayList(23);
      final int originalLength = value.length();
      if (originalLength > maxLength) {
         // split it if necessary
         final int splitCount = originalLength / maxLength;
         for (int splitIndex = 0; splitIndex < splitCount; splitIndex++) {
            parts.add(value.substring(splitIndex * maxLength, (splitIndex + 1) * maxLength));
         }
         // process last piece if any
         final int leftOverLength = originalLength - splitCount * maxLength;
         if (leftOverLength > 0) {
            parts.add(value.substring(originalLength - leftOverLength, originalLength));
         }
      } else {
         parts.add(value);
      }
      return parts;
   }


   public static String formatWithTrailingZeroes(final int value, final int zeroes) {

      final String stringValue = Integer.toString(value);
      final StringBuilder result = new StringBuilder(5);
      final int numberOfZeroesToAdd = zeroes - stringValue.length();
      for (int i = 0; i < numberOfZeroesToAdd; i++) {
         result.append('0');
      }
      result.append(stringValue);
      return result.toString();
   }


   /**
    * Makes pattern array of compiled regex patterns.
    */
   public static Pattern[] makeRegexPatternsFromMultilineString(final String customPatterns) {

      final Set result = new HashSet(5);
      for (final StringTokenizer st = new StringTokenizer(customPatterns, "\n",
              false); st.hasMoreTokens(); ) {
         final String pattern = st.nextToken().trim();
         if (isRegex(pattern)) {
            result.add(Pattern.compile(pattern));
         }
      }
      return (Pattern[]) result.toArray(new Pattern[result.size()]);
   }


   /**
    * @param pattern
    * @return <code>true</code> if pattern starts with '^' and ends with '$'
    */
   public static boolean isRegex(final String pattern) {

      return !isBlank(pattern) && pattern.charAt(0) == '^' && pattern.endsWith("$");
   }


   public static boolean isBase64(final String isValidString) {

      return isArrayByteBase64(isValidString.getBytes());
   }


   public static boolean isBase64(final byte octect) {
      //shall we ignore white space? JEFF??
      return octect == PAD || base64Alphabet[((int) octect)] != (byte) -1;
   }


   public static boolean isArrayByteBase64(final byte[] arrayOctect) {

      final int length = arrayOctect.length;
      if (length == 0) {
         // shouldn't a 0 length array be valid base64 data?
         // return false;
         return true;
      }
      for (final byte anArrayOctect : arrayOctect) {
         if (!isBase64(anArrayOctect)) {
            return false;
         }
      }
      return true;
   }


   /**
    * Encodes hex octects into Base64.
    *
    * @param binaryData Array containing binary data to encode.
    * @return Base64-encoded data.
    */
   public static byte[] encode(final byte[] binaryData) {

      final int lengthDataBits = binaryData.length * EIGHTBIT;
      final int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
      final int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
      byte[] encodedData = null;


      if (fewerThan24bits == 0) {
         // 16 or 8 bit
         encodedData = new byte[(numberTriplets << 2)];
      } else {
         //data not divisible by 24 bit
         encodedData = new byte[(numberTriplets + 1 << 2)];
      }

      byte k = (byte) 0;
      byte l = (byte) 0;
      byte b1 = (byte) 0;
      byte b2 = (byte) 0;
      byte b3 = (byte) 0;

      int encodedIndex = 0;
      int dataIndex = 0;
      int i = 0;
      //log.debug("number of triplets = " + numberTriplets);
      for (i = 0; i < numberTriplets; i++) {
         dataIndex = i * 3;
         b1 = binaryData[dataIndex];
         b2 = binaryData[dataIndex + 1];
         b3 = binaryData[dataIndex + 2];

         //log.debug("b1= " + b1 +", b2= " + b2 + ", b3= " + b3);

         l = (byte) (b2 & 0x0f);
         k = (byte) (b1 & 0x03);

         encodedIndex = i << 2;
         final byte val1 = (b1 & SIGN) == 0 ? (byte) (b1 >> 2) : (byte) (b1 >> 2 ^ 0xc0);
         final byte val2 = (b2 & SIGN) == 0 ? (byte) (b2 >> 4) : (byte) (b2 >> 4 ^ 0xf0);
         final byte val3 = (b3 & SIGN) == 0 ? (byte) (b3 >> 6) : (byte) (b3 >> 6 ^ 0xfc);

         encodedData[encodedIndex] = lookUpBase64Alphabet[((int) val1)];
         //log.debug( "val2 = " + val2 );
         //log.debug( "k4   = " + (k<<4) );
         //log.debug(  "vak  = " + (val2 | (k<<4)) );
         encodedData[encodedIndex + 1] =
                 lookUpBase64Alphabet[val2 | k << 4];
         encodedData[encodedIndex + 2] =
                 lookUpBase64Alphabet[l << 2 | val3];
         encodedData[encodedIndex + 3] = lookUpBase64Alphabet[b3 & 0x3f];
      }

      // form integral number of 6-bit groups
      final int dataIndex1 = i * 3;
      final int encodedIndex1 = i << 2;
      if (fewerThan24bits == EIGHTBIT) {
         final byte b11 = binaryData[dataIndex1];
         final byte k1 = (byte) (b11 & 0x03);
         //log.debug("b1=" + b1);
         //log.debug("b1<<2 = " + (b1>>2) );
         final byte val1 = (b11 & SIGN) == 0 ? (byte) (b11 >> 2) : (byte) (b11 >> 2 ^ 0xc0);
         encodedData[encodedIndex1] = lookUpBase64Alphabet[((int) val1)];
         encodedData[encodedIndex1 + 1] = lookUpBase64Alphabet[k1 << 4];
         encodedData[encodedIndex1 + 2] = PAD;
         encodedData[encodedIndex1 + 3] = PAD;
      } else if (fewerThan24bits == SIXTEENBIT) {

         final byte b11 = binaryData[dataIndex1];
         final byte b21 = binaryData[dataIndex1 + 1];
         final byte l1 = (byte) (b21 & 0x0f);
         final byte k1 = (byte) (b11 & 0x03);

         final byte val1 = (b11 & SIGN) == 0 ? (byte) (b11 >> 2) : (byte) (b11 >> 2 ^ 0xc0);
         final byte val2 = (b21 & SIGN) == 0 ? (byte) (b21 >> 4) : (byte) (b21 >> 4 ^ 0xf0);

         encodedData[encodedIndex1] = lookUpBase64Alphabet[((int) val1)];
         encodedData[encodedIndex1 + 1] =
                 lookUpBase64Alphabet[val2 | k1 << 4];
         encodedData[encodedIndex1 + 2] = lookUpBase64Alphabet[l1 << 2];
         encodedData[encodedIndex1 + 3] = PAD;
      }

      return encodedData;
   }


   /**
    * Decodes Base64 data into octects
    *
    * @param base64Data Byte array containing Base64 data
    * @return Array containing decoded data.
    */
   public static byte[] decode(final byte[] base64Data) {
      //noinspection ZeroLengthArrayAllocation
      final byte[] emptyByteArray = {}; // NOPMD
      // handle the edge case, so we don't have to worry about it later
      if (base64Data.length == 0) {
         return emptyByteArray;
      } // NOPMD

      final int numberQuadruple = base64Data.length / FOURBYTE;

      // Throw away anything not in base64Data

      // this sizes the output array properly - rlw
      int lastData = base64Data.length;
      // ignore the '=' padding
      while (base64Data[lastData - 1] == PAD) {
         if (--lastData == 0) {
            return emptyByteArray; // NOPMD
         }
      }
      final byte[] decodedData = new byte[lastData - numberQuadruple];

      int dataIndex = 0;
      int encodedIndex = 0;
      byte marker1 = (byte) 0;
      byte marker0 = (byte) 0;
      byte b4 = (byte) 0;
      byte b3 = (byte) 0;
      byte b2 = (byte) 0;
      byte b1 = (byte) 0;
      for (int i = 0; i < numberQuadruple; i++) {
         dataIndex = i << 2;
         marker0 = base64Data[dataIndex + 2];
         marker1 = base64Data[dataIndex + 3];

         b1 = base64Alphabet[base64Data[dataIndex]];
         b2 = base64Alphabet[base64Data[dataIndex + 1]];

         if (marker0 != PAD && marker1 != PAD) {
            //No PAD e.g 3cQl
            b3 = base64Alphabet[((int) marker0)];
            b4 = base64Alphabet[((int) marker1)];

            decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
            decodedData[encodedIndex + 1] =
                    (byte) ((b2 & 0xf) << 4 | b3 >> 2 & 0xf);
            decodedData[encodedIndex + 2] = (byte) (b3 << 6 | b4);
         } else if (marker0 == PAD) {
            //Two PAD e.g. 3c[Pad][Pad]
            decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
         } else if (marker1 == PAD) {
            //One PAD e.g. 3cQ[Pad]
            b3 = base64Alphabet[((int) marker0)];

            decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
            decodedData[encodedIndex + 1] =
                    (byte) ((b2 & 0xf) << 4 | b3 >> 2 & 0xf);
         }
         encodedIndex += 3;
      }
      return decodedData;
   }


   /**
    * Helper method.
    *
    * @param s
    * @return
    */
   public static List makeList(final String s) {

      final List result = new ArrayList(1);
      result.add(s);
      return result;
   }


   public static StringBuffer appendChars(final StringBuffer buf, final int charCount,
                                          final char c) {

      for (int i = 0; i < charCount; i++) {
         buf.append(c);
      }
      return buf;
   }


   /**
    * Helper method.
    *
    * @param value
    * @return
    */
   public static boolean toBoolean(final String value) {

      return value.equalsIgnoreCase(STRING_TRUE)
              || value.equalsIgnoreCase(STRING_YES)
              || value.equalsIgnoreCase(STRING_ENABLED);
   }


   /**
    * Transform InetAddress to a pretty-printed string.
    *
    * @param address InetAddress to transform.
    * @return InetAddress as a pretty-printed string.
    */
   public static String toString(final InetAddress address) {

      return removeLeadingSlash(address.toString());
   }


   public static String toString(final InetSocketAddress endpoint) {

      return removeLeadingSlash(endpoint.toString());
   }


   /**
    * Helper method to transform a boolean value to as "YES" or "NO" string.
    *
    * @param value boolean value to transform.
    * @return a String containing "YES" or "NO".
    * @see #YES
    * @see #NO
    */
   public static String toYesNo(final boolean value) {

      return value ? YES : NO;
   }


   public static String toString(final Object[] objects) {

      if (objects == null) {
         return NULL_AS_STRING;
      }
      final StringBuilder sb = new StringBuilder(50);
      sb.append('[');
      for (int i = 0; i < objects.length; i++) {
         final Object o = objects[i];
         sb.append(o.toString());
         if (i < objects.length - 1) {
            sb.append(", ");
         }
      }
      sb.append(']');
      return sb.toString();
   }


   public static void dump(final byte[] b, final PrintStream out) {

      for (int i = 0; i < b.length; ++i) {
         if (i % 16 == 0) {
            out.print(Integer.toHexString(i & 0xFFFF | 0x10000).substring(1, 5) + " - ");
         }
         out.print(Integer.toHexString(b[i] & 0xFF | 0x100).substring(1, 3) + ' ');
         if (i % 16 == 15 || i == b.length - 1) {
            int j;
            for (j = 16 - i % 16; j > 1; --j) {
               out.print("   ");
            }
            out.print(" - ");
            final int start = (i / 16) * 16;
            final int end = b.length < i + 1 ? b.length : i + 1;
            for (j = start; j < end; ++j) {
               if (b[j] >= 32 && b[j] <= 126) {
                  out.print((char) b[j]);
               } else {
                  out.print(".");
               }
            }
            out.println();
         }
      }
      out.println();
   }


   public static String toString(final IntArrayList list) {

      final StringBuilder sb = new StringBuilder(20);
      if (list == null) {
         sb.append("null");
      } else {
         sb.append('[');
         sb.append("size:");
         final int listSize = list.size();
         sb.append(Integer.toString(listSize));
         if (!list.isEmpty()) {
            sb.append("; ");
            for (int i = 0; i < listSize; i++) {
               final Object o = list.get(i);
               sb.append(o);
               if (i < (listSize - 1)) {
                  sb.append(',');
               }
            }
         }
         sb.append(']');
      }
      return sb.toString();
   }


   public static String toShortName(final Class clazz) {

      final String name = clazz.getName();
      return name.substring(name.lastIndexOf('.') + 1);
   }


   public static String sizeToString(final Collection collection) {

      if (collection == null) {
         return "null";
      } else {
         return Integer.toString(collection.size());
      }
   }


   public static String sizeToString(final Hash hash) {

      return hash == null ? "null" : Integer.toString(hash.size());
   }


   public static String sizeToString(final IntArrayList intArrayList) {

      return intArrayList == null ? "null" : Integer.toString(intArrayList.size());
   }


   public static long readTime(final String stringTime) throws IllegalArgumentException {

      final Matcher matcher = TIME_PATTERN.matcher(stringTime);
      if (matcher.matches()) {

         final String stringValue = matcher.group(1);
         final String stringMeasure = matcher.group(2).toLowerCase();

         final long multiplier;
         if ("milliseconds".equals(stringMeasure) || "millis".equals(stringMeasure) || "ms".equals(stringMeasure)) {

            multiplier = 1;
         } else if ("seconds".equals(stringMeasure) || "secs".equals(stringMeasure) || "s".equals(stringMeasure)) {

            multiplier = 1000;
         } else if ("minutes".equals(stringMeasure) || "min".equals(stringMeasure)) {

            multiplier = 60000;
         } else {
            throw new IllegalArgumentException("Unknown measure: " + stringTime);
         }
         return Long.parseLong(stringValue) * multiplier;

      } else {

         throw new IllegalArgumentException("Unknown time format: " + stringTime);
      }
   }


   /**
    * Parses a size in bytes. Cacheonix supports bytes, kilobytes (k, kb), megabytes (mb) and gigabytes (gb) as a unit
    * of measure. Example: parsing "5mb" will return 5000000.
    *
    * @param stringBytes the size in bytes. Cacheonix supports bytes, kilobytes (k, kb), megabytes (mb) and gigabytes
    *                    (gb) as a unit of measure. Example: "5mb".
    * @return the size in bytes. Example: parsing "5mb" will return 5000000.
    * @throws IllegalArgumentException if the stringBytes cannot be parsed.
    */
   public static long readBytes(final String stringBytes) throws IllegalArgumentException {

      final Matcher matcher = BYTES_PATTERN.matcher(stringBytes);
      if (!matcher.matches()) {

         throw new IllegalArgumentException("Unknown byte size format: " + stringBytes);
      }

      final String stringValue = matcher.group(1);
      final String stringMeasure = matcher.group(2).toLowerCase();

      if ("b".equals(stringMeasure) || "bytes".equals(stringMeasure)) {

         return Long.parseLong(stringValue);
      } else if ("k".equals(stringMeasure) || "kb".equals(stringMeasure) || "kilobytes".equals(stringMeasure)) {

         return Long.parseLong(stringValue) * KILO_BYTE;
      } else if ("m".equals(stringMeasure) || "mb".equals(stringMeasure) || "megabytes".equals(stringMeasure)) {

         return Long.parseLong(stringValue) * MEGA_BYTE;
      } else if ("g".equals(stringMeasure) || "gb".equals(stringMeasure) || "gigabytes".equals(stringMeasure)) {

         return Long.parseLong(stringValue) * GIGA_BYTE;
      } else if ("%".equals(stringMeasure)) {

         final int percent = Integer.parseInt(stringValue);
         final int normalizedPercent = percent > 100 ? 100 : percent < 0 ? 0 : percent;
         return (long) ((double) Runtime.getRuntime().maxMemory() * ((double) normalizedPercent / (double) 100));
      } else {
         throw new IllegalArgumentException("Unknown measure: " + stringBytes);
      }
   }


   private static String removeLeadingSlash(final String str) {

      final int i = str.indexOf('/');
      return i >= 0 ? str.substring(i + 1) : str;
   }


   public static InetAddress readInetAddress(final String stringInetAddress) throws IllegalArgumentException {

      try {

         return InetAddress.getByName(stringInetAddress);
      } catch (final UnknownHostException e) {

         throw new IllegalArgumentException(e);
      }
   }
}
