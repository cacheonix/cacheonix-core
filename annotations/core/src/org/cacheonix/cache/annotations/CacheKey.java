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
 * A parameter-level annotation that marks a method parameter to be included as part of the cache key.
 * <p/>
 * <p/>
 * If a method has multiple parameters, the parameters that are annotated with the @CacheKey are included as part of the
 * aggregated key. If the method is annotated with @DataSource but none of the method parameters are are marked with
 *
 * @CacheKey annotation then all the parameters are used for creating aggregated key. if the method is annotated with
 * @CacheInvalidate but none of the method parameters are annotated with @CacheKey then all the method parameters except
 * the last one will be used as part of the aggregate key.
 * <p/>
 * <p/>
 * This annotation is ignored if @CacheConfiguration is not provided for the method's class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface CacheKey {

}
