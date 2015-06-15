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
package org.cacheonix.cache.datasource;

import junit.framework.TestCase;

/**
 * A tester <tt>DataSourceException</tt>.
 */
public final class DataSourceExceptionTest extends TestCase {


   private static final String MESSAGE = "Test message";

   private static final Throwable CAUSE = new Throwable();


   public void testConstructor() {

      assertNotNull(new DataSourceException().getMessage());
      assertTrue(new DataSourceException(MESSAGE).getMessage().contains(MESSAGE));
      assertTrue(new DataSourceException(MESSAGE, CAUSE).getMessage().contains(MESSAGE));
      assertEquals(CAUSE, new DataSourceException(MESSAGE, CAUSE).getCause());
      assertEquals(CAUSE, new DataSourceException(CAUSE).getCause());
   }
}
