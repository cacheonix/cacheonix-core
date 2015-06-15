/* 
 * Created on Apr 6, 2005
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
package org.springmodules.cache.util;

/**
 * <p>
 * Text Matcher.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class TextMatcher {

  /**
   * <p>
   * Returns <code>true</code> if the given text matches the base text. The
   * default implementation checks for "xxx*" and "*xxx" matches.
   * </p>
   * <p>
   * For example "getName" should match "getName", "getN*" and "get*".
   * </p>
   * 
   * @param text
   *          the text to match.
   * @param baseText
   *          the base text.
   * @return <code>true</code> if the text matches.
   */
  public static boolean isMatch(String text, String baseText) {
    boolean match = (baseText.endsWith("*") && text.startsWith(baseText
        .substring(0, baseText.length() - 1)))
        || (baseText.startsWith("*") && text.endsWith(baseText.substring(1,
            baseText.length())));

    return match;
  }
}
