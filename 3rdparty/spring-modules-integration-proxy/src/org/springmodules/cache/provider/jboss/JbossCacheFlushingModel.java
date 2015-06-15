/* 
 * Created on Sep 29, 2005
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
package org.springmodules.cache.provider.jboss;

import java.util.Arrays;

import org.springframework.util.StringUtils;

import org.springmodules.cache.provider.AbstractFlushingModel;
import org.springmodules.util.Objects;

/**
 * <p>
 * Configuration options needed to remove one or more nodes from JBossCache.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class JbossCacheFlushingModel extends AbstractFlushingModel {

  private static final long serialVersionUID = 7299844898815952890L;

  /**
   * FQN of the nodes to remove from the cache.
   */
  private String[] nodes;

  /**
   * Constructor.
   */
  public JbossCacheFlushingModel() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param csvNodes
   *          a comma-separated list containing the names of the nodes to remove
   *          from the cache
   */
  public JbossCacheFlushingModel(String csvNodes) {
    this();
    setNodes(csvNodes);
  }

  /**
   * Constructor.
   * 
   * @param newNodes
   *          the names of the nodes to remove from the cache
   */
  public JbossCacheFlushingModel(String[] newNodes) {
    this();
    setNodes(newNodes);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof JbossCacheFlushingModel)) {
      return false;
    }
    JbossCacheFlushingModel flushingModel = (JbossCacheFlushingModel) obj;
    if (!Arrays.equals(nodes, flushingModel.nodes)) {
      return false;
    }
    return true;
  }

  /**
   * @return the names of the nodes to remove from the cache
   */
  public String[] getNodes() {
    return nodes;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int multiplier = 31;
    int hash = 7;
    hash = multiplier * hash + Objects.nullSafeHashCode(nodes);
    return hash;
  }

  /**
   * Sets the names of the nodes to remove from the cache.
   * 
   * @param csvNodes
   *          a comma-separated list of Strings containing the nodes to remove
   *          from the cache
   */
  public void setNodes(String csvNodes) {
    String[] newNodeFqns = null;
    if (csvNodes != null) {
      newNodeFqns = StringUtils.commaDelimitedListToStringArray(csvNodes);
    }
    setNodes(newNodeFqns);
  }

  /**
   * Sets the names of the nodes to remove from the cache.
   * 
   * @param newNodes
   *          the new names
   */
  public void setNodes(String[] newNodes) {
    nodes = newNodes;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer buffer = Objects.identityToString(this);
    buffer.append("[nodes=" + Objects.nullSafeToString(nodes) + ", ");
    buffer.append("flushBeforeMethodExecution="
        + flushBeforeMethodExecution() + "]");
    return buffer.toString();
  }
}
