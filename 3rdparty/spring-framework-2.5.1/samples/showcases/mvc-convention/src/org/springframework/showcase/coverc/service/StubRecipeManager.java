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

import java.util.*;

/**
 * Stub {@link RecipeManager} implementation that maintains simple
 * in-memory state for {@link org.springframework.showcase.coverc.domain.Recipe Recipes}.
 *
 * @author Rick Evans
 */
public class StubRecipeManager implements RecipeManager {

    private Map recipes = new TreeMap();


    /**
     * Creates a new instance of the {@link StubRecipeManager} class.
     */
    public StubRecipeManager() {
        loadRecipes();
    }


    public void save(Recipe recipe) {
        // passed in should be a clone - simply replace
        putRecipe(recipe);
    }

    public Recipe findById(Long id) {
        Recipe recipe = (Recipe) this.recipes.get(id);
        if (recipe != null) {
            return cloneRecipe(recipe);
        }
        return null;
    }

    public Collection findAll() {
        List recipeList = new ArrayList();
        Iterator itr = this.recipes.values().iterator();
        while (itr.hasNext()) {
            Recipe recipe = (Recipe) itr.next();
            recipeList.add(cloneRecipe(recipe));
        }
        return recipeList;
    }


    private void loadRecipes() {
        Recipe recipe = new Recipe();
        recipe.setId(new Long(1));
        recipe.setName("Goats Cheese with beetroot sauce");

        putRecipe(recipe);

        recipe = new Recipe();
        recipe.setId(new Long(2));
        recipe.setName("Bucket of MaccyDeez chicken de Harrop");

        putRecipe(recipe);

        recipe = new Recipe();
        recipe.setId(new Long(3));
        recipe.setName("Deep fried battered Hershey bar");

        putRecipe(recipe);
    }

    private void putRecipe(Recipe recipe) {
        this.recipes.put(recipe.getId(), recipe);
    }

    private Recipe cloneRecipe(Recipe recipe) {
        try {
            return (Recipe) recipe.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Unable to clone recipe.");
        }
    }

}
