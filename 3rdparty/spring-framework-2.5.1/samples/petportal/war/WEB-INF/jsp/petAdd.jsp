<%@ include file="/WEB-INF/jsp/include.jsp" %>

<c:choose>
    <c:when test="${empty page}">
        <c:set var="page" value="0"/>
    </c:when>
    <c:otherwise>
        <c:set var="page" value="${page}"/>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${page == 3}">
        <c:set var="nextPage" value="${null}"/>
    </c:when>
    <c:otherwise>
        <c:set var="nextPage" value="${page + 1}"/>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${page == 0}">
        <c:set var="prevPage" value="${null}"/>
    </c:when>
    <c:otherwise>
        <c:set var="prevPage" value="${page - 1}"/>
    </c:otherwise>
</c:choose>

<h1>Add New Pet</h1>

<portlet:actionURL var="formAction">
    <portlet:param name="action" value="add"/>
    <portlet:param name="_page"><jsp:attribute name="value"><c:out value="${page}"/></jsp:attribute></portlet:param>
</portlet:actionURL>

<form:form commandName="pet" method="post" action="${formAction}">

    <form:errors path="*" cssStyle="color:red"/>
	
	<table border="0" cellpadding="4">
		<c:choose> 
			<c:when test="${page == 0}" >
				<tr>
				    <th>Species</th>
				    <td><form:input path="species" size="30" maxlength="80"/></td>
				</tr>
			</c:when> 
			<c:when test="${page == 1}" > 
			    <tr>
				    <th>Breed</th>
				    <td><form:input path="breed" size="30" maxlength="80"/></td>
				</tr>
			</c:when> 
			<c:when test="${page == 2}" >
		        <tr>
			        <th>Name</th>
			        <td><form:input path="name" size="30" maxlength="80"/></td>
			    </tr>
			</c:when> 
			<c:when test="${page == 3}" > 
				<tr>
				    <th>Birthdate (<c:out value="${dateFormat}"/>)</th>
			        <td><form:input path="birthdate" size="30" maxlength="80"/></td>
				</tr>
			</c:when> 
		</c:choose>  
		<tr>
			<th colspan="2">
				<c:choose>
				    <c:when test="${empty prevPage}">
				        <input type="submit" value="Previous" disabled/>
				    </c:when>
				    <c:otherwise>
				        <input type="submit" name="_target<c:out value="${prevPage}"/>" value="Previous">
				    </c:otherwise>
				</c:choose>
				<c:choose>
				    <c:when test="${empty nextPage}">
				        <input type="submit" value="Next" disabled/>
				    </c:when>
				    <c:otherwise>
				        <input type="submit" name="_target<c:out value="${nextPage}"/>" value="Next">
				    </c:otherwise>
				</c:choose>
				<input type="submit" name="_finish" value="Finish"/>
				<input type="submit" name="_cancel" value="Cancel"/>
			</th>
		</tr>
	</table>
</form:form>

<p style="text-align:center;"><a href="<portlet:renderURL portletMode="view" windowState="normal"/>">- <spring:message code="button.home"/> -</a></p>
