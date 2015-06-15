<%@ include file="IncludeTop.jsp" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<html:form action="/shop/newOrder.do" styleId="workingOrderForm" method="post" >

<TABLE bgcolor="#008800" border=0 cellpadding=3 cellspacing=1 bgcolor="#FFFF88">
<TR bgcolor="#FFFF88"><TD colspan=2>
<FONT color=GREEN size=4><B>Payment Details</B></FONT>
</TD></TR><TR bgcolor="#FFFF88"><TD>
Card Type:</TD><TD>
<html:select name="workingOrderForm" property="order.cardType">
  <html:options name="workingOrderForm" property="creditCardTypes" />
</html:select>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Card Number:</TD><TD><html:text name="workingOrderForm" property="order.creditCard" /> <font color=red size=2>* Use a fake number!</font>
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Expiry Date (MM/YYYY):</TD><TD><html:text name="workingOrderForm" property="order.expiryDate" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD colspan=2>
<FONT color=GREEN size=4><B>Billing Address</B></FONT>
</TD></TR>

<TR bgcolor="#FFFF88"><TD>
First name:</TD><TD><html:text name="workingOrderForm" property="order.billToFirstName" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Last name:</TD><TD><html:text name="workingOrderForm" property="order.billToLastName" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Address 1:</TD><TD><html:text size="40" name="workingOrderForm" property="order.billAddress1" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Address 2:</TD><TD><html:text size="40" name="workingOrderForm" property="order.billAddress2" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
City: </TD><TD><html:text name="workingOrderForm" property="order.billCity" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
State:</TD><TD><html:text size="4" name="workingOrderForm" property="order.billState" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Zip:</TD><TD><html:text size="10" name="workingOrderForm" property="order.billZip" />
</TD></TR>
<TR bgcolor="#FFFF88"><TD>
Country: </TD><TD><html:text size="15" name="workingOrderForm" property="order.billCountry" />
</TD></TR>

<TR bgcolor="#FFFF88"><TD colspan=2>
<html:checkbox name="workingOrderForm" property="shippingAddressRequired" /> Ship to different address...
</TD></TR>

</TABLE>
<P>
<input type="image" src="../images/button_submit.gif">

</html:form>

<%@ include file="IncludeBottom.jsp" %>
