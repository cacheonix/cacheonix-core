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
package org.cacheonix.impl.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cacheonix.impl.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Class ClusterConfiguration.
 */
public final class ClusterConfiguration extends DocumentReader {

   public static final long DEFAULT_CLUSTER_ANNOUNCEMENT_TIMEOUT_MILLS = StringUtils.readTime("100ms");

   public static final long DEFAULT_CLUSTER_SURVEY_TIMEOUT_MILLS = StringUtils.readTime("500ms");

   private static final long DEFAULT_HOME_ALONE_TIMEOUT_MILLS = StringUtils.readTime("1s");

   private static final long DEFAULT_WORST_CASE_LATENCY_MILLS = StringUtils.readTime("10s");

   private static final int DEFAULT_MINIMUM_NODE_COUNT = 1;

   /**
    * The cluster name limits cluster membership and communication only to the cluster having this name. The default is
    * "Cacheonix".
    */
   private String name = "Cacheonix";

   /**
    * A timeout in milliseconds after that a node considers that there is no communication path to other nodes and
    * proceeds to form a single-node cluster.
    * <p/>
    * The default value is 60 seconds.
    * <p/>
    * Use system property <code>cacheonix.home.alone.timeout</code> to override the attribute 'homeAloneTimeoutMillis'
    * from the command line.
    */
   private long homeAloneTimeoutMillis = DEFAULT_HOME_ALONE_TIMEOUT_MILLS;

   /**
    * The frequency the cluster should announce itself.
    */
   private long clusterAnnouncementTimeoutMillis = DEFAULT_CLUSTER_ANNOUNCEMENT_TIMEOUT_MILLS;

   /**
    * Time that a new Cacheonix node waits for to identify all available Cacheonix nodes before stating a join
    * procedure. The cluster survey timeout should be set to a value that is at least two times higher than the cluster
    * announcement frequency. Use system property 'cacheonix.cluster.survey.time' to override the attribute
    * clusterSurveyTimeout from the command line.
    */
   private long clusterSurveyTimeoutMillis = DEFAULT_CLUSTER_SURVEY_TIMEOUT_MILLS;

   /**
    * Worst case communication latency in milliseconds. Cacheonix considers a host failed if it has not responded to a
    * connection request or to an I/O request within this time. Worst case network latency should take in account time
    * for that the host may be completely unresponsive due garbage collection (GC). Default is 30 seconds.
    */
   private long worstCaseLatencyMillis = DEFAULT_WORST_CASE_LATENCY_MILLS;

   /**
    * Field minimumNodeCount.
    */
   private long minimumNodeCount = DEFAULT_MINIMUM_NODE_COUNT;

   /**
    * Field propertyList.
    */
   private final List<PropertyConfiguration> propertyList = new ArrayList<PropertyConfiguration>(0);


   /**
    * @param vProperty
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void addProperty(final PropertyConfiguration vProperty) throws IndexOutOfBoundsException {

      this.propertyList.add(vProperty);
   }


   /**
    * @param index
    * @param vProperty
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void addProperty(final int index, final PropertyConfiguration vProperty) throws IndexOutOfBoundsException {

      this.propertyList.add(index, vProperty);
   }


   /**
    * Method enumerateProperty.
    *
    * @return an Enumeration over all org.cacheonix.impl.configuration.PropertyConfiguration elements
    */
   public List<? extends PropertyConfiguration> enumerateProperty() {

      return new ArrayList<PropertyConfiguration>(this.propertyList);
   }


   /**
    * Returns the timeout in milliseconds after that a node considers that there is no communication path to other nodes
    * and proceeds to form a single-node cluster.
    * <p/>
    * The default value is 60 seconds.
    * <p/>
    * Use system property <code>cacheonix.home.alone.timeout</code> to override the attribute 'homeAloneTimeout' from
    * the command line.
    *
    * @return the timeout in milliseconds after that a node considers that there is no communication path to other nodes
    *         and proceeds to form a single-node cluster.
    */
   public long getHomeAloneTimeoutMillis() {

      return this.homeAloneTimeoutMillis;
   }


   /**
    * Returns the frequency the cluster should announce itself.
    *
    * @return the frequency the cluster should announce itself.
    */
   public long getClusterAnnouncementTimeoutMillis() {

      return clusterAnnouncementTimeoutMillis;
   }


   /**
    * Sets the frequency the cluster should announce itself.
    *
    * @param clusterAnnouncementTimeoutMillis
    *         the frequency the cluster should announce itself.
    */
   public void setClusterAnnouncementTimeoutMillis(final long clusterAnnouncementTimeoutMillis) {

      this.clusterAnnouncementTimeoutMillis = clusterAnnouncementTimeoutMillis;
   }


   /**
    * Returns the time that a new Cacheonix node waits for to identify all available Cacheonix nodes before stating a
    * join procedure.
    *
    * @return the time that a new Cacheonix node waits for to identify all available Cacheonix nodes before stating a
    *         join procedure. The cluster survey timeout should be set to a value that is at least two times higher than
    *         the cluster announcement frequency. Use system property 'cacheonix.cluster.survey.timeout' to override the
    *         attribute clusterSurveyTimeout from the command line. The default value for clusterSurveyTimeout is 2
    *         seconds.
    */
   public long getClusterSurveyTimeoutMillis() {

      return clusterSurveyTimeoutMillis;
   }


   /**
    * Sets the time that a new Cacheonix node waits for to identify all available Cacheonix nodes before stating a join
    * procedure. The cluster survey timeout should be set to a value that is at least two times higher than the cluster
    * announcement frequency. Use system property 'cacheonix.cluster.survey.time' to override the attribute
    * clusterSurveyTimeout from the command line.
    *
    * @param clusterSurveyTimeoutMillis the time that a new Cacheonix node waits for to identify all available Cacheonix
    *                                   nodes before stating a join procedure. The cluster survey timeout should be set
    *                                   to a value that is at least two times higher than the cluster announcement
    *                                   frequency. Use system property 'cacheonix.cluster.survey.timeout' to override
    *                                   the attribute clusterSurveyTimeout from the command line.
    */
   public void setClusterSurveyTimeoutMillis(final long clusterSurveyTimeoutMillis) {

      this.clusterSurveyTimeoutMillis = clusterSurveyTimeoutMillis;
   }


   /**
    * Returns the value of field 'minimumNodeCount'.
    *
    * @return the value of field 'MinimumNodeCount'.
    */
   public long getMinimumNodeCount() {

      return this.minimumNodeCount;
   }


   /**
    * Returns the cluster name. The cluster name limits cluster membership and communication only to the cluster having
    * this name. The default is "Cacheonix".
    *
    * @return the the cluster name.
    */
   public String getName() {

      return this.name;
   }


   /**
    * Method getProperty.
    *
    * @param index
    * @return the value of the org.cacheonix.impl.configuration.PropertyConfiguration at the given index
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public PropertyConfiguration getProperty(final int index) throws IndexOutOfBoundsException {

      // check bounds for index
      if (index < 0 || index >= this.propertyList.size()) {
         throw new IndexOutOfBoundsException(
                 "getProperty: Index value '" + index + "' not in range [0.." + (this.propertyList.size() - DEFAULT_MINIMUM_NODE_COUNT) + ']');
      }

      return propertyList.get(index);
   }


   /**
    * Method getProperty.Returns the contents of the collection in an Array.  <p>Note:  Just in case the collection
    * contents are changing in another thread, we pass a 0-length Array of the correct type into the API call.  This way
    * we <i>know</i> that the Array returned is of exactly the correct length.
    *
    * @return this collection as an Array
    */
   public PropertyConfiguration[] getProperty() {

      final PropertyConfiguration[] array = new PropertyConfiguration[0];
      return this.propertyList.toArray(array);
   }


   /**
    * Method getPropertyCount.
    *
    * @return the size of this collection
    */
   public int getPropertyCount() {

      return this.propertyList.size();
   }


   /**
    * Returns the worst case communication latency. Cacheonix considers a host failed if it has not responded to a
    * connection request or to an I/O request within this time. Worst case network latency should take in account time
    * for that the host may be completely unresponsive due garbage collection (GC). Default is 30 seconds.
    *
    * @return the worst case communication latency.
    */
   public long getWorstCaseLatencyMillis() {

      return this.worstCaseLatencyMillis;
   }


   /**
    */
   public void removeAllProperty() {

      this.propertyList.clear();
   }


   /**
    * Method removeProperty.
    *
    * @param vProperty
    * @return true if the object was removed from the collection.
    */
   public boolean removeProperty(final PropertyConfiguration vProperty) {

      return propertyList.remove(vProperty);
   }


   /**
    * Method removePropertyAt.
    *
    * @param index
    * @return the element removed from the collection
    */
   public PropertyConfiguration removePropertyAt(final int index) {

      return this.propertyList.remove(index);
   }


   /**
    * Sets the timeout in milliseconds after that a node considers that there is no communication path to other nodes
    * and proceeds to form a single-node cluster.
    * <p/>
    * The default value is 60 seconds.
    * <p/>
    * Use system property <code>cacheonix.home.alone.timeout</code> to override the attribute 'homeAloneTimeout' from
    * the command line.
    *
    * @param homeAloneTimeoutMillis the timeout in milliseconds after that a node considers that there is no
    *                               communication path to other nodes and proceeds to form a single-node cluster.
    */
   public void setHomeAloneTimeoutMillis(final long homeAloneTimeoutMillis) {

      this.homeAloneTimeoutMillis = homeAloneTimeoutMillis;
   }


   /**
    * Sets the value of field 'minimumNodeCount'.
    *
    * @param minimumNodeCount the value of field 'minimumNodeCount'
    */
   public void setMinimumNodeCount(final long minimumNodeCount) {

      this.minimumNodeCount = minimumNodeCount;
   }


   /**
    * Sets the the cluster name. The cluster name limits cluster membership and communication only to the cluster having
    * this name.
    *
    * @param name the the cluster name.
    */
   public void setName(final String name) {

      this.name = name;
   }


   /**
    * @param index
    * @param vProperty
    * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
    */
   public void setProperty(final int index, final PropertyConfiguration vProperty) throws IndexOutOfBoundsException {

      // check bounds for index
      if (index < 0 || index >= this.propertyList.size()) {
         throw new IndexOutOfBoundsException(
                 "setProperty: Index value '" + index + "' not in range [0.." + (this.propertyList.size() - DEFAULT_MINIMUM_NODE_COUNT) + ']');
      }

      this.propertyList.set(index, vProperty);
   }


   /**
    * @param vPropertyArray
    */
   public void setProperty(final PropertyConfiguration[] vPropertyArray) {

      //-- copy array
      propertyList.clear();

      this.propertyList.addAll(Arrays.asList(vPropertyArray));
   }


   /**
    * Sets the worst case communication latency in milliseconds.
    *
    * @param worstCaseLatencyMillis the worst case communication latency in milliseconds. Cacheonix considers a host
    *                               failed if it has not responded to a connection request or to an I/O request within
    *                               this time. Worst case network latency should take in account time for that the host
    *                               may be completely unresponsive due garbage collection (GC). Default is 30 seconds.
    */
   public void setWorstCaseLatencyMillis(final long worstCaseLatencyMillis) {

      this.worstCaseLatencyMillis = worstCaseLatencyMillis;
   }


   protected void readNode(final String nodeName, final Node childNode) {

      if ("property".equals(nodeName)) {

         final PropertyConfiguration property = new PropertyConfiguration();
         property.read(childNode);
         propertyList.add(property);
      }
   }


   protected void readAttribute(final String attributeName, final Attr attributeNode, final String attributeValue) {

      if ("name".equals(attributeName)) {

         name = attributeValue;
      } else if ("homeAloneTimeout".equals(attributeName)) {

         homeAloneTimeoutMillis = StringUtils.readTime(attributeValue);

         applyCommandLineOverwriteToHomeAloneTimeoutMillis();
      } else if ("clusterAnnouncementTimeout".equals(attributeName)) {

         clusterAnnouncementTimeoutMillis = StringUtils.readTime(attributeValue);

         applyCommandLineOverwriteToClusterAnnouncementTimeoutMillis();
      } else if ("clusterSurveyTimeout".equals(attributeName)) {

         clusterSurveyTimeoutMillis = StringUtils.readTime(attributeValue);

         applyCommandLineOverwriteToClusterSurveyTimeoutMillis();
      } else if ("worstCaseLatency".equals(attributeName)) {

         worstCaseLatencyMillis = StringUtils.readTime(attributeValue);
      } else if ("minimumNodeCount".equals(attributeName)) {

         minimumNodeCount = Integer.parseInt(attributeValue);
      }
   }


   protected void postProcessRead() {

      super.postProcessRead();

      applyCommandLineOverwriteToHomeAloneTimeoutMillis();
   }


   /**
    * Configures default values. This method is used to set up default values of the cluster configuration when it is
    * not present in the configuration file.
    */
   public void setUpDefaults() {

      // Set up defaults
      name = "Cacheonix";
      homeAloneTimeoutMillis = DEFAULT_HOME_ALONE_TIMEOUT_MILLS;
      worstCaseLatencyMillis = DEFAULT_WORST_CASE_LATENCY_MILLS;
      minimumNodeCount = DEFAULT_MINIMUM_NODE_COUNT;

      // Apply overrides if any
      applyCommandLineOverwriteToClusterAnnouncementTimeoutMillis();
      applyCommandLineOverwriteToClusterSurveyTimeoutMillis();
      applyCommandLineOverwriteToHomeAloneTimeoutMillis();
   }


   private void applyCommandLineOverwriteToHomeAloneTimeoutMillis() {

      final Long systemPropertyHomeAloneTimeoutMillis = SystemProperty.CACHEONIX_HOME_ALONE_TIMEOUT_VALUE_MILLIS;
      if (systemPropertyHomeAloneTimeoutMillis != null) {

         homeAloneTimeoutMillis = systemPropertyHomeAloneTimeoutMillis;
      }
   }


   private void applyCommandLineOverwriteToClusterAnnouncementTimeoutMillis() {

      final Long value = SystemProperty.CACHEONIX_CLUSTER_ANNOUNCEMENT_TIMEOUT_VALUE_MILLIS;
      if (value != null) {

         clusterAnnouncementTimeoutMillis = value;
      }
   }


   private void applyCommandLineOverwriteToClusterSurveyTimeoutMillis() {

      final Long value = SystemProperty.CACHEONIX_CLUSTER_SURVEY_TIMEOUT_VALUE_MILLIS;
      if (value != null) {

         clusterSurveyTimeoutMillis = value;
      }
   }


   public String toString() {

      return "ClusterConfiguration{" +
              "name='" + name + '\'' +
              ", homeAloneTimeoutMillis=" + homeAloneTimeoutMillis +
              ", clusterAnnouncementTimeoutMillis=" + clusterAnnouncementTimeoutMillis +
              ", clusterSurveyTimeoutMillis=" + clusterSurveyTimeoutMillis +
              ", worstCaseLatencyMillis=" + worstCaseLatencyMillis +
              ", minimumNodeCount=" + minimumNodeCount +
              ", propertyList=" + propertyList +
              "} " + super.toString();
   }
}
