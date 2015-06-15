<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h3>An Error Occurred</h3>

message: <c:out value="${exception.message}"/>

<p style="text-align:center;"><a href="<portlet:renderURL portletMode="view" windowState="normal"/>">- <spring:message code="button.home"/> -</a></p>