/*
 * Created on Apr 5, 2006
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
 * Copyright @2006 the original author or authors.
 */
package org.springmodules.cache.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springmodules.util.Objects;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * <p>
 * A cache element that stores <em>copies</em> of the given key and value.
 * </p>
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public class Element implements Serializable, Cloneable {

  private static final long DEFAULT_TIME_TO_LIVE_MS = 120000;

  private static final long EXPIRY_NEVER = -1l;

  private static Log logger = LogFactory.getLog(Element.class);

  private static final long serialVersionUID = -935757449385127201L;

  private final long creationTime;

  private final Serializable key;

  private final long timeToLive;

  private Serializable value;

  /**
   * Constructor. Entries created with this constructor never expire.
   *
   * <p>
   * The key and value stored in this element are copies of the ones passed as
   * arguments.
   * </p>
   *
   * @param newKey
   *          the new key for this entry
   * @param newValue
   *          the new value for this entry
   * @throws ObjectCannotBeCopiedException
   *           if the key or the value cannot be copied using serialization
   */
  public Element(Serializable newKey, Serializable newValue)
      throws ObjectCannotBeCopiedException {
    this(newKey, newValue, EXPIRY_NEVER);
  }

  /**
   * Constructor.
   *
   * <p>
   * The key and value stored in this element are copies of the ones passed as
   * arguments.
   * </p>
   *
   * @param newKey
   *          the new key for this entry
   * @param newValue
   *          the new value for this entry
   * @param newTimeToLive
   *          the number of milliseconds until the cache entry will expire
   * @throws ObjectCannotBeCopiedException
   *           if the key or the value cannot be copied using serialization
   */
  public Element(Serializable newKey, Serializable newValue, long newTimeToLive)
      throws ObjectCannotBeCopiedException {
    this(newKey, newValue, System.currentTimeMillis(), newTimeToLive);
  }

  private Element(Serializable newKey, Serializable newValue,
      long newCreationTime, long newTimeToLive)
      throws ObjectCannotBeCopiedException {
    super();
    key = copy(newKey);

    setValue(newValue);
    creationTime = newCreationTime;

    boolean invalidTimeToLive = newTimeToLive <= 0
        && newTimeToLive != EXPIRY_NEVER;
    timeToLive = invalidTimeToLive ? DEFAULT_TIME_TO_LIVE_MS : newTimeToLive;
  }

  /**
   * @see Object#clone()
   */
  public Object clone() {
    Element newElement = new Element(key, value, creationTime, timeToLive);
    return newElement;
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof Element)) {
      return false;
    }
    Element other = (Element) obj;
    if (!ObjectUtils.nullSafeEquals(key, other.key)) {
      return false;
    }
    if (!ObjectUtils.nullSafeEquals(value, other.value)) {
      return false;
    }
    return true;
  }

  /**
   * @return the creation time (in milliseconds) of this cache element
   */
  public final long getCreationTime() {
    return creationTime;
  }

  /**
   * @return the key of this cache element
   */
  public final Serializable getKey() {
    return key;
  }

  /**
   * @return the number of milliseconds until the cache entry will expire
   */
  public final long getTimeToLive() {
    return timeToLive;
  }

  /**
   * @return the value of this cache element
   */
  public final Serializable getValue() {
    return value;
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    int multiplier = 31;
    int hash = 7;
    hash = multiplier * hash + key.hashCode();
    hash = multiplier * hash + Objects.nullSafeHashCode(value);
    return hash;
  }

  /**
   * @return <code>true</code> if this cache element has not expired yet. This
   *         method always returns <code>true</code> for eternal entries.
   * @see #Element(Serializable, Serializable)
   */
  public final boolean isAlive() {
    if (timeToLive == EXPIRY_NEVER) {
      return true;
    }

    long currentTime = System.currentTimeMillis();
    long delta = currentTime - creationTime;

    return delta < timeToLive;
  }

  /**
   * @return <code>true</code> if this cache element has expired
   * @see #isAlive()
   */
  public final boolean isExpired() {
    return !isAlive();
  }

  /**
   * Sets the value for this cache element.
   *
   * @param newValue
   *          the new value for this cache element
   * @throws ObjectCannotBeCopiedException
   *           if the key or the value cannot be copied using serialization
   */
  public final void setValue(Serializable newValue)
      throws ObjectCannotBeCopiedException {
    value = copy(newValue);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
		return Objects.identityToString(this)
				.append("[key=").append(StringUtils.quoteIfString(key)).append(", ")
				.append("value=").append(StringUtils.quoteIfString(value)).append(", ")
				.append("creationTime=").append(new Date(creationTime)).append(", ")
				.append("timeToLive=").append(timeToLive).append("]")
				.toString();
  }

  private void close(InputStream closeable) {
    if (closeable == null) {
      return;
    }

    try {
      closeable.close();
    } catch (Exception exception) {
      String clazz = closeable.getClass().getName();
      logger.error("Unable to close " + clazz, exception);
    }
  }

  private void close(OutputStream closeable) {
    if (closeable == null) {
      return;
    }

    try {
      closeable.close();
    } catch (Exception exception) {
      String clazz = closeable.getClass().getName();
      logger.error("Unable to close " + clazz, exception);
    }
  }

  private Serializable copy(Serializable oldValue)
      throws ObjectCannotBeCopiedException {
    Serializable newValue = null;

    ByteArrayInputStream oldValueInputStream = null;
    ByteArrayOutputStream oldValueOutputStream = new ByteArrayOutputStream();

    ObjectInputStream newValueInputStream = null;
    ObjectOutputStream newValueOutputStream = null;

    try {
      newValueOutputStream = new ObjectOutputStream(oldValueOutputStream);
      newValueOutputStream.writeObject(oldValue);

      byte[] oldValueAsByteArray = oldValueOutputStream.toByteArray();
      oldValueInputStream = new ByteArrayInputStream(oldValueAsByteArray);

      newValueInputStream = new ObjectInputStream(oldValueInputStream);
      newValue = (Serializable) newValueInputStream.readObject();

    } catch (Exception exception) {
      String errMsg = "Unable to copy value " + oldValue;
      throw new ObjectCannotBeCopiedException(errMsg, exception);

    } finally {
      close(newValueInputStream);
      close(newValueOutputStream);
      close(oldValueInputStream);
      close(oldValueOutputStream);
    }
    return newValue;
  }
}
