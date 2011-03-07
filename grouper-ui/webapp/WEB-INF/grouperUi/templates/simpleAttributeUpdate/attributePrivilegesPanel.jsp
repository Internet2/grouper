<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/attributePrivilegesPanel.jsp -->

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.privilegesSectionHeader" />

  <div class="sectionBody">
    <form id="attributeActionsFormId" name="attributePrivilegesFormName" onsubmit="return false;" >
    
	    <%-- signify which attribute def we are talking about --%>
	    <input type="hidden" name="attributeDefToEditId" 
	              value="${attributeUpdateRequestContainer.attributeDefToEdit.id}" />

      <c:forEach items="${attributeUpdateRequestContainer.privilegeSubjectContainers}" var="privilegeSubjectContainer">
	       ${privilegeSubjectContainer.subject }
	    </c:forEach>          
    </form>
    <br />
  </div>
</div>

<!-- End: simpleAttributeUpdate/attributePrivilegesPanel.jsp -->
