<%@ include file="IncludeTop.jsp" %>


<table border="0" width="100%" cellspacing="0" cellpadding="0">
<tr><td valign="top" width="20%" align="left">
<table align="left" bgcolor="#008800" border="0" cellspacing="2" cellpadding="2">
<tr><td bgcolor="#FFFF88">
<a href="<c:url value="/shop/viewCart.do"/>"><b><font color="BLACK" size="2">&lt;&lt; Shopping Cart</font></b></a>
</td></tr>
</table>
</td>

<td valign="top" align="center">
<h2 align="center">Checkout Summary</h2>

<table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="5">

  <tr bgcolor="#cccccc">
  <td><b>Item ID</b></td>  <td><b>Product ID</b></td>  <td><b>Description</b></td> <td><b>In Stock?</b></td> <td><b>Quantity</b></td>  <td><b>List Price</b></td> <td><b>Total Cost</b></td>
  </tr>

<c:forEach var="cartItem" items="${cartForm.cart.cartItemList.pageList}">
  <tr bgcolor="#FFFF88">
  <td><b>
  <a href="<c:url value="/shop/viewItem.do"><c:param name="itemId" value="${cartItem.item.itemId}"/></c:url>">
    <c:out value="${cartItem.item.itemId}"/>
	</a></b></td>
  <td><c:out value="${cartItem.item.productId}"/></td>
  <td>
    <c:out value="${cartItem.item.attribute1}"/>
    <c:out value="${cartItem.item.attribute2}"/>
    <c:out value="${cartItem.item.attribute3}"/>
    <c:out value="${cartItem.item.attribute4}"/>
    <c:out value="${cartItem.item.attribute5}"/>
    <c:out value="${cartItem.item.product.name}"/>
   </td>
  <td align="center"><c:out value="${cartItem.inStock}"/></td>
  <td align="center">
  <c:out value="${cartItem.quantity}"/>
  </td>
  <td align="right"><fmt:formatNumber value="${cartItem.item.listPrice}" pattern="$#,##0.00" /></td>
  <td align="right"><fmt:formatNumber value="${cartItem.totalPrice}" pattern="$#,##0.00" /></td>
  </tr>
</c:forEach>
<tr bgcolor="#FFFF88">
<td colspan="7" align="right">
<b>Sub Total: <fmt:formatNumber value="${cartForm.cart.subTotal}" pattern="$#,##0.00" /></b><br />

</td>
</tr>
</table>
<center>
  <c:if test="${!cartForm.cart.cartItemList.firstPage}">
    <a href="checkout.do?page=previousCart"><font color="green"><B>&lt;&lt; Prev</B></font></a>
  </c:if>
  <c:if test="${!cartForm.cart.cartItemList.lastPage}">
    <a href="checkout.do?page=nextCart"><font color="green"><B>Next &gt;&gt;</B></font></a>
  </c:if>
</center>
<br>
<center><a href="<c:url value="/shop/newOrderForm.do"/>"><img border="0" src="../images/button_continue.gif" /></a></center>
</td>

<td valign="top" width="20%" align="right">&nbsp;</td>
</tr>
</table>

<%@ include file="IncludeBottom.jsp" %>
