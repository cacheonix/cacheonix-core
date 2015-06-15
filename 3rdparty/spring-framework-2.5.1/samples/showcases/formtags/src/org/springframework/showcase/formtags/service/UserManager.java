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

package org.springframework.showcase.formtags.service;

import org.springframework.showcase.formtags.domain.Country;
import org.springframework.showcase.formtags.domain.User;

import java.util.Collection;

/**
 * Central service interface for the application.
 *
 * @author Rob Harrop
 */
public interface UserManager {

    /**
     * Finds all of the {@link User Users} in the system.
     *
     * @return a {@link Collection} of all of the {@link User Users} in the system.
     */
    Collection findAll();

    /**
     * Finds the specific {@link User} identified by the supplied <code>id</code>.
     *
     * @param id the value uniquely identifying a {@link User}
     * @return the located {@link User} or <code>null</code> if not found
     */
    User findById(Integer id);

    /**
     * Saves the supplied {@link User} to persistent storage.
     *
     * @param user the {@link User} to be so saved
     */
    void save(User user);

    /**
     * Finds all of the {@link Country Countries} in the system.
     *
     * @return all of the {@link Country Countries} in the system
     */
    Collection findAllCountries();

    /**
     * Finds the specific {@link Country} identified by the supplied (country) <code>code</code>.
     *
     * @param code the country code to be used to locate a specific {@link Country}
     * @return the specific {@link Country} identified by the supplied (country) <code>code</code>
     */
    Country findCountry(String code);
}
