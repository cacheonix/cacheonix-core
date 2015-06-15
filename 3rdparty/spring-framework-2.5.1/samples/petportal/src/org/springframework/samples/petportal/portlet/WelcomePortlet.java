package org.springframework.samples.petportal.portlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

/**
 * An example of a pre-existing JSR-168 compliant portlet which is used
 * in the demonstration of Spring's PortletWrappingController. View the
 * bean definitions in welcome-portlet.xml for details.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
public class WelcomePortlet extends GenericPortlet {

	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<h1>Welcome to the Pet Portal!</h1>");
		if(request.getWindowState().equals(WindowState.MAXIMIZED)) {
			out.println("<p>This portlet delegates to an existing JSR-168 portlet via a HandlerAdapter</p>");
			out.println("<p>(see WEB-INF/context/welcome-context.xml for the details).</p>");
			out.println("<p>Portlet Name: " + this.getPortletName() + "</p>");
			out.println("<p>Init Parameters:</p><ul>");
			for (Enumeration e = this.getInitParameterNames(); e.hasMoreElements();) {
					String name = (String)e.nextElement();
					out.println("<li>" + name + " = " + this.getInitParameter(name) + "</li>");
			}
			out.println("</ul>");
			out.println("<p>Your Locale: " + request.getLocale().toString() + "</p>");
		}
	}

}
