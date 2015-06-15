<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1>Pet Sites</h1>

<form method="post" action="<portlet:actionURL/>">
	<c:forEach items="${petSites}" var="site">
		<c:out value="${site.key} [${site.value}]"/>
		<a href="<portlet:actionURL portletMode="edit">
			        <portlet:param name="action" value="delete"/>
			        <portlet:param name="site">
			            <jsp:attribute name="value">
			                <c:out value="${site.key}"/>
			            </jsp:attribute>
			        </portlet:param>
			     </portlet:actionURL>">
			Remove
		</a>
		<br/>
	</c:forEach>
	<br/>
</form>
<br/>
<a href="<portlet:renderURL portletMode="edit">
	        <portlet:param name="action" value="add"/>
	     </portlet:renderURL>">
	Add Site
</a>
 | Back to  
<a href="<portlet:renderURL portletMode="view"/>">
	View Mode
</a>
