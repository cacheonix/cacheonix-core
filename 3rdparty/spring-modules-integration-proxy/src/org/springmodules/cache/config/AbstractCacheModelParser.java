/* 
 * Created on Mar 13, 2006
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
package org.springmodules.cache.config;

import org.w3c.dom.Element;

import org.springmodules.cache.FlushingModel;

/**
 * <p>
 * Template for implementations of <code>{@link CacheModelParser}</code>.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractCacheModelParser implements CacheModelParser {

  /**
   * @see CacheModelParser#parseFlushingModel(Element)
   */
  public final FlushingModel parseFlushingModel(Element element) {
    String whenToFlush = element.getAttribute("when");
    boolean flushBeforeMethodExecution = "before".equals(whenToFlush);
    return doParseFlushingModel(element, flushBeforeMethodExecution);
  }

  /**
   * Creates an instance of <code>{@link FlushingModel}</code> by parsing the
   * given XML element.
   * 
   * @param element
   *          the XML element to parse
   * @param flushBeforeMethodExecution
   *          indicates if the cache should be flushed before or after the
   *          execution of the intercepted method
   * @return the created flushing model
   */
  protected abstract FlushingModel doParseFlushingModel(Element element,
      boolean flushBeforeMethodExecution);
}
