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
              <td class="privilegeRow">
                <input  style="margin-right: -3px"
                  type="checkbox" ${privilegeSubjectContainer.privilegeContainers[privilegeName].privilegeAssignType.immediate ? 'checked="checked"' : '' } 
                /><c:choose>
			            <c:when test="${privilegeSubjectContainer.privilegeContainers[privilegeName].privilegeAssignType.allowed}"
			            ><img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
			            /></c:when>
			            <c:otherwise><img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" /></c:otherwise>
			          </c:choose>
              </td>
            </c:forTokens>
            <td>
	            <c:set var="guiMember" value="${attributeUpdateRequestContainer.privilegeSubjectContainerGuiMembers[row]}" />
	            <%-- show an icon for the subject --%>
	            <grouper:subjectIcon guiSubject="${guiMember.guiSubject}" /> 
	            ${fn:escapeXml(guiMember.guiSubject.screenLabel)}
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
