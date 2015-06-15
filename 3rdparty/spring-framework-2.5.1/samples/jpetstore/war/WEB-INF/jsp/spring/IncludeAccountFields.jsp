<FONT color=darkgreen><H3>Account Information</H3></FONT>

<TABLE bgcolor="#008800" border=0 cellpadding=3 cellspacing=1 bgcolor="#FFFF88">
<TR bgcolor="#FFFF88"><TD>
First name:</TD><TD>
  <spring:bind path="accountForm.account.firstName">
	  <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Last name:</TD><TD>
  <spring:bind path="accountForm.account.lastName">
	  <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Email:</TD><TD>
  <spring:bind path="accountForm.account.email">
	  <input type="text" size="40" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Phone:</TD><TD>
  <spring:bind path="accountForm.account.phone">
	  <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Address 1:</TD><TD>
  <spring:bind path="accountForm.account.address1">
	  <input type="text" size="40" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Address 2:</TD><TD>
  <spring:bind path="accountForm.account.address2">
	  <input type="text" size="40" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
City: </TD><TD>
  <spring:bind path="accountForm.account.city">
	  <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
State:</TD><TD>
  <spring:bind path="accountForm.account.state">
	  <input type="text" size="4" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Zip:</TD><TD>
  <spring:bind path="accountForm.account.zip">
	  <input type="text" size="10" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Country: </TD><TD>
  <spring:bind path="accountForm.account.country">
	  <input type="text" size="15" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
  </spring:bind>
</TD></TR>
</TABLE>

<FONT color=darkgreen><H3>Profile Information</H3></FONT>

<TABLE bgcolor="#008800" border=0 cellpadding=3 cellspacing=1 >
<TR bgcolor="#FFFF88"><TD>
Language Preference:</TD><TD>
  <spring:bind path="accountForm.account.languagePreference">
    <select name="<c:out value="${status.expression}"/>">
      <c:forEach var="language" items="${languages}">
        <option <c:if test="${language == status.value}">selected</c:if> value="<c:out value="${language}"/>">
        <c:out value="${language}"/></option>
	    </c:forEach>
    </select>
  </spring:bind>
</TD></TR><TR bgcolor="#FFFF88"><TD>
Favourite Category:</TD><TD>
  <spring:bind path="accountForm.account.favouriteCategoryId">
    <select name="<c:out value="${status.expression}"/>">
      <c:forEach var="category" items="${categories}">
        <option <c:if test="${category.categoryId == status.value}">selected</c:if> value="<c:out value="${category}"/>">
        <c:out value="${category.name}"/></option>
	    </c:forEach>
    </select>
  </spring:bind>
</TD></TR><TR bgcolor="#FFFF88"><TD colspan=2>
  <spring:bind path="accountForm.account.listOption">
    <input type="checkbox" name="<c:out value="${status.expression}"/>" value="true" <c:if test="${status.value}">checked</c:if>>
		Enable MyList
  </spring:bind>

</TD></TR><TR bgcolor="#FFFF88"><TD colspan=2>
  <spring:bind path="accountForm.account.bannerOption">
    <input type="checkbox" name="<c:out value="${status.expression}"/>" value="true" <c:if test="${status.value}">checked</c:if>>
		Enable MyBanner
  </spring:bind>
</TD></TR>
</TABLE>
