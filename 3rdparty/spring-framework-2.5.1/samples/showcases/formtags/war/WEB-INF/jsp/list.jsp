<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"    uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <head>
    <title>Spring 2.0 form tag showcase application</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <link rel="icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />
    <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />

    <link href="<c:url value="/css/style.css"/>" rel="stylesheet" type="text/css"/>

  </head>

<body>
  <div id="main">

    <div id="topmenu">
      <a href="http://www.interface21.com/">Interface21</a><a href="http://www.springframework.org/">Spring home</a><a href="http://forum.springframework.org/">Spring forum</a><a href="http://www.infoq.com/">Infoq.com</a>
    </div>

    <div id="header">
      <span class="title">Spring<span class="darktitle">2.0</span> form tags</span><br/>
    </div>


    <div id="content">
       <div id="block">
           <p>Welcome to this Spring 2.0 showcase application that demonstrates
           the use of new JSP tags for creating form elements.</p>

           <p>Choose an apprentice magician on the left-hand side to continue.</p>


      </div>

    </div>

  <div class="lefty">
    <div class="menu">
      <c:forEach items="${userList}" var="user">
        <a href="form.htm?id=<c:out value="${user.id}"/>"><c:out value="${user.lastName}"/>, <c:out value="${user.firstName}"/></a>
      </c:forEach>

    </div>
    <p>New JSP tags in Spring 2.0 make building forms with Spring MVC much easier</p>
    <div class="menu">
       <a href="<c:url value="/about.htm"/>">About</a>
    </div>
  </div>

</div>

</body>
</html>
