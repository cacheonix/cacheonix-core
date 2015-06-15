<%@ include file="/WEB-INF/jsp/include.jsp" %>

<font color="red"><c:out value="${exception.message}"/></font>

<br/>

Please <a href="<portlet:renderURL/>">try again</a>.