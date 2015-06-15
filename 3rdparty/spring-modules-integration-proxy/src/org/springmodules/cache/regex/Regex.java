/* 
 * Created on Mar 8, 2005
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
package org.springmodules.cache.regex;

/**
 * <p>
 * Represents an inmutable regular expression.
 * </p>
 * 
 * @author Alex Ruiz
 */
public interface Regex {

  /**
   * @return the pattern used to compile this regular expression.
   */
  String getPattern();

  /**
   * Attempts to match the entire input sequence against the given regular
   * expression.
   * 
   * @param input
   *          the input sequence.
   * @return the result of the match operation.
   */
  Match match(String input);
}
