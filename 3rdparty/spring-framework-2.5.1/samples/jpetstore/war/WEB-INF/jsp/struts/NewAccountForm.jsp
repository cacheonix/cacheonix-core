<%@ include file="IncludeTop.jsp" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<html:form action="/shop/newAccount.do" styleId="workingAccountForm" method="post" >
<html:hidden name="workingAccountForm" property="validate" value="newAccount"/>

<TABLE cellpadding=10 cellspacing=0 align=center border=1 bgcolor="#dddddd"><TR><TD>

<FONT color=darkgreen><H3>User Information</H3></FONT>
<TABLE bgcolor="#008800" border=0 cellpadding=3 cellspacing=1 bgcolor="#FFFF88">
<TR bgcolor="#FFFF88"><TD>
User ID:</TD><TD><html:text name="workingAccountForm" property="account.username" />
</TD></TR><TR bgcolor="#FFFF88"><TD>
New password:</TD><TD><html:password name="workingAccountForm" property="account.password"/>
</TD></TR><TR bgcolor="#FFFF88"><TD>
Repeat password:</TD><TD> <html:password name="workingAccountForm" property="repeatedPassword"/>
</TD></TR>
</TABLE>

<%@ include file="IncludeAccountFields.jsp" %>

</TABLE>

<BR><CENTER>
<input border=0 type="image" src="../images/button_submit.gif" />
</CENTER>

</html:form>

<%@ include file="IncludeBottom.jsp" %>