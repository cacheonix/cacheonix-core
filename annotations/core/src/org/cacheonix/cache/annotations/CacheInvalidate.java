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
 * A method-level annotation that removes an entry from the cache.
 * <p/>
 * <p/>
 * If a method has multiple parameters, the parameters are treated as an aggregate key. A wrapper key object is created
 * for parameters object. The wrapper object implements equals() and hashCode() by delegating calculation to the list of
 * the parameter objects. If the method does not define parameters, a synthetic key is used. The synthetic key value is
 * the method's name. When an annotated method is called, the annotation calls cache.remove().
 * <p/>
 * <p/>
 * This annotation is ignored if @CacheConfiguration is not provided for the method's class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CacheInvalidate {

}
