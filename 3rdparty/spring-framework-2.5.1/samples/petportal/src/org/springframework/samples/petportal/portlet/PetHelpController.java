package org.springframework.samples.petportal.portlet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Juergen Hoeller
 */
@Controller
@RequestMapping("HELP")
public class PetHelpController {

	@RequestMapping
	public String showHelp() {
		return "petHelp";
	}

}
