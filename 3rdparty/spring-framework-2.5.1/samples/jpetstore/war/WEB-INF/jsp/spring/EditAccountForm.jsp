<%@ include file="IncludeTop.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!-- Support for Spring errors object -->
<spring:bind path="accountForm.*">
  <c:forEach var="error" items="${status.errorMessages}">
    <B><FONT color=RED>
      <BR><c:out value="${error}"/>
    </FONT></B>
  </c:forEach>
</spring:bind>

<c:if test="${accountForm.newAccount}">
<form action="<c:url value="/shop/newAccount.do"/>" method="post">
</c:if>
<c:if test="${!accountForm.newAccount}">
<form action="<c:url value="/shop/editAccount.do"/>" method="post">
</c:if>

<table cellpadding="10" cellspacing="0" align="center" border="1" bgcolor="#dddddd"><tr><td>

<font color="darkgreen"><h3>User Information</h3></font>
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#FFFF88">
<tr bgcolor="#FFFF88"><td>
User ID:</td><td>
<c:if test="${accountForm.newAccount}">
  <spring:bind path="accountForm.account.username">
	  <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</c:if>
<c:if test="${!accountForm.newAccount}">
  <c:out value="${accountForm.account.username}"/>
</c:if>
</td></tr><tr bgcolor="#FFFF88"><td>
New password:</td><td>
  <spring:bind path="accountForm.account.password">
	  <input type="password" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</td></tr><tr bgcolor="#FFFF88"><td>
Repeat password:</td><td>
  <spring:bind path="accountForm.repeatedPassword">
	  <input type="password" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
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
