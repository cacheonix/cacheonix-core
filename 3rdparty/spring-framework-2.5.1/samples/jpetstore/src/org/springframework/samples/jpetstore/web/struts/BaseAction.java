package org.springframework.samples.jpetstore.web.struts;

import javax.servlet.ServletContext;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionServlet;

import org.springframework.samples.jpetstore.domain.logic.PetStoreFacade;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Superclass for Struts actions in JPetStore's web tier.
 *
 * <p>Looks up the Spring WebApplicationContext via the ServletContext
 * and obtains the PetStoreFacade implementation from it, making it
 * available to subclasses via a protected getter method.
 *
 * <p>As alternative to such a base class, consider using Spring's
 * ActionSupport class for Struts, which pre-implements
 * WebApplicationContext lookup in a generic fashion.
 *
 * @author Juergen Hoeller
 * @since 30.11.2003
 * @see #getPetStore
 * @see org.springframework.web.context.support.WebApplicationContextUtils#getRequiredWebApplicationContext
 * @see org.springframework.web.struts.ActionSupport
 */
public abstract class BaseAction extends Action {

  private PetStoreFacade petStore;

	public void setServlet(ActionServlet actionServlet) {
		super.setServlet(actionServlet);
		if (actionServlet != null) {
			ServletContext servletContext = actionServlet.getServletContext();
			WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			this.petStore = (PetStoreFacade) wac.getBean("petStore");
		}
	}

	protected PetStoreFacade getPetStore() {
		return petStore;
	}

}
