/* 
 * Created on Apr 21, 2006
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
package org.springmodules.cache.interceptor.caching;

import java.io.Serializable;

import org.springframework.util.ObjectUtils;

final class NullObject implements Serializable {

  private static final long serialVersionUID = 3257007674280522803L;

  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof NullObject)) return false;
    return true;
  }

  public int hashCode() {
    return 80992;
  }

  public String toString() {
    String identity = ObjectUtils.getIdentityHexString(this);
    return getClass().getName() + "@" + identity;
  }
}