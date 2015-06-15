<%@ include file="IncludeTop.jsp" %>

<table align="left" bgcolor="#008800" border="0" cellspacing="2" cellpadding="2">
<tr><td bgcolor="#FFFF88">
<a href="<c:url value="/shop/index.do"/>"><b><font color="BLACK" size="2">&lt;&lt; Main Menu</font></b></a>
</td></tr>
</table>

<table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="3">
  <tr bgcolor="#CCCCCC">  <td>&nbsp;</td>  <td><b>Product ID</b></td>  <td><b>Name</b></td>  </tr>
<c:forEach var="product" items="${productList.pageList}">
  <tr bgcolor="#FFFF88">
  <td><a href="<c:url value="/shop/viewProduct.do"><c:param name="productId" value="${product.productId}"/></c:url>">
  <c:out value="${product.description}" escapeXml="false"/></a></td>
  <td><b><a href="<c:url value="/shop/viewProduct.do"><c:param name="productId" value="${product.productId}"/></c:url>">
	  <font color="BLACK"><c:out value="${product.productId}"/></font>
  </a></b></td>
  <td><c:out value="${product.name}"/></td>
  </tr>
</c:forEach>
  <tr>
  <td>
  <c:if test="${!productList.firstPage}">
    <a href="?page=previous"><font color="white"><B>&lt;&lt; Prev</B></font></a>
  </c:if>
  <c:if test="${!productList.lastPage}">
    <a href="?page=next"><font color="white"><B>Next &gt;&gt;</B></font></a>
  </c:if>
  </td>
  </tr>

</table>

<%@ include file="IncludeBottom.jsp" %>
