<%@ include file="IncludeTop.jsp" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<html:form styleId="workingAccountForm" method="post" action="/shop/editAccount.do">
<html:hidden name="workingAccountForm" property="validate" value="editAccount" />
<html:hidden name="workingAccountForm" property="account.username" />

<table cellpadding="10" cellspacing="0" align="center" border="1" bgcolor="#dddddd"><tr><td>

<font color="darkgreen"><h3>User Information</h3></font>
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#FFFF88">
<tr bgcolor="#FFFF88"><td>
User ID:</td><td><c:out value="${workingAccountForm.account.username}"/>
</td></tr><tr bgcolor="#FFFF88"><td>
New password:</td><td><html:password name="workingAccountForm" property="account.password" />
</td></tr><tr bgcolor="#FFFF88"><td>
Repeat password:</td><td> <html:password name="workingAccountForm" property="repeatedPassword" />
</td></tr>
</table>

<%@ include file="IncludeAccountFields.jsp" %>

</td></tr></table>

<br /><center>
<input border="0" type="image" src="../images/button_submit.gif" name="submit" value="Save Account Information" />
</center>

</html:form>
<p>
<center><b><a href="<c:url value="/shop/listOrders.do"/>">My Orders</a></b></center>

<%@ include file="IncludeBottom.jsp" %></p>
