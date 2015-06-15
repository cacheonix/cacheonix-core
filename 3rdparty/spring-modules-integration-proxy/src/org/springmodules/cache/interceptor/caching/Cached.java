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

package org.springmodules.cache.interceptor.caching;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springmodules.cache.CacheAttribute;
import org.springmodules.util.Objects;

/**
 * Source-level metadata attribute that identifies the methods which return
 * value should be stored in the cache.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public class Cached implements CacheAttribute {

	private static final long serialVersionUID = 3256728394032297785L;

	private String modelId;

	/**
	 * Construct a new <code>Cached</code>.
	 */
	public Cached() {
		super();
	}

	/**
	 * Construct a new <code>Cached</code> with the given caching model id.
	 *
	 * @param newModelId the id of the caching model associated to this caching attribute
	 */
	public Cached(String newModelId) {
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
		if (!(obj instanceof Cached)) {
			return false;
		}

		Cached cached = (Cached) obj;

		if (!ObjectUtils.nullSafeEquals(modelId, cached.modelId)) {
			return false;
		}

		return true;
	}

	/**
	 * @return the id of the caching model associated to this caching attribute
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
	 * Sets the id of the caching model to associate to this caching attribute.
	 *
	 * @param newModelId the new caching model id
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