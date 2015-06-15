<%@ include file="IncludeTop.jsp" %>

<table align="left" bgcolor="#008800" border="0" cellspacing="2" cellpadding="2">
<tr><td bgcolor="#FFFF88">
<a href="<c:url value="/shop/index.do"/>"><b><font color="BLACK" size="2">&lt;&lt; Main Menu</font></b></a>
</td></tr>
<tr><td bgcolor="#FFFF88">
<%--
<html:link paramId="orderId" paramName="order" paramProperty="orderId" page="/shop/viewOrder.do?webservice=true"><b><font color="BLACK" size="2">Use Web Service</font></b></c:url>
--%>
</td></tr>
</table>

<c:if test="${!empty message}">
  <center><b><c:out value="${message}"/></b></center>
</c:if>

<p>

<table width="60%" align="center" border="0" cellpadding="3" cellspacing="1" bgcolor="#FFFF88">
<tr bgcolor="#FFFF88"><td align="center" colspan="2">
  <font size="4"><b>Order #<c:out value="${order.orderId}"/></b></font>
  <br /><font size="3"><b><fmt:formatDate value="${order.orderDate}" pattern="yyyy/MM/dd hh:mm:ss" /></b></font>
</td></tr>
<tr bgcolor="#FFFF88"><td colspan="2">
<font color="GREEN" size="4"><b>Payment Details</b></font>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Card Type:</td><td>
<c:out value="${order.cardType}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Card Number:</td><td><c:out value="${order.creditCard}"/> <font color="red" size="2">* Fake number!</font>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Expiry Date (MM/YYYY):</td><td><c:out value="${order.expiryDate}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td colspan="2">
<font color="GREEN" size="4"><b>Billing Address</b></font>
</td></tr>
<tr bgcolor="#FFFF88"><td>
First name:</td><td><c:out value="${order.billToFirstName}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Last name:</td><td><c:out value="${order.billToLastName}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Address 1:</td><td><c:out value="${order.billAddress1}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Address 2:</td><td><c:out value="${order.billAddress2}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
City: </td><td><c:out value="${order.billCity}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
State:</td><td><c:out value="${order.billState}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Zip:</td><td><c:out value="${order.billZip}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Country: </td><td><c:out value="${order.billCountry}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td colspan="2">
<font color="GREEN" size="4"><b>Shipping Address</b></font>
</td></tr><tr bgcolor="#FFFF88"><td>
First name:</td><td><c:out value="${order.shipToFirstName}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Last name:</td><td><c:out value="${order.shipToLastName}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Address 1:</td><td><c:out value="${order.shipAddress1}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Address 2:</td><td><c:out value="${order.shipAddress2}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
City: </td><td><c:out value="${order.shipCity}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
State:</td><td><c:out value="${order.shipState}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Zip:</td><td><c:out value="${order.shipZip}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Country: </td><td><c:out value="${order.shipCountry}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td>
Courier: </td><td><c:out value="${order.courier}"/>
</td></tr>
<tr bgcolor="#FFFF88"><td colspan="2">
  <b><font color="GREEN" size="4">Status:</font> <c:out value="${order.status}"/></b>
</td></tr>
<tr bgcolor="#FFFF88"><td colspan="2">
<table width="100%" align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="3">
  <tr bgcolor="#CCCCCC">
  <td><b>Item ID</b></td>
  <td><b>Description</b></td>
  <td><b>Quantity</b></td>
  <td><b>Price</b></td>
  <td><b>Total Cost</b></td>
  </tr>
<c:forEach var="lineItem" items="${order.lineItems}">
  <tr bgcolor="#FFFF88">
  <td><b><a href="<c:url value="/shop/viewItem.do"><c:param name="itemId" value="${lineItem.itemId}"/></c:url>">
    <font color="BLACK"><c:out value="${lineItem.itemId}"/></font>
  </a></b></td>
  <td>
    <c:out value="${lineItem.item.attribute1}"/>
    <c:out value="${lineItem.item.attribute2}"/>
    <c:out value="${lineItem.item.attribute3}"/>
    <c:out value="${lineItem.item.attribute4}"/>
    <c:out value="${lineItem.item.attribute5}"/>
    <c:out value="${lineItem.item.product.name}"/>
  </td>
  <td><c:out value="${lineItem.quantity}"/></td>
  <td align="right"><fmt:formatNumber value="${lineItem.unitPrice}" pattern="$#,##0.00"/></td>
  <td align="right"><fmt:formatNumber value="${lineItem.totalPrice}" pattern="$#,##0.00"/></td>
  </tr>
</c:forEach>
  <tr bgcolor="#FFFF88">
  <td colspan="5" align="right"><b>Total: <fmt:formatNumber value="${order.totalPrice}" pattern="$#,##0.00"/></b></td>
  </tr>
</table>
</td></tr>

</table>

<%@ include file="IncludeBottom.jsp" %>
