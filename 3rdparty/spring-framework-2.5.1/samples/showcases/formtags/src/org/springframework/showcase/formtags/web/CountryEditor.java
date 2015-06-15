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

package org.springframework.showcase.formtags.web;

import org.springframework.showcase.formtags.domain.Country;
import org.springframework.showcase.formtags.service.UserManager;

import java.beans.PropertyEditorSupport;

/**
 * Simple {@link java.beans.PropertyEditor} for the {@link org.springframework.showcase.formtags.domain.Country} class.
 * 
 * @author Rick Evans
 */
public class CountryEditor extends PropertyEditorSupport {

    private UserManager userManager;


    /**
     * Creates a new instance of the {@link org.springframework.showcase.formtags.web.CountryEditor} class.
     *
     * @param userManager the service object that is to be used to resolve country codes
     */
    public CountryEditor(UserManager userManager) {
        this.userManager = userManager;
    }


    public void setAsText(String text) throws IllegalArgumentException {
        setValue(this.userManager.findCountry(text));
    }

    public String getAsText() {
        if (getValue() == null) {
            return "";
        }
        return ((Country) getValue()).getCode();
    }

}
