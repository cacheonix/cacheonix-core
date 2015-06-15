<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1>Pet Info for: <c:out value="${pet.name}"/></h1>

<ul>
   <li>Species: <c:out value="${pet.species}"/></li>
   <li>Breed: <c:out value="${pet.breed}"/></li>
   <li>Birthdate: <c:out value="${pet.birthdate}"/></li>
   <li>Description: <c:out value="${pet.description}"/></li>
</ul>

<br/>

<a href="<portlet:renderURL>
            <portlet:param name="action" value="listPets"/>
         </portlet:renderURL>">
    List All Pets
</a>

<br/>
