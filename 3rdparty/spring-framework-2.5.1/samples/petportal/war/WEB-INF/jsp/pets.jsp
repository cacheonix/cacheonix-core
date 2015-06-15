<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1>Pets</h1>

<table border="0" cellpadding="4">
   <tr>
      <th>Species</th>
      <th>Breed</th>
      <th>Name</th>
      <th/>
   </tr>
   <c:forEach items="${pets}" var="pet">
      <tr>
          <td><c:out value="${pet.species}"/></td>
          <td><c:out value="${pet.breed}"/></td>
          <td>
             <a href="<portlet:renderURL>
                         <portlet:param name="action" value="view"/>
                         <portlet:param name="pet">
                             <jsp:attribute name="value">
                                 <c:out value="${pet.key}"/>
                             </jsp:attribute>
                         </portlet:param>
                      </portlet:renderURL>">
                <c:out value="${pet.name}"/>
             </a>
          </td>
          <td>
             <a href="<portlet:actionURL>
                         <portlet:param name="action" value="delete"/>
                         <portlet:param name="pet">
                             <jsp:attribute name="value">
                                 <c:out value="${pet.key}"/>
                             </jsp:attribute>
                         </portlet:param>
                      </portlet:actionURL>">
                Remove
             </a>
           </td>                
       </tr>
    </c:forEach>
    
    <tr>
       <td colspan="4" align="right">
          <a href="<portlet:renderURL>
                      <portlet:param name="action" value="add"/>
                   </portlet:renderURL>">
             Add a Pet
          </a>
       </td>
    </tr>
		
</table>
<br/>

Switch to <a href="<portlet:renderURL portletMode="edit"/>">Edit Mode</a>
