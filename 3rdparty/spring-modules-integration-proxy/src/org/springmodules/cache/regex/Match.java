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
 * Represents the result of single regular expression match.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class Match {

  private String[] groups;

  private boolean successful;

  /**
   * Constructor.
   * 
   * @param newSuccessful
   *          flag that indicates whether the match is successful
   * @param newGroups
   *          the groups contained in the result. This number includes the 0th
   *          group. In other words, the result refers to the number of
   *          parenthesized subgroups plus the entire match itself
   */
  protected Match(boolean newSuccessful, String[] newGroups) {
    super();

    successful = newSuccessful;
    groups = newGroups;
  }

  /**
   * @return the groups contained in the result. This number includes the 0th
   *         group. In other words, the result refers to the number of
   *         parenthesized subgroups plus the entire match itself
   */
  public String[] getGroups() {
    return groups;
  }

  /**
   * @return <code>true</code> if the match was successful, <code>false</code>
   *         otherwise
   */
  public boolean isSuccessful() {
    return successful;
  }

}
