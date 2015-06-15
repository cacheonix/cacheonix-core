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
package org.cacheonix.impl.util;

import java.io.File;
import java.io.IOException;

/**
 * This interface defines a callback method used when traversing a directories and files under the drirectories. For
 * aeach File object callback is called once.
 *
 * @see IoUtils#traverseDir
 * @see File
 */
public interface DirectoryTraverserCallback {

   /**
    * Callback method called by IoUtils#traverseDir.
    *
    * @param file
    * @return <code>true</code> if traversal should continue;
    * @throws IOException
    * @see IoUtils#traverseDir
    */
   boolean callback(File file) throws IOException;
}
