
<c:if test="${!empty accountForm.myList}">
<p>&nbsp;</p>
<table align="right" bgcolor="#008800" border="0" cellspacing="2" cellpadding="3">
  <tr bgcolor="#CCCCCC"><td>
<font size="4"><b>Pet Favorites</b></font>
<font size="2"><i><br />Shop for more of your <br />favorite pets here.</i></font>
  </td></tr>
  <tr bgcolor="#FFFF88">
  <td>
  <c:forEach var="product" items="${accountForm.myList.pageList}" >
    <a href="<c:url value="/shop/viewProduct.do"><c:param name="productId" value="${product.productId}"/></c:url>">
      <c:out value="${product.name}"/>
    </a>
    <br/>
    <font size="2">(<c:out value="${product.productId}"/>)</font>
    <br/>
  </c:forEach>
  </td>
  </tr>
  <tr>
  <td>
  <c:if test="${!accountForm.myList.firstPage}">
    <a href="viewCart.do?page=previous"><font color="white"><B>&lt;&lt; Prev</B></font></a>
  </c:if>
  <c:if test="${!accountForm.myList.lastPage}">
    <a href="viewCart.do?page=next"><font color="white"><B>Next &gt;&gt;</B></font></a>
  </c:if>
  </td>
  </tr>

</table>
</c:if>
