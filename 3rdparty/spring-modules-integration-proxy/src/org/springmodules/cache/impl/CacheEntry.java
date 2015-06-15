/* 
 * Created on Apr 13, 2006
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

/**
 * Template for cache entries. Cache entries wrap <code>{@link Element}</code>s
 * and encapsulate storage details.
 * 
 * @author Alex Ruiz
 */
public abstract class CacheEntry {

  /**
   * Cache element stored in this entry.
   */
  public Element element;

  /**
   * Hash code for this entry.
   */
  public final int hash;

  /**
   * Points to the next entry in the bucket this entry is assigned to.
   */
  public CacheEntry next;

  /**
   * Constructor.
   * 
   * @param newElement
   *          the new element for this entry
   * @param newHash
   *          the new hash code for this entry
   */
  public CacheEntry(Element newElement, final int newHash) {
    super();
    element = newElement;
    hash = newHash;
  }

  /**
   * Constructor.
   * 
   * @param newElement
   *          the new element for this entry
   * @param newHash
   *          the new hash code for this entry
   * @param newNext
   *          the new pointer to the next entry in the bucket
   */
  public CacheEntry(Element newElement, final int newHash, CacheEntry newNext) {
    this(newElement, newHash);
    next = newNext;
  }
}
