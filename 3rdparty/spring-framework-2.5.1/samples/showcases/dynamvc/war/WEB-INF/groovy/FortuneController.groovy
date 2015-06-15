package org.springframework.showcase.fortune.web;

import org.springframework.showcase.fortune.service.FortuneService;
import org.springframework.showcase.fortune.domain.Fortune;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple Groovy-based implementation of the Fortune-telling Controller.
 *
 * @author Rick Evans
 */
public class FortuneController implements Controller {

    @Property FortuneService fortuneService

    public ModelAndView handleRequest(
            HttpServletRequest request, HttpServletResponse httpServletResponse) {
        return new ModelAndView("tell", "fortune", this.fortuneService.tellFortune())
    }

}
