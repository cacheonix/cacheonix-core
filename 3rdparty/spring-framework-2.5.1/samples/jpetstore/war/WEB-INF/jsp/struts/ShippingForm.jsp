<%@ include file="IncludeTop.jsp" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<html:form action="/shop/newOrder.do" styleId="workingOrderForm" method="post" >

<TABLE bgcolor="#008800" border=0 cellpadding=3 cellspacing=1 bgcolor="#FFFF88">
<TR bgcolor="#FFFF88"><TD colspan=2>
<FONT color=GREEN size=4><B>Shipping Address</B></FONT>
</TD></TR>

<TR bgcolor="#FFFF88"><TD>
First name:</TD><TD><html:text name="workingOrderForm" property="order.shipToFirstName" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Last name:</TD><TD><html:text name="workingOrderForm" property="order.shipToLastName" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Address 1:</TD><TD><html:text size="40" name="workingOrderForm" property="order.shipAddress1" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Address 2:</TD><TD><html:text size="40" name="workingOrderForm" property="order.shipAddress2" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
City: </TD><TD><html:text name="workingOrderForm" property="order.shipCity" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
State:</TD><TD><html:text size="4" name="workingOrderForm" property="order.shipState" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Zip:</TD><TD><html:text size="10" name="workingOrderForm" property="order.shipZip" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Country: </TD><TD><html:text size="15" name="workingOrderForm" property="order.shipCountry" />
</TD></TR>

</TABLE>
<P>
<input type="image" src="../images/button_submit.gif">

</html:form>

<%@ include file="IncludeBottom.jsp" %>