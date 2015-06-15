/* 
 * Created on Apr 20, 2006
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
 * Copyright @2006 the original author or authors.
 */
package org.springmodules.cache.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springmodules.cache.util.TextMatcher;

import org.springframework.util.ClassUtils;

/**
 * TODO Describe this class
 * 
 * @author Alex Ruiz
 */
final class MethodMatcher {

  private class MethodFQN {

    final String className;

    final String methodName;

    MethodFQN(String fqn) throws IllegalArgumentException {
      int separatorIndex = methodSeparator(fqn);
      className = fqn.substring(0, separatorIndex);
      methodName = fqn.substring(separatorIndex + 1);
    }

    private int methodSeparator(String fqn) {
      int separatorIndex = fqn.lastIndexOf(".");
      if (separatorIndex == -1)
        throw new IllegalArgumentException("'" + fqn
            + "' is not a fully qualified name");
      return separatorIndex;
    }
  }

  Collection matchingMethods(String fullyQualifiedMethodName)
      throws IllegalArgumentException {
    MethodFQN parser = new MethodFQN(fullyQualifiedMethodName);
    List matchingMethods = new ArrayList();
    Method[] methods = methods(parser.className);
    for (int i = 0; i < methods.length; i++) {
      if (matchingMethod(methods[i], parser.methodName))
        matchingMethods.add(methods[i]);
    }
    return matchingMethods;
  }

  private Class load(String className) throws IllegalArgumentException {
    Class declaringClass = null;
    try {
      declaringClass = ClassUtils.forName(className);
    } catch (ClassNotFoundException exception) {
      throw new IllegalArgumentException("Class '" + className + "' not found");
    }
    return declaringClass;
  }

  private boolean matchingMethod(Method method, String target) {
    String n = method.getName();
    return n.equals(target) || TextMatcher.isMatch(n, target);
  }

  private Method[] methods(String className) throws IllegalArgumentException {
    Class declaringClass = load(className);
    return declaringClass.getDeclaredMethods();
  }
}
