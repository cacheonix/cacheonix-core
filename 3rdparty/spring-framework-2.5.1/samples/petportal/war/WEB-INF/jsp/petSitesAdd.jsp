<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1>Add a Pet Site</h1>

<portlet:actionURL var="formAction" portletMode="edit">
    <portlet:param name="action" value="add"/>
</portlet:actionURL>
                            
<form:form commandName="site" method="post" action="${formAction}">
    <form:errors path="*" cssStyle="color:red"/>
    <table>
	    <tr>
	        <td>Name: </td>
	        <td><form:input path="name" size="20" maxlength="20"/></td>
	    </tr> 
	    <tr>
	        <td>URL: </td>
	        <td><form:input path="url" size="20" maxlength="255"/></td>
	    </tr>
	</table>
	<button type="submit">Add</button>
</form:form>