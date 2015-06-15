/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.showcase.coverc.service;

import org.springframework.showcase.coverc.domain.Recipe;

import java.util.Collection;

/**
 * Central service interface for the application.
 *
 * @author Rick Evans
 */
public interface RecipeManager {

    /**
     * Finds all of the {@link Recipe Recipes} in the system.
     * @return a {@link Collection} of all of the {@link Recipe Recipes} in the system.
     */
    Collection findAll();

    /**
     * Finds the specific {@link Recipe} identified by the supplied <code>id</code>.
     * @param id the value uniquely identifying a {@link Recipe}
     * @return the located {@link Recipe} or <code>null</code> if not found
     */
    Recipe findById(Long id);

    /**
     * Saves the supplied {@link Recipe} to persistent storage.
     * @param user the {@link Recipe} to be so saved
     */
    void save(Recipe user);

}
