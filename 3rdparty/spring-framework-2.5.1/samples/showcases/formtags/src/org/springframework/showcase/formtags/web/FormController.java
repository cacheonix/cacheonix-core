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

import org.springframework.core.enums.StaticLabeledEnumResolver;
import org.springframework.showcase.formtags.domain.Colour;
import org.springframework.showcase.formtags.domain.Country;
import org.springframework.showcase.formtags.domain.User;
import org.springframework.showcase.formtags.service.UserManager;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorSupport;
import java.util.Map;

/**
 * The central form controller for this showcase application.
 *
 * @author Rob Harrop
 */
public class FormController extends SimpleFormController {

	private UserManager userManager;


	/**
	 * Sets the {@link UserManager} to which this presentation component
	 * delegates in order to perform complex business logic.
	 * @param userManager the {@link UserManager} to which this presentation
	 *                    component delegatesin order to perform complex business logic
	 */
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}


    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Country.class, new CountryEditor(this.userManager));
        binder.registerCustomEditor(Colour.class, new PropertyEditorSupport() {
            public void setAsText(String string) throws IllegalArgumentException {
                Short code = new Short(string);
                StaticLabeledEnumResolver resolver = new StaticLabeledEnumResolver();
                setValue(resolver.getLabeledEnumByCode(Colour.class, code));
            }
        });
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        return new ModelMap(this.userManager.findAllCountries())
            .addObject("skills", getSkills())
            .addObject(this.userManager.findAll());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
        return this.userManager.findById(new Integer(id));
    }

    protected void doSubmitAction(Object managedResource) throws Exception {
        this.userManager.save((User) managedResource);
    }


    private String[] getSkills() {
        return new String[]{
                "Potions",
                "Herbology",
                "Quidditch"
        };
    }

}
