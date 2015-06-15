<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"    uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
  <head><title>Recipes</title></head>
  <body>
    <table>

    <c:forEach items="${recipeList}" var="recipe">
        <tr>
            <td>${recipe.id}</td>
            <td>${recipe.name}</td>
            <c:url value="/editrecipe.htm?id=${recipe.id}" var="editLink"/>
            <td><a href="${editLink}">[edit]</a></td>
        </tr>
    </c:forEach>
    </table>
  </body>
</html>