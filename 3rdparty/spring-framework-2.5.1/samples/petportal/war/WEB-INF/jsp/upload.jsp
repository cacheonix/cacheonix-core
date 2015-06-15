<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1>Upload a Pet Description Here</h1>

<p> 
  Please upload a small text file (under 2K) and the contents 
  will be added as a description for the pet you choose.
</p>

<form method="post" action="<portlet:actionURL/>" enctype="multipart/form-data">
	<select name="selectedPet">
		<c:forEach items="${pets}" var="pet">
			<option value="<c:out value="${pet.key}"/>">
	    		<c:out value="${pet.name} (${pet.species}/${pet.breed})"/>
			</option>
		</c:forEach>
	</select>
	<br/>
	<input type="file" name="file"/>
	<br/>
	<button type="submit">Upload</button>
</form>