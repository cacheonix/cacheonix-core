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
 * Copyright @2007 the original author or authors.
 */

package org.springmodules.cache.interceptor.flush;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springmodules.cache.CacheAttribute;
import org.springmodules.util.Objects;

/**
 * Metadata attribute used to identify the methods that flush the cache when
 * executed.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public class FlushCache implements CacheAttribute {

	private static final long serialVersionUID = 3689909557149513778L;

	/**
	 * Id of the model to be used to flush the cache.
	 */
	private String modelId;

	/**
	 * Construct a <code>FlushCache</code>.
	 */
	public FlushCache() {
		super();
	}

	/**
	 * Construct a new <code>FlushCache</code> with the given flushing model id.
	 *
	 * @param newModelId the id of the flushing model associated to this flushing attribute
	 */
	public FlushCache(String newModelId) {
		this();
		setModelId(newModelId);
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FlushCache)) {
			return false;
		}
		FlushCache flushCache = (FlushCache) obj;

		if (!ObjectUtils.nullSafeEquals(modelId, flushCache.modelId)) {
			return false;
		}

		return true;
	}

	/**
	 * @return the id of the flushing model associated to this flushing attribute
	 */
	public final String getModelId() {
		return modelId;
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		int multiplier = 31;
		int hash = 7;
		hash = multiplier * hash + (Objects.nullSafeHashCode(modelId));
		return hash;
	}

	/**
	 * Sets the id of the flushing model to associate to this flushing attribute.
	 *
	 * @param newModelId the new flushing model id
	 */
	public final void setModelId(String newModelId) {
		modelId = newModelId;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return Objects.identityToString(this)
				.append("[modelId=")
				.append(StringUtils.quote(modelId))
				.append("]")
				.toString();
	}

}