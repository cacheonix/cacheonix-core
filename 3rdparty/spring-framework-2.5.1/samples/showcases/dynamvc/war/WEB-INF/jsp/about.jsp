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
      <a href="<c:url value='/home.htm'/>">Home</a>

     </p>
	</dd>
 </dl>

 <div id="content">
     <h1>Overview of this sample application</h1>
     <p class="paragraph">
This small application showcases implementing Spring MVC Controllers
using the dynamic language support introduced in Spring 2.0.</p>


<p class="paragraph">The web application is *very* simplistic, because the intent is
to convey the basics of the dynamic language support as applied to
Spring MVC and pretty much nothing else.</p>

<p class="paragraph">There is one Groovy file in the application. It is called
<code>FortuneController.groovy</code> and it is located in the <code>war/WEB-INF/groovy</code>
folder. This Groovy script file is referenced by the 'fortune'
bean in the <code>war/WEB-INF/fortune-servlet.xml</code> Spring MVC configuration file.</p>

<p class="paragraph">You will notice that the <code>fortune</code> bean is set as refreshable via the use
of the <code>refresh-check-delay</code> attribute on the <code>&lt;lang:groovy/&gt;</code> element. The
value of this attribute is set to <code>3000</code> which means that changes to the
<code>FortuneController.groovy</code> file will be picked up after a delay of 3 seconds.</p>

<p class="paragraph">If you deploy the application to Tomcat (for example), you can then go into
the exploded <code>/WEB-INF/groovy</code> folder and edit the <code>FortuneController.groovy</code>
file directly. Any such changes that you make will be automatically picked up
and the <code>fortune</code> bean will be reconfigured... all without having to stop,
redeply and restart the application. Try it yourself... now admittedly
there is not a lot of complex logic in the <code>FortuneController.groovy</code> file
(which is good because <code>Controllers</code> in Spring MVC should be as thin as possible).</p>

<p class="paragraph">You could try returning a default Fortune instead of delegating to the injected
<code>FortuneService</code>, or you could return a different logical view name, or
(if you are feeling more ambitious) you could try creating a custom
Groovy implementation of the <code>FortuneService</code> interface and try plugging that into
the web application.</p>

<p class="paragraph">Perhaps your custom Groovy <code>FortuneService</code> could access
a web service to get some <code>Fortunes</code>, or apply some different randomizing logic
to the returned <code>Fortune</code>, or whatever. The key point is that you will be able to make
these changes without having to redeploy (or bounce) your application. This is
a great boon with regard to rapid prototyping.</p> </div>

 <dl id="footer">
	<dd>If you like these cookies you may also like <a href="http://www.youtube.com/watch?search=ice+age&v=ZYzJybb3_Ew">this</a>.</dd>
 </dl>

</div>

</body>
</html>
