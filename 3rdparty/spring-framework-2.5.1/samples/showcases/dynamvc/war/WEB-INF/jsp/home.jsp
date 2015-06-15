<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"    uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
 <title>Spring 2.0 dynamic language support showcase application</title>
 <link rel="stylesheet" type="text/css" href="<c:url value="/css/style.css"/>" title="style" />
 <link rel="icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />
 <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />
</head>
<body>

<div id="body">

<dl id="header"><dd><p><b>Fortune</b>cookies</p></dd></dl>

 <dl id="menu">
	<dd>
	 <p>
	  <a href="http://www.interface21.com">Interface21</a> -
	  <a href="http://www.springframework.org">Spring home</a> -
	  <a href="http://forum.springframework.org">Spring Forum</a> -
	  <a href="http://www.dzone.com">Dzone.com</a> -
	  <a href="http://www.infoq.com">Infoq.com</a> -
      <a href="<c:url value='/about.htm'/>">About</a>

     </p>
	</dd>
 </dl>

 <div id="content">
	<h1>Welcome to Madame Penc's Fortune Telling Emporium!</h1>
	<p><a href="<c:url value='/fortune.htm'/>">Get your fortune!</a></p>
 </div>

 <dl id="footer">
	<dd>If you like these cookies you may also like <a href="http://www.youtube.com/watch?search=ice+age&v=ZYzJybb3_Ew">this</a>.</dd>
 </dl>

</div>

</body>
</html>
