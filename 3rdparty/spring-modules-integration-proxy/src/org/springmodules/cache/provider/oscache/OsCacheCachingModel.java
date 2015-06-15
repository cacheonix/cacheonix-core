/* 
 * Created on Nov 10, 2004
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
 * Copyright @2004 the original author or authors.
 */

package org.springmodules.cache.provider.oscache;

import java.util.Arrays;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.util.Objects;

/**
 * <p>
 * Configuration options needed to store, retrieve and remove objects from
 * OSCache. All properties are optional.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class OsCacheCachingModel implements CachingModel {

  private static final long serialVersionUID = 3904681574367770928L;

  private String cronExpression;

  private String[] groups;

  private Integer refreshPeriod;

  /**
   * Constructor.
   */
  public OsCacheCachingModel() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param csvGroups
   *          comma-delimited list containing the names of the groups to use
   * @param refreshPeriod
   *          how long the object can stay in the cache (in seconds)
   */
  public OsCacheCachingModel(String csvGroups, int refreshPeriod) {
    this();
    setGroups(csvGroups);
    setRefreshPeriod(refreshPeriod);
  }

  /**
   * Constructor.
   * 
   * @param csvGroups
   *          comma-delimited list containing the names of the groups to use
   * @param refreshPeriod
   *          how long the object can stay in the cache (in seconds)
   * @param cronExpression
   *          cron expression that the age of the cache entry will be compared
   *          to. If the entry is older than the most recent match for the cron
   *          expression, the entry will be considered stale
   */
  public OsCacheCachingModel(String csvGroups, int refreshPeriod,
      String cronExpression) {
    this(csvGroups, new Integer(refreshPeriod), cronExpression);
  }

  /**
   * Constructor.
   * 
   * @param csvGroups
   *          comma-delimited list containing the names of the groups to use
   * @param refreshPeriod
   *          how long the object can stay in the cache (in seconds)
   * @param cronExpression
   *          cron expression that the age of the cache entry will be compared
   *          to. If the entry is older than the most recent match for the cron
   *          expression, the entry will be considered stale
   */
  public OsCacheCachingModel(String csvGroups, Integer refreshPeriod,
      String cronExpression) {
    this(csvGroups, cronExpression);
    setRefreshPeriod(refreshPeriod);
  }

  /**
   * Constructor.
   * 
   * @param csvGroups
   *          comma-delimited list containing the names of the groups to use
   * @param cronExpression
   *          cron expression that the age of the cache entry will be compared
   *          to. If the entry is older than the most recent match for the cron
   *          expression, the entry will be considered stale
   */
  public OsCacheCachingModel(String csvGroups, String cronExpression) {
    this();
    setCronExpression(cronExpression);
    setGroups(csvGroups);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OsCacheCachingModel)) {
      return false;
    }

    OsCacheCachingModel cachingModel = (OsCacheCachingModel) obj;

    if (!ObjectUtils
        .nullSafeEquals(cronExpression, cachingModel.cronExpression)) {
      return false;
    }
    if (!Arrays.equals(groups, cachingModel.groups)) {
      return false;
    }
    if (!ObjectUtils.nullSafeEquals(refreshPeriod, cachingModel.refreshPeriod)) {
      return false;
    }

    return true;
  }

  /**
   * @return the cron expression that the age of the cache entry will be
   *         compared to. If the entry is older than the most recent match for
   *         the cron expression, the entry will be considered stale
   */
  public final String getCronExpression() {
    return cronExpression;
  }

  /**
   * @return the groups to use
   */
  public final String[] getGroups() {
    return groups;
  }

  /**
   * @return how long the object can stay in the cache (in seconds)
   */
  public final Integer getRefreshPeriod() {
    return refreshPeriod;
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    int multiplier = 31;
    int hash = 17;
    hash = multiplier * hash + Objects.nullSafeHashCode(cronExpression);
    hash = multiplier * hash + Objects.nullSafeHashCode(groups);
    hash = multiplier * hash + Objects.nullSafeHashCode(refreshPeriod);
    return hash;
  }

  /**
   * Sets the cron expression that the age of the cache entry will be compared
   * to. If the entry is older than the most recent match for the cron
   * expression, the entry will be considered stale
   * 
   * @param newCronExpression
   *          the new cron expression
   */
  public final void setCronExpression(String newCronExpression) {
    cronExpression = newCronExpression;
  }

  /**
   * Sets the cache groups from a comma-delimited list of values.
   * 
   * @param csvGroups
   *          the comma-delimited list of values containing the cache groups.
   */
  public final void setGroups(String csvGroups) {
    String[] newGroups = null;
    if (StringUtils.hasText(csvGroups)) {
      newGroups = StringUtils.commaDelimitedListToStringArray(csvGroups);
    }
    setGroups(newGroups);
  }

  /**
   * Sets the groups to use.
   * 
   * @param newGroups
   *          the new groups
   */
  public final void setGroups(String[] newGroups) {
    groups = newGroups;
  }

  /**
   * Sets how long the object can stay in the cache (in seconds).
   * 
   * @param newRefreshPeriod
   *          the new refresh period
   */
  public final void setRefreshPeriod(int newRefreshPeriod) {
    setRefreshPeriod(new Integer(newRefreshPeriod));
  }

  /**
   * Sets how long the object can stay in the cache (in seconds).
   * 
   * @param newRefreshPeriod
   *          the new refresh period
   */
  public final void setRefreshPeriod(Integer newRefreshPeriod) {
    refreshPeriod = newRefreshPeriod;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    StringBuffer buffer = Objects.identityToString(this);
    buffer.append("[refreshPeriod=" + refreshPeriod + ", ");
    buffer.append("groups=" + Objects.nullSafeToString(groups) + ", ");
    buffer.append("cronExpression=" + StringUtils.quote(cronExpression) + "]");

    return buffer.toString();
  }
}