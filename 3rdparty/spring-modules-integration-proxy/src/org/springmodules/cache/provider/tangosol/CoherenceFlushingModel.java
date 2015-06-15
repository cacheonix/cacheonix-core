/* 
 * Created on Jan 27, 2006
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
package org.springmodules.cache.provider.tangosol;

import java.util.Arrays;

import org.springframework.util.StringUtils;

import org.springmodules.cache.provider.AbstractFlushingModel;
import org.springmodules.util.Objects;

/**
 * <p>
 * Configuration options needed to flush one or more caches from Tangosol
 * Coherence.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class CoherenceFlushingModel extends AbstractFlushingModel {

  private static final long serialVersionUID = -1170255199987704355L;

  /**
   * Names of the caches to flush.
   */
  private String[] cacheNames;

  /**
   * Constructor.
   */
  public CoherenceFlushingModel() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param csvCacheNames
   *          a comma-separated list containing the names of the EHCache caches
   *          to flush separated by commas
   */
  public CoherenceFlushingModel(String csvCacheNames) {
    this();
    setCacheNames(csvCacheNames);
  }

  /**
   * Constructor.
   * 
   * @param newCacheNames
   *          the names of the caches to flush
   */
  public CoherenceFlushingModel(String[] newCacheNames) {
    this();
    setCacheNames(newCacheNames);
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CoherenceFlushingModel)) {
      return false;
    }
    CoherenceFlushingModel flushingModel = (CoherenceFlushingModel) obj;
    if (!Arrays.equals(cacheNames, flushingModel.cacheNames)) {
      return false;
    }
    return true;
  }

  /**
   * @return the names of the caches to flush
   */
  public final String[] getCacheNames() {
    return cacheNames;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int multiplier = 31;
    int hash = 7;
    hash = multiplier * hash + Objects.nullSafeHashCode(cacheNames);
    return hash;
  }

  /**
   * Sets the names of the caches to flush.
   * 
   * @param csvCacheNames
   *          a comma-separated list of Strings containing the names of the
   *          caches to flush.
   */
  public void setCacheNames(String csvCacheNames) {
    String[] newCacheNames = null;
    if (csvCacheNames != null) {
      newCacheNames = StringUtils
          .commaDelimitedListToStringArray(csvCacheNames);
    }
    setCacheNames(newCacheNames);
  }

  /**
   * Sets the names of the caches to flush.
   * 
   * @param newCacheNames
   *          the names of the caches to flush
   */
  public final void setCacheNames(String[] newCacheNames) {
    cacheNames = newCacheNames;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer buffer = Objects.identityToString(this);
    buffer.append("[cacheNames=" + Objects.nullSafeToString(cacheNames) + ", ");
    buffer.append("flushBeforeMethodExecution="
        + flushBeforeMethodExecution() + "]");
    return buffer.toString();
  }

}
