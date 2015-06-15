<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1>Modify Preferences</h1>

<p> 
Current Date Format is: <c:out value="${currentFormat}"/>
</p>

<form method="post" action="<portlet:actionURL/>">
	<select name="dateFormat">
		<c:forEach items="${availableFormats}" var="format">
			<option><c:out value="${format}"/></option>
		</c:forEach>
	</select>
	<br/>
	<button type="submit">Modify</button>
</form>

<br/>
Back to <a href="<portlet:renderURL portletMode="view"/>">View Mode</a>