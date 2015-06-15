/*
 * Created on Jan 25, 2006
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
 * Copyright @2007 the original author or authors.
 */
package org.springmodules.cache.provider.tangosol;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.util.Objects;

/**
 * <p>
 * Configuration options needed to store, retrieve and remove objects from
 * Tangosol Coherence.
 * </p>
 *
 * @author Alex Ruiz
 */
public class CoherenceCachingModel implements CachingModel {

  private static final long serialVersionUID = -3162399503493313393L;

  private String cacheName;

  private Long timeToLive;

  /**
   * Constructor.
   */
  public CoherenceCachingModel() {
    super();
  }

  /**
   * Constructor.
   *
   * @param newCacheName
   *          the name of the cache to use
   */
  public CoherenceCachingModel(String newCacheName) {
    this();
    setCacheName(newCacheName);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CoherenceCachingModel)) {
      return false;
    }

    CoherenceCachingModel cachingModel = (CoherenceCachingModel) obj;

    if (!ObjectUtils.nullSafeEquals(cacheName, cachingModel.cacheName)) {
      return false;
    }
    if (!ObjectUtils.nullSafeEquals(timeToLive, cachingModel.timeToLive)) {
      return false;
    }

    return true;
  }

  /**
   * @return the name of the cache to use
   */
  public final String getCacheName() {
    return cacheName;
  }

  /**
   * @return how long (in milliseconds) a given entry should stay in the cache
   */
  public final Long getTimeToLive() {
    return timeToLive;
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    int multiplier = 31;
    int hash = 7;
    hash = multiplier * hash + (Objects.nullSafeHashCode(cacheName));
    hash = multiplier * hash + (Objects.nullSafeHashCode(timeToLive));
    return hash;
  }

  /**
   * Sets the name of the cache to use.
   *
   * @param newCacheName
   *          the new cache name to set
   */
  public final void setCacheName(String newCacheName) {
    cacheName = newCacheName;
  }

  /**
   * Sets the time in milliseconds an entry should stay in the cache
   *
   * @param newTimeToLive
   *          the new time to set
   */
  public void setTimeToLive(long newTimeToLive) {
    setTimeToLive(new Long(newTimeToLive));
  }

  /**
   * Sets the time in milliseconds an entry should stay in the cache
   *
   * @param newTimeToLive
   *          the new time to set
   */
  public final void setTimeToLive(Long newTimeToLive) {
    timeToLive = newTimeToLive;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
	StringBuffer buffer = Objects.identityToString(this);
	buffer.append("[cacheName=")
			.append(StringUtils.quote(cacheName))
			.append(", ")
			.append("timeToLive=")
			.append(timeToLive)
			.append("]");
	return buffer.toString();
  }

}
