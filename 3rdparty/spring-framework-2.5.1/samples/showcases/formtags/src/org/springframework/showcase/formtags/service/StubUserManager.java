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

import java.util.*;

/**
 * Stub {@link UserManager} implementation that maintains simple
 * in-memory state for users and countries.
 *
 * @author Rob Harrop
 */
public class StubUserManager implements UserManager {


    private Map users = new TreeMap();
    private Map countries = new TreeMap();


    /**
     * Creates a new instance of the {@link StubUserManager} class.
     */
    public StubUserManager() {
        loadCountries();
        loadUsers();
    }


    public void save(User user) {
        // passed in should be a clone - simply replace
        putUser(user);
    }

    public Collection findAllCountries() {
        return this.countries.values();
    }

    public Country findCountry(String code) {
        return (Country) this.countries.get(code);
    }

    public User findById(Integer id) {
        User user = (User) this.users.get(id);

        if (user != null) {
            return cloneUser(user);
        }

        return null;
    }

    public Collection findAll() {
        List userList = new ArrayList();
        Iterator itr = this.users.values().iterator();
        while (itr.hasNext()) {
            User user = (User) itr.next();
            userList.add(cloneUser(user));
        }
        return userList;
    }


    private void loadCountries() {
        putCountry(new Country("AT", "Austria"));
        putCountry(new Country("UK", "United Kingdom"));
        putCountry(new Country("US", "United States"));
    }

    private void loadUsers() {
        User u = new User();
        u.setId(new Integer(1));
        u.setFirstName("Harry");
        u.setLastName("Potter");
        u.setNotes("Promising Wizard...");
        u.setCountry(findCountry("UK"));
        u.setSex('M');
        u.setHouse("Gryffindor");
        u.getPreferences().setReceiveNewsletter(true);
        u.getPreferences().setInterests(new String[]{"Quidditch"});
        u.getPreferences().setFavouriteWord("Magic");
        u.setPassword("password");

        putUser(u);

        u = new User();
        u.setId(new Integer(2));
        u.setFirstName("Ronald");
        u.setLastName("Weasly");
        u.setNotes("Friends with Harry Potter.");
        u.setCountry(findCountry("UK"));
        u.setSex('M');
        u.setHouse("Gryffindor");
        u.setPassword("password");

        putUser(u);

        u = new User();
        u.setId(new Integer(3));
        u.setFirstName("Hermione");
        u.setLastName("Granger");
        u.setNotes("Friends with Harry Potter.");
        u.setCountry(findCountry("UK"));
        u.setSex('F');
        u.setHouse("Gryffindor");
        u.setPassword("password");

        putUser(u);
    }

    private void putUser(User user) {
        this.users.put(user.getId(), user);
    }

    private void putCountry(Country country) {
        this.countries.put(country.getCode(), country);
    }

    private User cloneUser(User user) {
        try {
            return (User) user.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Unable to clone user.");
        }
    }

}
