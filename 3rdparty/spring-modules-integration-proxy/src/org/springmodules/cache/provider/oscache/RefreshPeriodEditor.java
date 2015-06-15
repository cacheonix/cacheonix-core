/* 
 * Created on Sep 9, 2005
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
package org.springmodules.cache.provider.oscache;

import java.beans.PropertyEditorSupport;

import com.opensymphony.oscache.base.CacheEntry;

import org.springframework.util.StringUtils;

/**
 * <p>
 * Property editor for the property "refreshPeriod" of
 * <code>{@link OsCacheCachingModel}</code>
 * </p>
 * 
 * @author Alex Ruiz
 */
public class RefreshPeriodEditor extends PropertyEditorSupport {

  /**
   * Indicates that a cache entry never gets expired.
   */
  public static final String INDEFINITE_EXPIRY = "INDEFINITE_EXPIRY";

  /**
   * @throws IllegalArgumentException
   *           if the given text is not a parseable number or not equal to the
   *           value of <code>INDEFINITE_EXPIRY</code>.
   * @see java.beans.PropertyEditorSupport#setAsText(String)
   */
  public void setAsText(String newText) throws IllegalArgumentException {
    Integer refreshPeriod = null;

    if (INDEFINITE_EXPIRY.equalsIgnoreCase(newText)) {
      refreshPeriod = new Integer(CacheEntry.INDEFINITE_EXPIRY);

    } else if (StringUtils.hasText(newText)) {
      try {
        refreshPeriod = new Integer(newText);

      } catch (NumberFormatException numberFormatException) {
        throw new IllegalArgumentException(StringUtils.quote(newText)
            + " is not a valid value. Refresh period should be an integer "
            + "or the String " + StringUtils.quote(INDEFINITE_EXPIRY));
      }
    }

    setValue(refreshPeriod);
  }
}