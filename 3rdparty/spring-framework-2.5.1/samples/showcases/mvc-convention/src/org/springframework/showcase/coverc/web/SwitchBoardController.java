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

package org.springframework.showcase.coverc.web;

import org.springframework.showcase.coverc.service.RecipeManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The central switchboard
 * {@link org.springframework.web.servlet.mvc.Controller} implementation for the
 * application.
 *
 * @author Rick Evans
 */
public class SwitchBoardController extends MultiActionController {

    private RecipeManager recipeManager;


    /**
     * Sets the {@link RecipeManager} that to which this presentation component delegates
     * in order to perform complex business logic.
     *
     * @param recipeManager the {@link RecipeManager} to which this presentation
     *                      component delegates in order to perform complex business logic
     */
    public void setRecipeManager(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }


    public ModelAndView listRecipes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView().addObject(this.recipeManager.findAll());
    }

}
