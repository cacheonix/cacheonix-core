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

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown if an unexpected error occurs when working with the cache.
 * 
 * @author Alex Ruiz
 */
public abstract class CachingException extends NestedRuntimeException {

  /**
   * Constructor.
   * 
   * @param detailMessage
   *          the detail message
   */
  public CachingException(String detailMessage) {
    super(detailMessage);
  }

  /**
   * Constructor.
   * 
   * @param detailMessage
   *          the detail message
   * @param nested
   *          the nested exception
   */
  public CachingException(String detailMessage, Throwable nested) {
    super(detailMessage, nested);
  }

}
