/* 
 * Created on Sep 24, 2004
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

package org.springmodules.cache.provider.jcs;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.util.Objects;

/**
 * <p>
 * Configuration options needed to store, retrieve and remove objects from JCS.
 * 
 * @author Alex Ruiz
 */
public class JcsCachingModel implements CachingModel {

  private static final long serialVersionUID = 3257282547976057398L;

  private String cacheName;

  private String group;

  /**
   * Constructor.
   */
  public JcsCachingModel() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param cacheName
   *          the name of the JCS cache to use.
   */
  public JcsCachingModel(String cacheName) {
    this();
    setCacheName(cacheName);
  }

  /**
   * Constructor.
   * 
   * @param cacheName
   *          the name of the JCS cache to use
   * @param group
   *          the name of the group to use. This group belongs to the specified
   *          cache
   */
  public JcsCachingModel(String cacheName, String group) {
    this(cacheName);
    setGroup(group);
  }

  /**
   * @see Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof JcsCachingModel)) {
      return false;
    }

    JcsCachingModel cachingModel = (JcsCachingModel) obj;

    if (!ObjectUtils.nullSafeEquals(cacheName, cachingModel.cacheName)) {
      return false;
    }
    if (!ObjectUtils.nullSafeEquals(group, cachingModel.group)) {
      return false;
    }

    return true;
  }

  /**
   * @return the name of the JCS cache to use
   */
  public final String getCacheName() {
    return cacheName;
  }

  /**
   * @return the name of the group to use. The group returned by this getter
   *         belongs to the cache returned by the cache specified by
   *         <code>getCacheName()</code>
   */
  public final String getGroup() {
    return group;
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    int multiplier = 31;
    int hash = 7;
    hash = multiplier * hash + Objects.nullSafeHashCode(cacheName);
    hash = multiplier * hash + Objects.nullSafeHashCode(group);
    return hash;
  }

  /**
   * Sets the name of the cache name to use.
   * 
   * @param newCacheName
   *          the new name of the cache
   */
  public final void setCacheName(String newCacheName) {
    cacheName = newCacheName;
  }

  /**
   * Sets the name of the group to use. The group should belong to the cache
   * specified in this model.
   * 
   * @param newGroup
   *          the new name of the group
   */
  public final void setGroup(String newGroup) {
    group = newGroup;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    StringBuffer buffer = Objects.identityToString(this);
    buffer.append("[cacheName=" + StringUtils.quote(cacheName) + ", ");
    buffer.append("group=" + StringUtils.quote(group) + "]");

    return buffer.toString();
  }
}