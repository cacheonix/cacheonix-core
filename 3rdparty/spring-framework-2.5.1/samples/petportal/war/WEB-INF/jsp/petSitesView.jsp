<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1>Pet Sites</h1>

<form method="post" action="<portlet:actionURL/>">

	<c:choose>
		<c:when test="${empty petSites}">
		  <p>
		    There are currently no sites to display.
		    Edit Mode allows you to add sites.
		  </p>
		</c:when>
		<c:otherwise>
			<select name="url">
				<c:forEach items="${petSites}" var="site">
					<option value="<c:out value="${site.value}"/>">
					    <c:out value="${site.key} [${site.value}]"/>
					</option>
				</c:forEach>
			</select>
			<br/>
			<button type="submit">View Selected</button>
		</c:otherwise>
	</c:choose>
</form>
<br/>
Switch to: 
<a href="<portlet:renderURL portletMode="edit"/>">
	Edit Mode
</a>
