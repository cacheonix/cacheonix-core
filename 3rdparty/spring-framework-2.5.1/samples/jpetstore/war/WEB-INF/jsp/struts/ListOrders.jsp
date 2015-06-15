<%@ include file="IncludeTop.jsp" %>

<center>
  <font size="4"><b>My Orders</b></font>
</center>
<table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="3">
  <tr bgcolor="#CCCCCC">  <td><b>Order ID</b></td>  <td><b>Date</b></td>  <td><b>Total Price</b></td>  </tr>
<c:forEach var="order" items="${orderList}">
  <tr bgcolor="#FFFF88">
  <td><b><a href="<c:url value="/shop/viewOrder.do"><c:param name="orderId" value="${order.orderId}"/></c:url>">
	  <font color="BLACK"><c:out value="${order.orderId}"/></font>
  </a></b></td>
  <td><fmt:formatDate value="${order.orderDate}" pattern="yyyy/MM/dd hh:mm:ss"/></td>
  <td><fmt:formatNumber value="${order.totalPrice}" pattern="$#,##0.00"/></td>
  </tr>
</c:forEach>
</table>

<%@ include file="IncludeBottom.jsp" %>
