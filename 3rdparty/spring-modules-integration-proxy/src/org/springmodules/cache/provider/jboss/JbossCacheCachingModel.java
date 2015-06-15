/* 
 * Created on Sep 1, 2005
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

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.util.Objects;

/**
 * <p>
 * Configuration options needed to store, retrieve and remove objects from
 * JBossCache.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class JbossCacheCachingModel implements CachingModel {

  private static final long serialVersionUID = -9019322549512783005L;

  private String node;

  /**
   * Constructor.
   */
  public JbossCacheCachingModel() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param newNode
   *          the fully qualified name of the node to use
   */
  public JbossCacheCachingModel(String newNode) {
    this();
    setNode(newNode);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof JbossCacheCachingModel)) {
      return false;
    }

    JbossCacheCachingModel cachingModel = (JbossCacheCachingModel) obj;

    if (!ObjectUtils.nullSafeEquals(node, cachingModel.node)) {
      return false;
    }

    return true;
  }

  /**
   * @return the fully qualified name of the node to use
   */
  public final String getNode() {
    return node;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int multiplier = 31;
    int hash = 17;

    hash = multiplier * hash + Objects.nullSafeHashCode(node);

    return hash;
  }

  /**
   * Sets the fully qualified name of the node to use
   * 
   * @param newNodeFqn
   *          the new node FQN
   */
  public final void setNode(String newNodeFqn) {
    node = newNodeFqn;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer buffer = Objects.identityToString(this);
    buffer.append("[nodeFqn=" + StringUtils.quote(node) + "]");

    return buffer.toString();
  }
}
