<%@ include file="IncludeTop.jsp" %>

<c:if test="${!empty message}">
  <b><font color="RED"><c:url value="${message}"/></font></b>
</c:if>

<form action="<c:url value="/shop/signon.do"/>" method="POST">

<c:if test="${!empty signonForwardAction}">
<input type="hidden" name="forwardAction" value="<c:url value="${signonForwardAction}"/>"/>
</c:if>

<table align="center" border="0">
<tr>
<td colspan="2">Please enter your username and password.
<br />&nbsp;</td>
</tr>
<tr>
<td>Username:</td>
<td><input type="text" name="username" value="j2ee" /></td>
</tr>
<tr>
<td>Password:</td>
<td><input type="password" name="password" value="j2ee" /></td>
</tr>
<tr>
<td>&nbsp;</td>
<td><input type="image" border="0" src="../images/button_submit.gif" name="update" /></td>
</tr>
</table>

</form>

<center>
<a href="<c:url value="/shop/newAccountForm.do"/>">
<img border="0" src="../images/button_register_now.gif" />
</a>
</center>

<%@ include file="IncludeBottom.jsp" %>

