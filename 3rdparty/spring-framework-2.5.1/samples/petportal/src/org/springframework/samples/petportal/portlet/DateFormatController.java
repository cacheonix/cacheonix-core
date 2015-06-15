package org.springframework.samples.petportal.portlet;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.portlet.PortletPreferences;

import org.springframework.samples.petportal.service.PetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This controller provides a simple example of modifying portlet preferences.
 * In this case, it allows the user to change the default date format.
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
@Controller
@RequestMapping("EDIT")
public class DateFormatController {
	
	private final Set availableFormats;
	
	public DateFormatController() {
		this.availableFormats = new LinkedHashSet(4);
		this.availableFormats.add(PetService.DEFAULT_DATE_FORMAT);
		this.availableFormats.add("MM-dd-yyyy");
		this.availableFormats.add("dd/MM/yyyy");
		this.availableFormats.add("dd-MM-yyyy");
	}

	/**
	 * In the render phase, the current format and available formats will be
	 * exposed to the 'dateFormat' view via the model.
	 */
	@RequestMapping
	public String showPreferences(PortletPreferences preferences, Model model) {
		model.addAttribute("currentFormat", preferences.getValue("dateFormat", PetService.DEFAULT_DATE_FORMAT));
		model.addAttribute("availableFormats", this.availableFormats);
		return "dateFormat";
	}

	/**
	 * In the action phase, the dateFormat preference is modified. To persist any
	 * modifications, the PortletPreferences must be stored.
	 */
	@RequestMapping
	public void changePreference(PortletPreferences preferences, @RequestParam("dateFormat") String dateFormat)
			throws Exception {

		preferences.setValue("dateFormat", dateFormat);
		preferences.store();
	}

}
