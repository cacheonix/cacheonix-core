<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1>Help Mode</h1>

<p>This portlet simply displays a different view depending on the 
mode of the portlet.</p>

<p>You can switch modes using the controls in your portal.  You can also
experiment with the URLs below for changing mode and window state.</p>

<h2>Portlet URLs</h2>
<ul>
	<li><a href="<portlet:renderURL portletMode="view" />">View Mode</a>
	<li><a href="<portlet:renderURL portletMode="edit" />">Edit Mode</a>
	<li><a href="<portlet:renderURL portletMode="help" />">Help Mode</a>
	<li><a href="<portlet:renderURL windowState="normal" />">Normal State</a>
	<li><a href="<portlet:renderURL windowState="maximized" />">Maximized State</a>
	<li><a href="<portlet:renderURL windowState="minimized" />">Minimized State</a>
</ul>
