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
package org.cacheonix.impl.cluster.node.state.bucket;

/**
 * BucketEventSubscriber
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Oct 23, 2009 8:21:54 PM
 */
public interface BucketEventListener {

   /**
    * @noinspection UnusedParameters
    */
   void execute(BeginBucketTransferCommand command);

   void execute(FinishBucketTransferCommand command);

   void execute(CancelBucketTransferCommand command);

   /**
    * Restores a primary bucket from a local replica.
    *
    * @param command
    */
   void execute(RestoreBucketCommand command);

   /**
    * Executes a command to orphan a bucket.
    *
    * @param command the command to orphan a bucket.
    */
   void execute(OrphanBucketCommand command);

   /**
    * Executes a command to assign a bucket.
    *
    * @param command the command to assign a bucket.
    */
   void execute(AssignBucketCommand command);
}
