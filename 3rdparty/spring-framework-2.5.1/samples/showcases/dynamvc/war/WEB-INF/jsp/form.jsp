<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form"   uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"    uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <title>Edit User: ${command.firstName} <c:out value="${command.lastName}" /></title>
    <link type="text/css" href="/css/base.css" />
</head>

<body>
<form:form>
    <form:errors path="*" cssClass="errorBox" />
    <table>
        <tr>
            <td>First Name:</td>
            <td><form:input path="firstName" /></td>
            <td><form:errors path="firstName" /></td>
        </tr>

        <tr>
            <td>Last Name:</td>
            <td><form:input path="lastName" /></td>
            <td><form:errors path="lastName"  /></td>
        </tr>

        <tr>
            <td>Country:</td>
            <td>
                <form:select path="country">
                    <form:option value="" label="--Please Select"/>
                    <form:options items="${countryList}" itemValue="code" itemLabel="name"/>
                </form:select>
            </td>
            <td></td>
        </tr>


        <tr>
            <td>Skills:</td>
            <td><form:select path="skills" items="${skills}"/></td>
            <td></td>
        </tr>
        <tr>
            <td>Notes:</td>
            <td><form:textarea path="notes" rows="3" cols="20" /></td>
            <td><form:errors path="notes" /></td>
        </tr>

        <tr>
            <td>Sex:</td>
            <td>Male: <form:radiobutton path="sex" value="M"/> <br/>
                Female: <form:radiobutton path="sex" value="F"/> </td>
            <td></td>
        </tr>

        <tr>
            <td>House:</td>
            <td>
                <form:select path="house">
                    <form:option value="Gryffindor"/>
                    <form:option value="Hufflepuff"/>
                    <form:option value="Ravenclaw"/>
                    <form:option value="Slytherin"/>
                </form:select>
            </td>
        </tr>

        <tr>
            <td>Subscribe to newsletter?:</td>
            <td><form:checkbox path="preferences.receiveNewsletter"/></td>
            <td></td>
        </tr>

        <tr>
            <td>Interests:</td>
            <td>
                Quidditch: <form:checkbox path="preferences.interests" value="Quidditch"/>
                Herbology: <form:checkbox path="preferences.interests" value="Herbology"/>
                Defence Against the Dark Arts: <form:checkbox path="preferences.interests" value="Defence Against the Dark Arts"/>
            </td>
            <td></td>
        </tr>

        <tr>
            <td>Favourite Word:</td>
            <td>
                Magic: <form:checkbox path="preferences.favouriteWord" value="Magic"/>
            </td>
            <td></td>
        </tr>

        <tr>
            <td>Favourite Colour:</td>
            <td>
                <form:select path="favouriteColour">
                    <form:option value="0" label="RED"/>
                    <form:option value="1" label="GREEN"/>
                    <form:option value="2" label="BLUE"/>
                </form:select>
            </td>
        </tr>

        <tr>
            <td>Password:</td>
            <td>
                <form:password path="password" />
            </td>
        </tr>

        <tr>
            <td>
                <form:hidden path="house" />
            </td>
        </tr>

        <tr>
            <td colspan="3">
                <input type="submit" value="Save Changes" />
            </td>
        </tr>

    </table>
</form:form>
</body>
</html>