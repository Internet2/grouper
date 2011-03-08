<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/attributePrivilegesPanel.jsp -->

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.privilegesSectionHeader" />

  <div class="sectionBody">
    <form id="attributeActionsFormId" name="attributePrivilegesFormName" onsubmit="return false;" >
    
	    <%-- signify which attribute def we are talking about --%>
	    <input type="hidden" name="attributeDefToEditId" 
	              value="${attributeUpdateRequestContainer.attributeDefToEdit.id}" />
      
      <table>
	      <c:forEach items="${attributeUpdateRequestContainer.privilegeSubjectContainers}" var="privilegeSubjectContainer">
          
          <tr>
		        <c:forTokens var="privilegeName" items="attrView attrRead attrUpdate attrAdmin attrOptin attrOptout" delims=" ">
		          <td><grouper:message key="priv.${privilegeName}" /></grou></td>
		        </c:forTokens>
		        <td>
  			      ${privilegeSubjectContainer.subject }
  			    </td>
			    </tr>
		    </c:forEach>          
      </table> 
    </form>
    <br />
  </div>
</div>

<!-- End: simpleAttributeUpdate/attributePrivilegesPanel.jsp -->
