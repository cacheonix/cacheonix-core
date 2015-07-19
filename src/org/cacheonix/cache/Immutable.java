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
package org.cacheonix.cache;

/**
 * A signature interface that tells Cacheonix that a class implementing it is immutable. Cacheonix always stores objects
 * declared as immutable by reference. Storing by reference significantly speeds up cache read and write operations by
 * eliminating the time to create a copy of an object when returning keys and values.
 * <p/>
 * <code>cacheonix.cache.Immutable</code> interface is a signature interface. It does not define any methods. It is the
 * responsibility of the implementing class to ensure that it is immutable. An immutable object is an object that state
 * cannot be changed after the object was created. An example of an immutable object is an object that fields are
 * declared as <code>final</code> and all public methods are getters.
 * <p/>
 * Subtypes of an immutable type are also treated as immutable.
 * <p/>
 * Cacheonix always treats as immutable objects of the following types: <code>java.lang.Byte</code>,
 * <code>java.lang.Short</code>, <code>java.lang.Integer</code>, <code>java.lang.Long</code>,
 * <code>java.lang.Boolean</code> and <code>java.lang.String</code>.
 * <p/>
 *
 * @noinspection InterfaceNeverImplemented, MarkerInterface
 */
public interface Immutable {

}
