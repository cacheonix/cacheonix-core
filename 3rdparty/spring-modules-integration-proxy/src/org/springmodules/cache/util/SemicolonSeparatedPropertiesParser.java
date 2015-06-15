/*
 * Created on Jan 18, 2005
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright @2005 the original author or authors.
 */

package org.springmodules.cache.util;

import java.util.Properties;

import org.springframework.util.StringUtils;

import org.springmodules.cache.regex.Match;
import org.springmodules.cache.regex.Perl5Regex;
import org.springmodules.cache.regex.Regex;

/**
 * <p>
 * Parses a String of form <code>key1=value1;key2=value2;keyN=valueN</code>,
 * and creates a <code>java.util.Properties</code>.
 * </p>
 *
 * @author Alex Ruiz
 */
public abstract class SemicolonSeparatedPropertiesParser {

  /**
   * Compiled representation of the regular expression pattern used to parse a
   * String of form "key=value".
   */
  private static final Regex KEY_VALUE_REGEX = new Perl5Regex(
      "([\\w]+)=([\\w /,\\*.:=&?]+)");

  private static final String PROPERTY_DELIMITER = ";";

  /**
   * Creates a <code>java.util.Properties</code> from the specified String.
   *
   * @param text
   *          the String to parse.
   * @throws IllegalArgumentException
   *           if the specified property does not match the regular expression
   *           pattern defined in <code>KEY_VALUE_REGEX</code>.
   * @throws IllegalArgumentException
   *           if the set of properties already contains the property specified
   *           by the given String.
   * @return a new instance of <code>java.util.Properties</code> created from
   *         the given text.
   */
  public static Properties parseProperties(String text)
      throws IllegalArgumentException {
    String newText = text;

    if (!StringUtils.hasText(newText)) {
      return null;
    }

    if (newText.endsWith(PROPERTY_DELIMITER)) {
      // remove ';' at the end of the text (if applicable)
      newText = newText.substring(0, newText.length()
          - PROPERTY_DELIMITER.length());

      if (!StringUtils.hasText(newText)) {
        return null;
      }
    }

    Properties properties = new Properties();
    String[] propertiesAsText = StringUtils.delimitedListToStringArray(newText,
        PROPERTY_DELIMITER);

    int propertyCount = propertiesAsText.length;
    for (int i = 0; i < propertyCount; i++) {
      String property = propertiesAsText[i];
      Match match = KEY_VALUE_REGEX.match(property);

      if (!match.isSuccessful()) {
        String message = "The String " + StringUtils.quote(property)
            + " should match the regular expression pattern "
            + StringUtils.quote(KEY_VALUE_REGEX.getPattern());
        throw new IllegalArgumentException(message);
      }

      String[] groups = match.getGroups();
      String key = groups[1].trim();
      String value = groups[2].trim();

      if (properties.containsKey(key)) {
        throw new IllegalArgumentException("The property "
            + StringUtils.quote(key) + " is specified more than once");
      }
      properties.setProperty(key, value);
    }
    return properties;
  }

}