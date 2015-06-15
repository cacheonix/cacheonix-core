<%@ include file="IncludeTop.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!-- Support for Spring errors holder -->
<spring:bind path="orderForm.*">
  <c:forEach var="error" items="${status.errorMessages}">
    <B><FONT color=RED>
      <BR><c:out value="${error}"/>
    </FONT></B>
  </c:forEach>
</spring:bind>

<form action="<c:url value="/shop/newOrder.do"/>" method="post">

<TABLE bgcolor="#008800" border=0 cellpadding=3 cellspacing=1 bgcolor="#FFFF88">
<TR bgcolor="#FFFF88"><TD colspan=2>
<FONT color=GREEN size=4><B>Shipping Address</B></FONT>
</TD></TR>

<TR bgcolor="#FFFF88"><TD>
First name:</TD><TD>
  <spring:bind path="orderForm.order.shipToFirstName">
	  <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Last name:</TD><TD>
  <spring:bind path="orderForm.order.shipToLastName">
	  <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Address 1:</TD><TD>
  <spring:bind path="orderForm.order.shipAddress1">
	  <input type="text" size="40" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Address 2:</TD><TD>
  <spring:bind path="orderForm.order.shipAddress2">
	  <input type="text" size="40" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
City: </TD><TD>
  <spring:bind path="orderForm.order.shipCity">
	  <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
State:</TD><TD>
  <spring:bind path="orderForm.order.shipState">
	  <input type="text" size="4" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Zip:</TD><TD>
  <spring:bind path="orderForm.order.shipZip">
	  <input type="text" size="10" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Country: </TD><TD>
  <spring:bind path="orderForm.order.shipCountry">
	  <input type="text" size="15" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>

</TABLE>
<P>
<input type="image" src="../images/button_submit.gif">

</form>

<%@ include file="IncludeBottom.jsp" %>
