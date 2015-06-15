/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.cache.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides an explicit configuration for method-level annotations
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface CacheConfiguration {

   /**
    * Defines a class as containing cacheable methods. Contains a path to cacheonix-config.xml as described by
    * Cacheonix.getInstance(configurationPath). If not set, Cacheonix uses default configuration path resolution policy
    * (file to classpath to default configuration)
    */
   String configurationPath() default "cacheonix-config.xml";

   /**
    * Defines a name of a cache to use as defined in cacheonix-config.xml. If not set, a cache named same as the fully
    * qualified class name is created using a default template (that's what Cacheonix.createCache() does). If there is no
    * default template and the there is no such name.
    */
   String cacheName() default "CacheonixAnnotatedCache";

}
