/* 
 * Created on Nov 23, 2005
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright @2005 the original author or authors.
 */
package org.springmodules.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Miscellaneous object utility methods.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class Objects {

  private static final String ARRAY_ELEMENT_SEPARATOR = ", ";

  private static final String ARRAY_END = "}";

  private static final String ARRAY_START = "{";

  private static final String EMPTY_ARRAY = "{}";

  private static final int INITIAL_HASH = 7;

  private static final int MULTIPLIER = 31;

  private static final String NULL_ARRAY = "null";

  private static final Set primitivesAndWrappers;

  static {
    primitivesAndWrappers = new HashSet();
    primitivesAndWrappers.add(boolean.class);
    primitivesAndWrappers.add(Boolean.class);
    primitivesAndWrappers.add(byte.class);
    primitivesAndWrappers.add(Byte.class);
    primitivesAndWrappers.add(char.class);
    primitivesAndWrappers.add(Character.class);
    primitivesAndWrappers.add(double.class);
    primitivesAndWrappers.add(Double.class);
    primitivesAndWrappers.add(float.class);
    primitivesAndWrappers.add(Float.class);
    primitivesAndWrappers.add(int.class);
    primitivesAndWrappers.add(Integer.class);
    primitivesAndWrappers.add(long.class);
    primitivesAndWrappers.add(Long.class);
    primitivesAndWrappers.add(short.class);
    primitivesAndWrappers.add(Short.class);
  }

  /**
   * Returns the same value as <code>{@link Boolean#hashCode()}</code>.
   * 
   * @param bool
   *          the given <code>boolean</code>.
   * @return the hash code for the given <code>boolean</code>.
   * @see Boolean#hashCode()
   */
  public static int hashCode(boolean bool) {
    return bool ? 1231 : 1237;
  }

  /**
   * Returns the same value as <code>{@link Double#hashCode()}</code>.
   * 
   * @param dbl
   *          the given <code>double</code>.
   * @return the hash code for the given <code>double</code>.
   * @see Double#hashCode()
   */
  public static int hashCode(double dbl) {
    long bits = Double.doubleToLongBits(dbl);
    return hashCode(bits);
  }

  /**
   * Returns the same value as <code>{@link Float#hashCode()}</code>.
   * 
   * @param flt
   *          the given <code>float</code>.
   * @return the hash code for the given <code>float</code>.
   * @see Float#hashCode()
   */
  public static int hashCode(float flt) {
    return Float.floatToIntBits(flt);
  }

  /**
   * Returns the same value as <code>{@link Long#hashCode()}</code>.
   * 
   * @param lng
   *          the given <code>long</code>.
   * @return the hash code for the given <code>long</code>.
   * @see Long#hashCode()
   */
  public static int hashCode(long lng) {
    return (int) (lng ^ (lng >>> 32));
  }

  /**
   * <p>
   * Returns a <code>StringBuffer</code> containing:
   * <ol>
   * <li>the class name of the given object</li>
   * <li>the character '@'</li>
   * <li>the hex string for the object's identity hash code</li>
   * </ol>
   * </p>
   * <p>
   * This method will return an empty <code>StringBuffer</code> if the given
   * object is <code>null</code>.
   * </p>
   * 
   * @param obj
   *          the given object.
   * @return a <code>StringBuffer</code> containing identity information of
   *         the given object.
   */
  public static StringBuffer identityToString(Object obj) {
    StringBuffer buffer = new StringBuffer();
    if (obj != null) {
      buffer.append(obj.getClass().getName());
      buffer.append("@");
      buffer.append(ObjectUtils.getIdentityHexString(obj));
    }
    return buffer;
  }

  /**
   * Returns <code>true</code> if the given object is an array of primitives.
   * 
   * @param array
   *          the given object to check.
   * @return <code>true</code> if the given object is an array of primitives.
   */
  public static boolean isArrayOfPrimitives(Object array) {
    boolean primitiveArray = false;

    if (array != null) {
      Class clazz = array.getClass();

      primitiveArray = clazz.isArray()
          && clazz.getComponentType().isPrimitive();
    }

    return primitiveArray;
  }

  /**
   * Returns <code>true</code> if the given class is any of the following:
   * <ul>
   * <li><code>boolean</code></li>
   * <li>Boolean</li>
   * <li><code>byte</code></li>
   * <li>Byte</li>
   * <li><code>char</code></li>
   * <li>Character</li>
   * <li><code>double</code></li>
   * <li>Double</li>
   * <li><code>float</code></li>
   * <li>Float</li>
   * <li><code>int</code></li>
   * <li>Integer</li>
   * <li><code>long</code></li>
   * <li>Long</li>
   * <li><code>short</code></li>
   * <li>Short</li>
   * </ul>
   * 
   * @param clazz
   *          the given class.
   * @return <code>true</code> if the given class represents a primitive or a
   *         wrapper, <code>false</code> otherwise.
   */
  public static boolean isPrimitiveOrWrapper(Class clazz) {
    return primitivesAndWrappers.contains(clazz);
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two <code>boolean</code> arrays <code>a</code> and <code>b</code>
   * such that <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * 
   * <p>
   * If <code>array</code> is <code>null</code>, this method returns 0.
   * </p>
   * 
   * @param array
   *          the array whose hash value to compute.
   * @return a content-based hash code for <code>array</code>.
   * @see #hashCode(boolean)
   */
  public static int nullSafeHashCode(boolean[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + hashCode(array[i]);
    }

    return hash;
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two <code>byte</code> arrays <code>a</code> and <code>b</code> such
   * that <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * 
   * <p>
   * If <code>array</code> is <code>null</code>, this method returns 0.
   * </p>
   * 
   * @param array
   *          the array whose hash value to compute.
   * @return a content-based hash code for <code>array</code>.
   */
  public static int nullSafeHashCode(byte[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + array[i];
    }

    return hash;
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two <code>char</code> arrays <code>a</code> and <code>b</code> such
   * that <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * 
   * <p>
   * If <code>array</code> is <code>null</code>, this method returns 0.
   * </p>
   * 
   * @param array
   *          the array whose hash value to compute.
   * @return a content-based hash code for <code>array</code>.
   */
  public static int nullSafeHashCode(char[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + array[i];
    }

    return hash;
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two <code>double</code> arrays <code>a</code> and <code>b</code> such
   * that <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * 
   * <p>
   * If <code>array</code> is <code>null</code>, this method returns 0.
   * </p>
   * 
   * @param array
   *          the array whose hash value to compute.
   * @return a content-based hash code for <code>array</code>.
   * @see #hashCode(double)
   */
  public static int nullSafeHashCode(double[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + hashCode(array[i]);
    }

    return hash;
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two <code>float</code> arrays <code>a</code> and <code>b</code> such
   * that <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * 
   * <p>
   * If <code>array</code> is <code>null</code>, this method returns 0.
   * </p>
   * 
   * @param array
   *          the array whose hash value to compute.
   * @return a content-based hash code for <code>array</code>.
   * @see #hashCode(float)
   */
  public static int nullSafeHashCode(float[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + hashCode(array[i]);
    }

    return hash;
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two <code>int</code> arrays <code>a</code> and <code>b</code> such
   * that <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * 
   * <p>
   * If <code>array</code> is <code>null</code>, this method returns 0.
   * </p>
   * 
   * @param array
   *          the array whose hash value to compute.
   * @return a content-based hash code for <code>array</code>.
   */
  public static int nullSafeHashCode(int[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + array[i];
    }

    return hash;
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two <code>long</code> arrays <code>a</code> and <code>b</code> such
   * that <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * 
   * <p>
   * If <code>array</code> is <code>null</code>, this method returns 0.
   * </p>
   * 
   * @param array
   *          the array whose hash value to compute.
   * @return a content-based hash code for <code>array</code>.
   * @see #hashCode(long)
   */
  public static int nullSafeHashCode(long[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + hashCode(array[i]);
    }

    return hash;
  }

  /**
   * <p>
   * Returns the value of <code>{@link Object#hashCode()}</code>. If the
   * object is an array, this method will delegate to any of the
   * <code>nullSafeHashCode</code> methods for arrays in this class.
   * </p>
   * 
   * <p>
   * If the object is <code>null</code>, this method returns 0.
   * </p>
   * 
   * @param obj
   *          the object whose hash value to compute.
   * @return the hash code of the given object.
   * @see #nullSafeHashCode(boolean[])
   * @see #nullSafeHashCode(byte[])
   * @see #nullSafeHashCode(char[])
   * @see #nullSafeHashCode(double[])
   * @see #nullSafeHashCode(float[])
   * @see #nullSafeHashCode(int[])
   * @see #nullSafeHashCode(long[])
   * @see #nullSafeHashCode(Object[])
   * @see #nullSafeHashCode(short[])
   */
  public static int nullSafeHashCode(Object obj) {
    if (obj == null)
      return 0;

    if (obj instanceof boolean[]) {
      return nullSafeHashCode((boolean[]) obj);
    }
    if (obj instanceof byte[]) {
      return nullSafeHashCode((byte[]) obj);
    }
    if (obj instanceof char[]) {
      return nullSafeHashCode((char[]) obj);
    }
    if (obj instanceof double[]) {
      return nullSafeHashCode((double[]) obj);
    }
    if (obj instanceof float[]) {
      return nullSafeHashCode((float[]) obj);
    }
    if (obj instanceof int[]) {
      return nullSafeHashCode((int[]) obj);
    }
    if (obj instanceof long[]) {
      return nullSafeHashCode((long[]) obj);
    }
    if (obj instanceof short[]) {
      return nullSafeHashCode((short[]) obj);
    }
    if (obj instanceof Object[]) {
      return nullSafeHashCode((Object[]) obj);
    }

    return obj.hashCode();
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two arrays <code>a</code> and <code>b</code> such that
   * <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * <p>
   * The value returned by this method is equal to the value that would be
   * returned by <code>Arrays.asList(a).hashCode()</code>, unless
   * <code>array</code> is <code>null</code>, in which case <code>0</code>
   * is returned.
   * </p>
   * 
   * @param array
   *          the array whose content-based hash code to compute.
   * @return a content-based hash code for <code>array</code>.
   */
  public static int nullSafeHashCode(Object[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + nullSafeHashCode(array[i]);
    }

    return hash;
  }

  /**
   * <p>
   * Returns a hash code based on the contents of the specified array. For any
   * two <code>short</code> arrays <code>a</code> and <code>b</code> such
   * that <code>Arrays.equals(a, b)</code>, it is also the case that
   * <code>Arrays.hashCode(a) == Arrays.hashCode(b)</code>.
   * </p>
   * 
   * <p>
   * If <code>array</code> is <code>null</code>, this method returns 0.
   * 
   * @param array
   *          the array whose hash value to compute
   * @return a content-based hash code for <code>array</code>
   */
  public static int nullSafeHashCode(short[] array) {
    if (array == null)
      return 0;

    int hash = INITIAL_HASH;
    int arraySize = array.length;
    for (int i = 0; i < arraySize; i++) {
      hash = MULTIPLIER * hash + array[i];
    }

    return hash;
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(boolean[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(array[i]);
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(byte[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(array[i]);
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(char[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append("'" + array[i] + "'");
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(double[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(array[i]);
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(float[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(array[i]);
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(int[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(array[i]);
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(long[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(array[i]);
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(Object[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(StringUtils.quoteIfString(array[i]));
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(short[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(array[i]);
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }

  /**
   * Returns a string representation of the contents of the specified array. The
   * string representation consists of a list of the array's elements, enclosed
   * in curly braces (<code>"{}"</code>). Adjacent elements are separated by
   * the characters <code>", "</code> (a comma followed by a space). Returns
   * <code>"null"</code> if <code>array</code> is <code>null</code>.
   * 
   * @param array
   *          the array whose string representation to return.
   * @return a string representation of <code>array</code>.
   */
  public static String nullSafeToString(String[] array) {
    if (array == null)
      return NULL_ARRAY;

    int length = array.length;
    if (length == 0)
      return EMPTY_ARRAY;

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < length; i++) {
      if (i == 0)
        buffer.append(ARRAY_START);
      else
        buffer.append(ARRAY_ELEMENT_SEPARATOR);

      buffer.append(StringUtils.quote(array[i]));
    }

    buffer.append(ARRAY_END);
    return buffer.toString();
  }
}
