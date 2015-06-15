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

package org.springframework.showcase.formtags.validation;

import org.springframework.showcase.formtags.domain.Country;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Simple {@link Validator} implementation for {@link Country} instances.
 *
 * @author Rick Evans
 */
public class CountryValidator implements Validator {


    public static final String DEFAULT_BAD_PLACEHOLDER_CODE = "-";


    private String badPlaceholderCode = DEFAULT_BAD_PLACEHOLDER_CODE;


    public void setBadPlaceholderCode(String badPlaceholderCode) {
        this.badPlaceholderCode = StringUtils.hasText(badPlaceholderCode)
                ? badPlaceholderCode : DEFAULT_BAD_PLACEHOLDER_CODE;
    }


    public boolean supports(Class candidate) {
        return Country.class.isAssignableFrom(candidate);
    }

    public void validate(Object object, Errors errors) {
        Country country = (Country) object;
        if (country.getCode() == this.badPlaceholderCode) {
            errors.rejectValue("bad.country.selected", "Please select a valid country");
        }
    }

}
