<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/attributePrivilegesPanel.jsp -->

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.privilegesSectionHeader" />

  <div class="sectionBody">
    <form id="attributeActionsFormId" name="attributePrivilegesFormName" onsubmit="return false;" >
    
	    <%-- signify which attribute def we are talking about --%>
	    <input type="hidden" name="attributeDefToEditId" 
	              value="${attributeUpdateRequestContainer.attributeDefToEdit.id}" />
      
      <table cellspacing="2" class="formTable">
        <c:set var="row" value="0" />
	      <c:forEach items="${attributeUpdateRequestContainer.privilegeSubjectContainers}" var="privilegeSubjectContainer">
          
          <c:if test="${attributeUpdateRequestContainer.showPrivilegeHeader[row]}">
	          <tr>
			        <c:forTokens var="privilegeName" items="attrView attrRead attrUpdate attrAdmin attrOptin attrOptout" delims=" ">
			          <th class="privilegeHeader"><grouper:message key="priv.${privilegeName}" /></grou></th>
			        </c:forTokens>
			        <th class="privilegeHeader" style="text-align: left">
			          &nbsp; &nbsp; <grouper:message key="simpleAttributeUpdate.entityHeader" /> 
	  			    </th>
				    </tr>
          </c:if>
          <tr>
            <c:forTokens var="privilegeName" items="attrView attrRead attrUpdate attrAdmin attrOptin attrOptout" delims=" ">
              <td>${row}<grouper:message key="priv.${privilegeName}" /></grou></td>
            </c:forTokens>
            <td>
              ${privilegeSubjectContainer.subject }
            </td>
          </tr>
          <c:set var="row" value="${row + 1}" />
		    </c:forEach>          
      </table> 
    </form>
    <br />
  </div>
</div>

<!-- End: simpleAttributeUpdate/attributePrivilegesPanel.jsp -->
