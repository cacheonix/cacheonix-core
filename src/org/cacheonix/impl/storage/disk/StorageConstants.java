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
package org.cacheonix.impl.storage.disk;

public interface StorageConstants {

   // ////////////////////////////////////////////////////////////////////////////////////////////
   // Parameters which need to be configured
   //
   long DEFAULT_STORAGE_CLUSTER_SIZE = 141L; // TODO @SF - sfishel@cacheonix.com -
   // Limit
   // < 4KB Create interface with Constants
   // Make it megabytes (default 1MB)
   long DEFAULT_NUMBER_OF_CLUSTERS = (long) (1024 << 10) / DEFAULT_STORAGE_CLUSTER_SIZE;
   // Indicator flags
   long FINAL_STORAGECELL_IN_SEQUENCE = -1L;
   byte STORAGE_CELL_OCCUPIED = (byte) 0x01;
   byte STORAGE_CELL_FREE = (byte) 0x00;
   // Header size for block
   int STORAGE_CELL_HEADER_SIZE = 9; // sizeof(byte) + sizeof(long)/ 1 + 9
   int STORAGE_CELL_MARKER_SIZE = 8; //sizeof long
   int STORAGE_DATA_LENGTH_SIZE = 4; //sizeof int
}
