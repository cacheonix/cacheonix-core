/**
 *
 */
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
package org.cacheonix.impl.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cacheonix.cache.annotations.CacheConfiguration;
import org.cacheonix.cache.annotations.CacheDataSource;
import org.cacheonix.cache.annotations.CacheInvalidate;
import org.cacheonix.cache.annotations.CacheKey;
import org.objectweb.asm.Type;

/**
 * Utility class that holds Cacheonix Annotation information
 * <p/>
 * <p/>
 * This class is used when reading the class file to see if the interested Cacheonix Annotations and their parameters
 * are present. It holds annotation name, it's parameters and their default values
 */
public class CacheonixAnnotation {

   public final Type atype;

   public final ETransformationState stateForProcessing;

   public final List<AnnotationParameter> parameters;

   public static final String CACHE_DATA_SOURCE_DESCRIPTOR = Type.getDescriptor(CacheDataSource.class);

   public static final String CACHE_INVALIDATE_DESCRIPTOR = Type.getDescriptor(CacheInvalidate.class);

   public static final String CACHE_KEY_DESCRIPTOR = Type.getDescriptor(CacheKey.class);

   public static final String CACHECONFIGURATION_CONFIGURATION_PATH = "configurationPath";

   public static final String CACHECONFIGURATION_CACHE_NAME = "cacheName";

   public static final String CACHECONFIGURATION_CONFIGURATION_PATH_DEFAULT = "cacheonix-config.xml";

   public static final String CACHEDATASOURCE_EXPIRATION_TIME_MILLIS = "expirationTimeMillis";

   public static final int CACHEDATASOURCE_EXPIRATION_TIME_MILLIS_DEFAULT_VALUE = -1;


   /**
    * Class constructor
    *
    * @param atype              Type of the annotation
    * @param stateForProcessing State of the ClassVisitor while parsing the class byte code. The state is used to detect
    *                           whether it is processing Class level annotation or method level annotation
    * @param parameters         Collection of parameters for the annotation
    */
   CacheonixAnnotation(final Type atype,
                       final ETransformationState stateForProcessing,
                       final List<AnnotationParameter> parameters) {

      this.atype = atype;
      this.stateForProcessing = stateForProcessing;
      this.parameters = new ArrayList<AnnotationParameter>(parameters);
   }


   /**
    * Creates a Map of Annotation type descriptor as the key and CacheonixAnnotation class containing the list of
    * annotation parameters as the value
    *
    * @return Map with Annotation type descriptor as the key and CacheonixAnnotation object as the value
    */
   public static Map<String, CacheonixAnnotation> annotationMapCreator() {

      final Map<String, CacheonixAnnotation> resMap = new HashMap<String, CacheonixAnnotation>();

      {
         final Type tp = Type.getType(CacheConfiguration.class);
         final List<AnnotationParameter> paramList = new ArrayList<AnnotationParameter>();
         paramList.add(new AnnotationParameter(
                 CACHECONFIGURATION_CONFIGURATION_PATH,
                 CACHECONFIGURATION_CONFIGURATION_PATH_DEFAULT, Type.getType(String.class)));
         paramList.add(new AnnotationParameter(
                 CACHECONFIGURATION_CACHE_NAME, "CacheonixAnnotatedCache",
                 Type.getType(String.class)));
         resMap.put(tp.getDescriptor(), new CacheonixAnnotation(tp,
                 ETransformationState.READING_CONFIG_ANNOTATION, paramList));
      }

      {
         final Type tp = Type.getType(CacheDataSource.class);
         final List<AnnotationParameter> paramList = new ArrayList<AnnotationParameter>();
         paramList.add(new AnnotationParameter(
                 CACHEDATASOURCE_EXPIRATION_TIME_MILLIS, "-1", Type
                 .getType(Integer.class)));
         resMap.put(tp.getDescriptor(), new CacheonixAnnotation(tp,
                 ETransformationState.READING_METHOD_ANNOTATION, paramList));
      }

      {
         final Type tp = Type.getType(CacheInvalidate.class);
         resMap.put(tp.getDescriptor(), new CacheonixAnnotation(tp,
                 ETransformationState.READING_METHOD_ANNOTATION, null));
      }

      return resMap;
   }
}
