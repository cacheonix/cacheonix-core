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

import org.springframework.showcase.formtags.service.UserManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple {@link org.springframework.web.servlet.mvc.Controller} implementation
 * that pretty much locates (and thus allows a {@link org.springframework.web.servlet.View}
 * to render a list) of all of the {@link org.springframework.showcase.formtags.domain.User Users}
 * in the application.
 *
 * @author Rob Harrop
 */
public class ListController extends AbstractController {


    private UserManager userManager;
    private String viewName;

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    /**
     * Sets the {@link UserManager} that to which this presentation component delegates
     * in order to perform complex business logic.
     *
     * @param userManager the {@link UserManager} that to which this presentation component delegates
     *                    in order to perform complex business logic
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView(viewName).addObject(this.userManager.findAll());
    }

}
