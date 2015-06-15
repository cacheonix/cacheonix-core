<%@ page session="false" %>
<%@ page import="java.util.List,
                 java.util.Iterator,
                 org.springframework.samples.imagedb.ImageDescriptor" %>

<!-- imageList.jsp -->

<html>
<body>

<%
List images = (List) request.getAttribute("images");
for (Iterator it = images.iterator(); it.hasNext();) {
ImageDescriptor image = (ImageDescriptor) it.next();
%>
<table border="1" cellspacing="0" cellpadding="5">
  <tr><td width="10%">Name</td><td><%= image.getName() %>&nbsp;</td></tr>
	<tr><td colspan="2"><img src="imageContent?name=<%= image.getName() %>" height="100"></td></tr>
	<tr><td>Description (<%= image.getDescriptionLength() %>)</td><td><%= image.getShortDescription() %>&nbsp;</td></tr>
</table>
<p>
<%
}
%>

<p>
<table border="1" cellspacing="0" cellpadding="5">
<form action="imageUpload" method="post" encType="multipart/form-data">
  <tr><td width="10%">Name</td><td><input type="text" name="name"><br></td></tr>
  <tr><td>Content</td><td><input type="file" name="image"><br></td></tr>
  <tr><td>Description</td><td><textarea name="description" cols="80" rows="5"></textarea></td></tr>
  <tr><td colspan="2"><input type="submit" value="Upload image"></td></tr>
</form>
</table>

<p><a href="clearDatabase">Clear database</a>

</body>
</html>
