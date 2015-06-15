/* 
 * Created on Oct 7, 2004
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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

import org.springmodules.cache.util.Reflections;
import org.springmodules.util.Objects;

/**
 * <p>
 * Generates the key for a cache entry using the hashCode of the intercepted
 * method and its arguments.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class HashCodeCacheKeyGenerator implements CacheKeyGenerator {

  /**
   * Flag that indicates if this generator should generate the hash code of the
   * arguments passed to the method to apply caching to. If <code>false</code>,
   * this generator uses the default hash code of the arguments.
   */
  private boolean generateArgumentHashCode;

  /**
   * Construct a <code>HashCodeCacheKeyGenerator</code>.
   */
  public HashCodeCacheKeyGenerator() {
    super();
  }

  /**
   * Construct a <code>HashCodeCacheKeyGenerator</code>.
   * 
   * @param generateArgumentHashCode
   *          the new value for the flag that indicates if this generator should
   *          generate the hash code of the arguments passed to the method to
   *          apply caching to. If <code>false</code>, this generator uses
   *          the default hash code of the arguments.
   */
  public HashCodeCacheKeyGenerator(boolean generateArgumentHashCode) {
    this();
    setGenerateArgumentHashCode(generateArgumentHashCode);
  }

  /**
   * @see CacheKeyGenerator#generateKey(MethodInvocation)
   */
  public final Serializable generateKey(MethodInvocation methodInvocation) {
    HashCodeCalculator hashCodeCalculator = new HashCodeCalculator();

    Method method = methodInvocation.getMethod();
    hashCodeCalculator.append(System.identityHashCode(method));

    Object[] methodArguments = methodInvocation.getArguments();
    if (methodArguments != null) {
      int methodArgumentCount = methodArguments.length;

      for (int i = 0; i < methodArgumentCount; i++) {
        Object methodArgument = methodArguments[i];
        int hash = 0;

        if (generateArgumentHashCode) {
          hash = Reflections.reflectionHashCode(methodArgument);
        } else {
          hash = Objects.nullSafeHashCode(methodArgument);
        }

        hashCodeCalculator.append(hash);
      }
    }

    long checkSum = hashCodeCalculator.getCheckSum();
    int hashCode = hashCodeCalculator.getHashCode();

    Serializable cacheKey = new HashCodeCacheKey(checkSum, hashCode);
    return cacheKey;
  }

  /**
   * Sets the flag that indicates if this generator should generate the hash
   * code of the arguments passed to the method to apply caching to. If
   * <code>false</code>, this generator uses the default hash code of the
   * arguments.
   * 
   * @param newGenerateArgumentHashCode
   *          the new value of the flag
   */
  public final void setGenerateArgumentHashCode(
      boolean newGenerateArgumentHashCode) {
    generateArgumentHashCode = newGenerateArgumentHashCode;
  }

}