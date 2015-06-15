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

import org.springframework.showcase.coverc.domain.Recipe;
import org.springframework.showcase.coverc.service.RecipeManager;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;

/**
 * Effects the edition of a {@link org.springframework.showcase.coverc.domain.Recipe}.
 *
 * @author Rick Evans
 */
public class EditRecipeController extends SimpleFormController {

    private RecipeManager recipeManager;


    public void setRecipeManager(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }


    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        long id = ServletRequestUtils.getRequiredLongParameter(request, "id");
        Recipe recipe = this.recipeManager.findById(new Long(id));
        return recipe;
    }


    protected void doSubmitAction(Object object) throws Exception {
        Recipe recipe = (Recipe) object;
        this.recipeManager.save(recipe);
    }

}
