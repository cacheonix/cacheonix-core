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
 * Copyright @2007 the original author or authors.
 */
package org.springmodules.cache.provider.jcs;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springmodules.cache.provider.AbstractFlushingModel;
import org.springmodules.util.Objects;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Configuration options needed to flush one or more cache and/or groups from
 * JCS.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public final class JcsFlushingModel extends AbstractFlushingModel {

	private static final long serialVersionUID = -1497138716500203888L;

	/**
	 * Struct containing the names of the caches and/or groups to flush.
	 */
	private CacheStruct[] cacheStructs;

	/**
	 * Constructor.
	 */
	public JcsFlushingModel() {
	}

	/**
	 * Constructor.
	 *
	 * @param cacheStruct a single cache structure that specifies what should be flushed
	 */
	public JcsFlushingModel(CacheStruct cacheStruct) {
		setCacheStruct(cacheStruct);
	}

	/**
	 * Constructor.
	 *
	 * @param cacheName the name of the cache that should be flushed
	 */
	public JcsFlushingModel(String cacheName) {
		this(new CacheStruct(cacheName));
	}

	/**
	 * Sets a single structure specifying which cache (and optionally groups)
	 * should be flushed
	 *
	 * @param cacheStruct the new cache structure
	 */
	public void setCacheStruct(CacheStruct cacheStruct) {
		setCacheStructs(new CacheStruct[]{cacheStruct});
	}

	/**
	 * Sets the cache structures that specify which caches (and optionally groups)
	 * should to be flushed.
	 *
	 * @param newCacheStructs the new cache structs
	 */
	public void setCacheStructs(CacheStruct[] newCacheStructs) {
		cacheStructs = newCacheStructs;
	}

	/**
	 * @return the cache structures that specify which caches (and optionally
	 *         groups) should to be flushed.
	 */
	public CacheStruct[] getCacheStructs() {
		return cacheStructs;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof JcsFlushingModel)) {
			return false;
		}
		JcsFlushingModel flushingModel = (JcsFlushingModel) obj;
		if (!Arrays.equals(cacheStructs, flushingModel.cacheStructs)) {
			return false;
		}
		return true;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int multiplier = 31;
		int hash = 7;
		hash = multiplier * hash + Objects.nullSafeHashCode(cacheStructs);
		return hash;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return Objects.identityToString(this)
				.append("[cacheStructs=")
				.append(Objects.nullSafeToString(cacheStructs))
				.append(", flushBeforeMethodExecution=")
				.append(flushBeforeMethodExecution())
				.append("]")
				.toString();
	}

	/**
	 * Specifies which cache (and optionally which groups) should be flushed.
	 */
	public static class CacheStruct implements Serializable {

		private static final long serialVersionUID = -2168328935167938683L;

		private String cacheName;

		private String[] groups;

		/**
		 * Constructor.
		 */
		public CacheStruct() {
		}

		/**
		 * Constructor.
		 *
		 * @param cacheName the name of the cache to flush.
		 */
		public CacheStruct(String cacheName) {
			setCacheName(cacheName);
		}

		/**
		 * Constructor.
		 *
		 * @param cacheName the name of the cache to use.
		 * @param csvGroups a comma-delimited list containing the groups to flush. Such
		 *                  groups belong to the specified cache.
		 */
		public CacheStruct(String cacheName, String csvGroups) {
			this(cacheName);
			setGroups(csvGroups);
		}

		/**
		 * Constructor.
		 *
		 * @param cacheName the name of the cache to use.
		 * @param groups	the groups to flush. Such groups belong to the specified cache.
		 */
		public CacheStruct(String cacheName, String[] groups) {
			this(cacheName);
			setGroups(groups);
		}

		/**
		 * Sets the name of the cache to flush. If no groups are specified, the
		 * whole cache is flushed.
		 *
		 * @param newCacheName the new name
		 */
		public void setCacheName(String newCacheName) {
			cacheName = newCacheName;
		}

		/**
		 * @return the name of the cache to flush. If no groups are specified, the
		 *         whole cache is flushed.
		 */
		public String getCacheName() {
			return cacheName;
		}

		/**
		 * Sets the groups to flush in a comma-delimited list. If no groups are
		 * specified, the whole cache is flushed.
		 *
		 * @param csvGroups the new groups to flush
		 */
		public void setGroups(String csvGroups) {
			String[] newGroups = null;
			if (StringUtils.hasText(csvGroups)) {
				newGroups = StringUtils.commaDelimitedListToStringArray(csvGroups);
			}
			setGroups(newGroups);
		}

		/**
		 * Sets the groups to flush. If no groups are specified, the whole cache is
		 * flushed.
		 *
		 * @param newGroups the new groups to flush
		 */
		public void setGroups(String[] newGroups) {
			groups = newGroups;
		}

		/**
		 * @return the groups to flush. If no groups are specified, the whole cache
		 *         is flushed.
		 */
		public String[] getGroups() {
			return groups;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof CacheStruct)) {
				return false;
			}

			CacheStruct cacheStruct = (CacheStruct) obj;

			if (!ObjectUtils.nullSafeEquals(cacheName, cacheStruct.cacheName)) {
				return false;
			}
			if (!Arrays.equals(groups, cacheStruct.groups)) {
				return false;
			}
			return true;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			int multiplier = 31;
			int hash = 7;
			hash = multiplier * hash + Objects.nullSafeHashCode(cacheName);
			hash = multiplier * hash + Objects.nullSafeHashCode(groups);
			return hash;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return Objects.identityToString(this)
					.append("[cacheName=")
					.append(StringUtils.quote(cacheName))
					.append(", groups=")
					.append(Objects.nullSafeToString(groups))
					.append("]")
					.toString();
		}
	}

}
