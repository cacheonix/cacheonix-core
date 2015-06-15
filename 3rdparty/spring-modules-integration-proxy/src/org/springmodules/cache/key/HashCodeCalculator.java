/*
 * Created on Nov 19, 2004
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
 * Copyright @2004 the original author or authors.
 */

package org.springmodules.cache.key;

/**
 * <p>
 * Builds the checksum and hash code needed to create a
 * <code>{@link HashCodeCacheKey}</code>.
 * </p>
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public final class HashCodeCalculator {

  private static final int INITIAL_HASH = 17;

  private static final int MULTIPLIER = 37;

  private long checkSum;

  /**
   * Counts the number of times <code>{@link #append(int)}</code> is executed.
   * It is also used to build <code>{@link #checkSum}</code> and
   * <code>{@link #hashCode}</code>.
   */
  private int count;

  /**
   * Hash code to build;
   */
  private int hashCode;

  /**
   * Constructor.
   */
  public HashCodeCalculator() {
    super();
    hashCode = INITIAL_HASH;
  }

  /**
   * Recalculates <code>{@link #checkSum}</code> and
   * <code>{@link #hashCode}</code> using the specified value.
   *
   * @param value
   *          the specified value.
   */
  public void append(int value) {
    count++;
    int valueToAppend = count * value;

    hashCode = MULTIPLIER * hashCode + (valueToAppend ^ (valueToAppend >>> 16));
    checkSum += valueToAppend;
  }

  /**
   * @return the number that ensures that the combination hashCode/checSum is
   *         unique
   */
  public long getCheckSum() {
    return checkSum;
  }

  /**
   * @return the calculated hash code
   */
  public int getHashCode() {
    return hashCode;
  }
}