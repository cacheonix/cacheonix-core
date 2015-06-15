/* 
 * Created on Apr 26, 2006
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
 * Copyright @2004-2006 the original author or authors.
 */
package org.springmodules.cache;

/**
 * Understands configuration errors on any resource of the caching module.
 * 
 * @author Alex Ruiz
 */
public abstract class ConfigurationError {

  public static FatalCacheException missingRequiredProperty(String property) {
    throw new FatalCacheException("Missing property <" + property + ">");
  }  
  
  private ConfigurationError() {}
}
