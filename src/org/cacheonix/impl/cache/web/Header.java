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
package org.cacheonix.impl.cache.web;

import javax.servlet.http.HttpServletResponse;

import org.cacheonix.impl.net.serializer.Wireable;

/**
 * An HttpServletResponse header.
 */
public interface Header extends Wireable {

   /**
    * Adds itself to the <code>httpServletResponse</code>. This is an implementation of the Strategy pattern where each
    * concrete implementation knows how to work with the target object.
    *
    * @param httpServletResponse an {@link HttpServletResponse} to add this header to.
    */
   void addToResponse(HttpServletResponse httpServletResponse);

   /**
    * Returns true if the value of the header contains a given string. The search ignores the case.
    *
    * @param s a string to check for.
    * @return true of the value of the header contains a given string.
    */
   boolean containsString(String s);

   /**
    * Returns true if the value of the header starts with a given string. The search ignores the case.
    *
    * @param s a string to check for.
    * @return true of the value of the header starts with a given string.
    */
   boolean startsWith(String s);
}
