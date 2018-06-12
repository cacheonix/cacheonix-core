/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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
package org.cacheonix.impl.net.serializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Wireable
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 1, 2010 7:41:35 PM
 */
public interface Wireable {

   /*
     ++++++++++++++++++++++++++++++++++++++ Wireable objects (non-message) ++++++++++++++++++++++++++++++++++++++
    */

   /**
    * Base for unknown type
    */
   int DESTINATION_NONE = 1;

   /**
    * Unknown
    */
   int TYPE_UNDEFINED = DESTINATION_NONE << 8 | 1;

   /**
    */
   int TYPE_PASS_BY_VALUE_LONG_BINARY = DESTINATION_NONE << 8 | 2;

   int TYPE_PASS_BY_VALUE_FLOAT_BINARY = DESTINATION_NONE << 8 | 3;

   int TYPE_PASS_BY_VALUE_DOUBLE_BINARY = DESTINATION_NONE << 8 | 4;

   int TYPE_PASS_BY_VALUE_BOOLEAN_BINARY = DESTINATION_NONE << 8 | 5;

   int TYPE_PASS_BY_VALUE_BYTE_BINARY = DESTINATION_NONE << 8 | 6;

   int TYPE_PASS_BY_REFERENCE_OBJECT_BINARY = DESTINATION_NONE << 8 | 7;

   int TYPE_PASS_BY_REFERENCE_INTEGER_BINARY = DESTINATION_NONE << 8 | 8;

   int TYPE_PASS_BY_COPY_BINARY = DESTINATION_NONE << 8 | 9;

   int TYPE_COMPRESSED_BINARY = DESTINATION_NONE << 8 | 10;

   int TYPE_BUCKET_OWNER = DESTINATION_NONE << 8 | 11;

   int TYPE_BINARY_ENTRY_MODIFIED_EVENT = DESTINATION_NONE << 8 | 12;

   int TYPE_REPLICATED_STATE = DESTINATION_NONE << 8 | 13;

   int TYPE_BUCKET = DESTINATION_NONE << 8 | 14;

   int TYPE_GROUP_MEMBER = DESTINATION_NONE << 8 | 15;

   int TYPE_BUCKET_OWNERSHIP_ASSIGNMENT = DESTINATION_NONE << 8 | 16;

   int TYPE_MD5_HASH_CODE_CALC = DESTINATION_NONE << 8 | 17;

   int TYPE_CLUSTER_VIEW = DESTINATION_NONE << 8 | 18;

   int TYPE_GROUP = DESTINATION_NONE << 8 | 19;

   int TYPE_NODE_ADDRESS = DESTINATION_NONE << 8 | 20;

   int TYPE_BUCKET_TRANSFER = DESTINATION_NONE << 8 | 21;

   int TYPE_LOCK_REGISTRY = DESTINATION_NONE << 8 | 22;

   int TYPE_LOCK_QUEUE = DESTINATION_NONE << 8 | 23;

   int TYPE_LOCK_OWNER = DESTINATION_NONE << 8 | 24;

   int TYPE_BINARY_STORE = DESTINATION_NONE << 8 | 25;

   int TYPE_CACHE_STATISTICS = DESTINATION_NONE << 8 | 26;

   int TYPE_BINARY_STORE_ELEMENT = DESTINATION_NONE << 8 | 27;

   int TYPE_LOCK_QUEUE_KEY = DESTINATION_NONE << 8 | 28;

   int TYPE_ENTRY_MODIFICATION_SUBSCRIPTION = DESTINATION_NONE << 8 | 29;

   int TYPE_TIME = DESTINATION_NONE << 8 | 30;

   int TYPE_CACHEABLE_VALUE = DESTINATION_NONE << 8 | 31;

   int TYPE_CACHEABLE_ENTRY = DESTINATION_NONE << 8 | 32;

   int TYPE_TRANSFER_BUCKET_RESULT = DESTINATION_NONE << 8 | 33;

   int TYPE_JOINING_NODE = DESTINATION_NONE << 8 | 34;

   int TYPE_RECEIVER_ADDRESS = DESTINATION_NONE << 8 | 35;

   int TYPE_NULL_BINARY = DESTINATION_NONE << 8 | 36;

   int TYPE_CACHED_RESPONSE_KEY = DESTINATION_NONE << 8 | 37;

   int TYPE_CACHED_RESPONSE_VALUE = DESTINATION_NONE << 8 | 38;

   int TYPE_DATE_HEADER = DESTINATION_NONE << 8 | 39;

   int TYPE_INTEGER_HEADER = DESTINATION_NONE << 8 | 40;

   int TYPE_STRING_HEADER = DESTINATION_NONE << 8 | 41;


   /*
     ++++++++++++++++++++++++++++++++++++++ Connection-related messages  ++++++++++++++++++++++++++++++++++++++
    */

   /**
    * Base for infrastructure.
    */
   int DESTINATION_CONNECTION = 2;

   /**
    * Open connection.
    */
   int TYPE_OPEN_CONNECTION = DESTINATION_CONNECTION << 8 | 2;

   /**
    * Close connection.
    */
   int TYPE_CONNECTION_CLOSE = DESTINATION_CONNECTION << 8 | 3;

   /*
     ++++++++++++++++++++++++++++++++++++++ Cluster processor messages  ++++++++++++++++++++++++++++++++++++++
    */

   /**
    * Base for cluster service messages.
    */
   int DESTINATION_CLUSTER_PROCESSOR = 3;

   /**
    * Marker.
    */
   int TYPE_CLUSTER_MULTICAST_MARKER = DESTINATION_CLUSTER_PROCESSOR << 8 | 1;

   /**
    * Join request. Sent by a node trying to join the cluster.
    */
   int TYPE_CLUSTER_JOIN_REQUEST = DESTINATION_CLUSTER_PROCESSOR << 8 | 2;

   /**
    *
    */
   int TYPE_CLUSTER_MARKER_LIST = DESTINATION_CLUSTER_PROCESSOR << 8 | 3;

   /**
    * Recovery marker.
    */
   int TYPE_CLUSTER_RECOVERY_MARKER = DESTINATION_CLUSTER_PROCESSOR << 8 | 4;

   /**
    * Cleanup marker.
    */
   int TYPE_CLUSTER_CLEANUP_MARKER = DESTINATION_CLUSTER_PROCESSOR << 8 | 5;

   /**
    * Blocked marker.
    */
   int TYPE_CLUSTER_BLOCKED_MARKER = DESTINATION_CLUSTER_PROCESSOR << 8 | 6;

   /**
    * Cluster announcement.
    */
   int TYPE_CLUSTER_ANNOUNCEMENT = DESTINATION_CLUSTER_PROCESSOR << 8 | 7;

   /**
    * Node shutdown.
    */
   int TYPE_CLUSTER_MARKER_TIMEOUT = DESTINATION_CLUSTER_PROCESSOR << 8 | 9;

   /**
    * Response to a cluster request.
    */
   int TYPE_CLUSTER_RESPONSE = DESTINATION_CLUSTER_PROCESSOR << 8 | 10;

   /**
    *
    */
   int TYPE_CLUSTER_KEY_OWNERS = DESTINATION_CLUSTER_PROCESSOR << 8 | 11;

   /**
    *
    */
   int TYPE_CLUSTER_KEY_OWNER = DESTINATION_CLUSTER_PROCESSOR << 8 | 12;

   /**
    *
    */
   int TYPE_LOCK_ENTRY_COUNT_REQUEST = DESTINATION_CLUSTER_PROCESSOR << 8 | 13;

   /**
    *
    */
   int TYPE_CLUSTER_GET_CLUSTER_VIEW_SIZE = DESTINATION_CLUSTER_PROCESSOR << 8 | 14;

   /**
    *
    */
   int TYPE_AGGREGATING_ANNOUNCEMENT_RESPONSE = DESTINATION_CLUSTER_PROCESSOR << 8 | 15;

   /**
    *
    */
   int TYPE_MULTICAST_FRAME_MESSAGE = DESTINATION_CLUSTER_PROCESSOR << 8 | 16;

   /**
    *
    */
   int TYPE_ADD_USER_CLUSTER_EVENT_SUBSCRIBER = DESTINATION_CLUSTER_PROCESSOR << 8 | 17;

   /**
    *
    */
   int TYPE_REMOVE_USER_CLUSTER_EVENT_SUBSCRIBER = DESTINATION_CLUSTER_PROCESSOR << 8 | 18;

   /*
     ++++++++++++++++++++++++++++++++++++++ Cache processor messages  ++++++++++++++++++++++++++++++++++++++
    */

   /**
    * Base for memory service messages.
    */
   int DESTINATION_CACHE_PROCESSOR = 4;

   /**
    * This request is sent to the remote key owner.
    */
   int TYPE_CACHE_PUT_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 1;

   int TYPE_CACHE_RESPONSE = DESTINATION_CACHE_PROCESSOR << 8 | 2;

   /**
    * This request is sent to the remote key owner.
    */
   int TYPE_CACHE_GET_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 3;

   /**
    * This request is sent to the remote key owner.
    */
   int TYPE_CACHE_CONTAINS_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 4;

   /**
    * This request is sent to the remote key owner.
    */
   int TYPE_CACHE_REMOVE_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 5;

   /**
    * Request to create a cache member.
    */
   int TYPE_CACHE_MEMBER_JOINED = DESTINATION_CACHE_PROCESSOR << 8 | 7;

   /**
    * Request to create a cache member.
    */
   int TYPE_CACHE_MEMBER_LEFT = DESTINATION_CACHE_PROCESSOR << 8 | 8;

   int TYPE_CACHE_BEGIN_BUCKET_TRANSFER_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 9;

   int TYPE_CACHE_TRANSFER_BUCKET_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 10;

   int TYPE_CACHE_FINISH_BUCKET_TRANSFER_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 12;

   int TYPE_CACHE_CANCEL_BUCKET_TRANSFER_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 13;

   int TYPE_CACHE_RESTORE_BUCKET_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 14;

   int TYPE_CACHE_CONTAINS_VALUE_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 16;

   int TYPE_CACHE_SIZE_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 17;

   int TYPE_CACHE_CLEAR_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 18;


   int TYPE_CACHE_PUT_ALL_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 22;

   int TYPE_CACHE_VALUES_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 24;

   int TYPE_CACHE_AGGREGATING_CACHE_RESPONSE = DESTINATION_CACHE_PROCESSOR << 8 | 25;

   int TYPE_CACHE_KEY_SET_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 26;

   int TYPE_CACHE_ENTRY_SET_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 27;

   int TYPE_CACHE_EXECUTE_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 28;

   int TYPE_CACHE_REMOVE_ALL_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 29;

   int TYPE_CACHE_EXECUTE_ALL_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 30;

   int TYPE_CACHE_GET_ALL_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 31;

   int TYPE_CACHE_RETAIN_ALL_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 32;

   int TYPE_CACHE_GET_MAX_SIZE_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 33;

   int TYPE_CACHE_SHUTDOWN_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 34;

   int TYPE_CACHE_STATISTICS_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 35;

   int TYPE_CACHE_ADD_ENTRY_MODIFIED_SUBSCRIBER_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 36;

   int TYPE_CACHE_REMOVE_ENTRY_MODIFIED_SUBSCRIBER_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 37;

   int TYPE_CACHE_ADD_REMOTE_SUBSCRIBER_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 38;

   int TYPE_CACHE_REMOVE_REMOTE_SUBSCRIBER_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 39;

   int TYPE_CACHE_ENTRY_MODIFIED_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 40;

   int TYPE_CACHE_ORPHAN_BUCKET = DESTINATION_CACHE_PROCESSOR << 8 | 41;

   int TYPE_CACHE_INVALIDATE_FRONT_CACHE_MESSAGE = DESTINATION_CACHE_PROCESSOR << 8 | 42;

   int TYPE_CACHE_UPDATE_KEY_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 43;

   int TYPE_CACHE_ATOMIC_REMOVE_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 44;

   int TYPE_CACHE_ATOMIC_REPLACE_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 45;

   int TYPE_CACHE_REPLACE_IF_MAPPED_REQUEST = DESTINATION_CACHE_PROCESSOR << 8 | 46;


   /*
     ++++++++++++++++++++++++++++++++++++++ Replicated state messages  ++++++++++++++++++++++++++++++++++++++
    */

   /**
    * Group membership protocol
    */
   int DESTINATION_REPLICATED_STATE = 9;

   /**
    * Request to add a member.
    */
   int TYPE_GROUP_JOIN_GROUP = DESTINATION_REPLICATED_STATE << 8 | 1;

   /**
    * Request to remove a member.
    */
   int TYPE_GROUP_LEAVE_ANNOUNCEMENT = DESTINATION_REPLICATED_STATE << 8 | 2;

   /**
    * An announcement about change of ownership.
    */
   int TYPE_GROUP_BUCKET_TRANSFER_COMPLETED = DESTINATION_REPLICATED_STATE << 8 | 4;

   /**
    * An announcement about change of ownership.
    */
   int TYPE_GROUP_ANNOUNCE_REPARTITIONING = DESTINATION_REPLICATED_STATE << 8 | 5;

   /**
    * An announcement about rejection of bucket transfer
    */
   int TYPE_GROUP_BUCKET_TRANSFER_REJECTED = DESTINATION_REPLICATED_STATE << 8 | 8;

   int TYPE_REGISTER_SUBSCRIPTION_ANNOUNCEMENT = DESTINATION_REPLICATED_STATE << 8 | 9;

   int TYPE_UNREGISTER_SUBSCRIPTION_ANNOUNCEMENT = DESTINATION_REPLICATED_STATE << 8 | 10;

   /**
    */
   int TYPE_NODE_LEFT_MESSAGE = DESTINATION_REPLICATED_STATE << 8 | 11;

   /**
    */
   int TYPE_NODE_JOINED_MESSAGE = DESTINATION_REPLICATED_STATE << 8 | 12;

   /**
    *
    */
   int TYPE_ACQUIRE_LOCK_REQUEST = DESTINATION_REPLICATED_STATE << 8 | 13;

   /**
    *
    */
   int TYPE_RELEASE_LOCK_REQUEST = DESTINATION_REPLICATED_STATE << 8 | 14;

   /**
    *
    */
   int TYPE_WAIT_FOR_LOCK_EXPIRED_ANNOUNCEMENT = DESTINATION_REPLICATED_STATE << 8 | 15;

   int TYPE_CACHE_INVALIDATE_FRONT_CACHE_ANNOUNCEMENT = DESTINATION_REPLICATED_STATE << 8 | 16;

   /**
    * ++++++++++++++++++++++++++++++++++++++ Multicast clients messages  ++++++++++++++++++++++++++++++++++++++
    */

   int DESTINATION_MULTICAST_CLIENT = 10;


   /**
    */
   int TYPE_TEST_MESSAGE = DESTINATION_MULTICAST_CLIENT << 8 | 1;


   /**
    * Writes this wireable object to the wire.
    *
    * @param out data output stream
    * @throws IOException if an I/O error occurred while writing to the wire.
    */
   void writeWire(DataOutputStream out) throws IOException;


   /**
    * Reads this wireable object from the stream.
    *
    * @param in a binary data input
    * @throws IOException            if an I/O error occurred while writing to the wire.
    * @throws ClassNotFoundException if an object read from the wire does not have a corresponding class.
    */
   void readWire(DataInputStream in) throws IOException, ClassNotFoundException;

   /**
    * Returns index of this object in the object registry.
    *
    * @return index of this object in the object registry.
    * @see WireableFactory
    */
   int getWireableType();
}
