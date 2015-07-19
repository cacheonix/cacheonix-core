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
package org.cacheonix.impl.net.processor;

/**
 * A signature interface that indicates that the implementing request is a strictly-local request and will not ever
 * cross a wire. Such a request usually contains contains transient object that shall be carries by the request to local
 * processors.
 * <p/>
 * <b>Router will pass such request to a local processor by reference instead of by copy</b>.
 * <p/>
 * The implementer of SafeLocalRequest must guarantee that it is free of side effects of the local request waiters
 * having reference access to their requests.
 */
public interface RouteByReferenceRequest {

}
