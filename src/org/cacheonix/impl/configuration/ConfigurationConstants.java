/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.org/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.configuration;

import java.util.concurrent.TimeUnit;

import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Configuration constants. Some of them are are candidates for making into Cacheonix configuration.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 1, 2008 9:23:28 PM
 */
public final class ConfigurationConstants {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ConfigurationConstants.class); // NOPMD

   /**
    * Default join timeout.
    */
   public static final long DEFAULT_JOIN_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60L);

   /**
    * Default logging level.
    */
   public static final LoggingLevel DEFAULT_LOGGING_LEVEL = LoggingLevel.INFO;

   /**
    * Lock timeout if not set.
    */
   public static final long DEFAULT_LOCK_TIMEOUT_MILLIS = StringUtils.readTime("30s");


   /**
    * Lock unlock if not set.
    */
   public static final long DEFAULT_UNLOCK_TIMEOUT_MILLIS = StringUtils.readTime("30s");


   /**
    * Fall-back configuration resource.
    */
   public static final String FALLBACK_CACHEONIX_XML_RESOURCE = "/META-INF/cacheonix-config.xml";

   /**
    * Default lease time.
    */
   public static final long DEFAULT_LEASE_TIME_MILLIS = StringUtils.readTime("5ms");

   /**
    * Bucket count, should be the same for all nodes in the cluster.
    * <p/>
    * <b>DO NOT CHANGE THIS CONSTANT!</b>
    */
   public static final int BUCKET_COUNT = 2053;

   /**
    * Extension for the storage files used in Cacheonix.
    */
   public static final String STORAGE_FILE_EXTENSION = ".dat";

   /**
    * Storage file prefix.
    */
   public static final String STORAGE_FILE_PREFIX = "storage-";

   /**
    * Prefix for internal local cache.
    */
   public static final String LOCAL_CACHE_NAME_SUFFIX = "-local"; // NOPMD

   public static final String JAVA_IO_TEMP = System.getProperty(SystemProperty.JAVA_IO_TEMP, ".");

   /**
    * Name of a default cache configuration.
    *
    * @noinspection ConstantDeclaredInInterface
    */
   public static final String CACHE_TEMPLATE_NAME_DEFAULT = "default";

   /**
    * Default selector timeout, milliseconds. Used if it cannot be loaded from the configuration file.
    */
   public static final long DEFAULT_SELECTOR_TIMEOUT_MILLIS = 1000L;

   /**
    * Default SO_TIMEOUT, milliseconds.
    */
   public static final long DEFAULT_SO_TIMEOUT_MILLIS = 5000L;

   public static final int MAX_PREFETCH_CANCELS_BEFORE_PURGE = 1000;


   /**
    * Constant collection constructor.
    */
   private ConfigurationConstants() {

   }
}
