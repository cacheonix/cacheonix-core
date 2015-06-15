/* 
 * Created on Oct 1, 2005
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
package org.springmodules.cache.provider;

import org.springmodules.cache.FlushingModel;
import org.springmodules.util.Objects;

/**
 * <p>
 * Template for implementations of <code>{@link FlushingModel}</code>.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractFlushingModel implements FlushingModel {

  private boolean flushBeforeMethodExecution;

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AbstractFlushingModel)) {
      return false;
    }
    AbstractFlushingModel model = (AbstractFlushingModel) obj;
    if (flushBeforeMethodExecution != model.flushBeforeMethodExecution) {
      return false;
    }
    return true;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int multiplier = 31;
    int hash = 7;
    hash = multiplier * hash + (Objects.hashCode(flushBeforeMethodExecution));
    return hash;
  }

  /**
   * @see FlushingModel#flushBeforeMethodExecution()
   */
  public final boolean flushBeforeMethodExecution() {
    return flushBeforeMethodExecution;
  }

  /**
   * Sets the flag that indicates if the cache should be flushed before or after
   * the execution of an intercepted method.
   * 
   * @param newFlushBeforeMethodExecution
   *          the new value for the flag
   */
  public final void setFlushBeforeMethodExecution(
      boolean newFlushBeforeMethodExecution) {
    flushBeforeMethodExecution = newFlushBeforeMethodExecution;
  }

}
