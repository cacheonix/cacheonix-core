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
package org.springmodules.cache.provider.ehcache;

import org.springframework.util.StringUtils;
import org.springmodules.cache.provider.AbstractFlushingModel;
import org.springmodules.util.Objects;

import java.util.Arrays;

/**
 * Configuration options needed to flush one or more caches from EHCache.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public final class EhCacheFlushingModel extends AbstractFlushingModel {

	private static final long serialVersionUID = 7299844898815952890L;

	/**
	 * Names of the caches to flush.
	 */
	private String[] cacheNames;

	/**
	 * Constructor.
	 */
	public EhCacheFlushingModel() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param csvCacheNames a comma-separated list containing the names of the EHCache caches
	 *                      to flush separated by commas
	 */
	public EhCacheFlushingModel(String csvCacheNames) {
		this();
		setCacheNames(csvCacheNames);
	}

	/**
	 * Constructor.
	 *
	 * @param newCacheNames the names of the EHCache caches to flush
	 */
	public EhCacheFlushingModel(String[] newCacheNames) {
		this();
		setCacheNames(newCacheNames);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EhCacheFlushingModel)) {
			return false;
		}
		EhCacheFlushingModel flushingModel = (EhCacheFlushingModel) obj;
		if (!Arrays.equals(cacheNames, flushingModel.cacheNames)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the names of the EHCache caches to flush
	 */
	public String[] getCacheNames() {
		return cacheNames;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int multiplier = 31;
		int hash = 7;
		hash = multiplier * hash + Objects.nullSafeHashCode(cacheNames);
		return hash;
	}

	/**
	 * Sets the names of the caches to flush.
	 *
	 * @param csvCacheNames a comma-separated list of Strings containing the names of the
	 *                      caches to flush.
	 */
	public void setCacheNames(String csvCacheNames) {
		String[] newCacheNames = null;
		if (csvCacheNames != null) {
			newCacheNames = StringUtils
					.commaDelimitedListToStringArray(csvCacheNames);
		}
		setCacheNames(newCacheNames);
	}

	/**
	 * Sets the names of the EHCache caches to flush. It also removes any
	 * duplicated cache names.
	 *
	 * @param newCacheNames the names of the caches
	 */
	public void setCacheNames(String[] newCacheNames) {
		cacheNames = newCacheNames;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return Objects.identityToString(this)
				.append("[cacheNames=")
				.append(Objects.nullSafeToString(cacheNames))
				.append(", flushBeforeMethodExecution=")
				.append(flushBeforeMethodExecution())
				.append("]")
				.toString();
	}
}
