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

import java.io.PrintWriter;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;

/**
 * A tester {@link ServletPrintWriterWrapper}.
 */
public final class ServletPrintWriterWrapperTest extends TestCase {


   private static final String TEST_STRING = "test string";

   private static final String LINE_SEPARATOR = System.getProperty("line.separator");


   private ServletPrintWriterWrapper servletPrintWriterWrapper;


   public void testPrintln() {

      servletPrintWriterWrapper.println(TEST_STRING);
      servletPrintWriterWrapper.flush();
      assertEquals(TEST_STRING + LINE_SEPARATOR, new String(servletPrintWriterWrapper.getByteOutput()));
   }


   public void testPrint() {

      servletPrintWriterWrapper.print(TEST_STRING);
      servletPrintWriterWrapper.flush();
      assertEquals(TEST_STRING, new String(servletPrintWriterWrapper.getByteOutput()));
   }


   public void testPrintf() {

      servletPrintWriterWrapper.printf("This is a %s", TEST_STRING);
      servletPrintWriterWrapper.flush();
      assertEquals("This is a " + TEST_STRING, new String(servletPrintWriterWrapper.getByteOutput()));
   }


   public void setUp() throws Exception {

      super.setUp();

      final PrintWriter printWriter = mock(PrintWriter.class);
      servletPrintWriterWrapper = new ServletPrintWriterWrapper(printWriter);
   }


   public void tearDown() throws Exception {

      servletPrintWriterWrapper = null;

      super.tearDown();
   }
}