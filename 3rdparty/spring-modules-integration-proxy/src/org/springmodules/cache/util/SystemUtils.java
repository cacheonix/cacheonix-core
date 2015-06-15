/* 
 * Created on Mar 25, 2006
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
package org.springmodules.cache.util;

import org.springframework.util.ClassUtils;

/**
 * <p>
 * System utilities.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class SystemUtils {

  /**
   * @return <code>true</code> if the current version of Java is 5 or later
   */
  public static boolean atLeastJ2SE5() {
    try {
      ClassUtils.forName("java.lang.annotation.Annotation");
      return true;

    } catch (ClassNotFoundException exception) {
      return false;
    }
  }
}
