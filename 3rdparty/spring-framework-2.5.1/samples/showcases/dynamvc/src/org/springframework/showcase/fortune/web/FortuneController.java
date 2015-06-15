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

package org.springframework.showcase.fortune.web;

import org.springframework.showcase.fortune.service.FortuneService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple Java-based implementation of the main Fortune Controller.
 *
 * @author Rick Evans
 */
public class FortuneController implements Controller {

    private FortuneService fortuneService;


    public void setFortuneService(FortuneService fortuneService) {
        this.fortuneService = fortuneService;
    }


    public ModelAndView handleRequest(
            HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("tell", "fortune", this.fortuneService.tellFortune());
    }

}
