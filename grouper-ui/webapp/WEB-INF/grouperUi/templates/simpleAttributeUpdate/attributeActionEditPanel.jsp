<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/attributeActionEditPanel.jsp -->

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.editActionsSectionHeader" />

  <div class="sectionBody">
    <form id="attributeActionEditFormId" name="attributeActionEditFormName" onsubmit="return false;" >
    
    <%-- signify which attribute def we are talking about --%>
    <input type="hidden" name="attributeDefToEditId" 
              value="${attributeUpdateRequestContainer.attributeDefToEdit.id}" />
    <input type="hidden" name="action" 
              value="${attributeUpdateRequestContainer.action}" />
    <table class="formTable formTableSpaced" cellspacing="2">
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleAttributeUpdate.editActionChange" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
        
          <input type="text" name="editActionChange" width="20" />
          
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdate.addActionEditImplies?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${attributeUpdateRequestContainer.action}', {formIds: 'attributeActionEditFormId'}); return false;" 
          value="${simpleAttributeUpdateContainer.text.addActionEditImplies} ${action}" style="margin-top: 2px" />
          
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdate.addActionEditImpliedBy?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${attributeUpdateRequestContainer.action}', {formIds: 'attributeActionEditFormId'}); return false;" 
          value="${simpleAttributeUpdateContainer.text.addActionEditImpliedBy} ${action}" style="margin-top: 2px" />
          
        </td>
      </tr>
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top">
          <grouper:message key="simpleAttributeUpdate.actionsImplies" />
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeUpdateRequestContainer.actions}" var="action">
	          <a href="#" onclick="if (confirm('${simpleAttributeUpdateContainer.text.deleteActionConfirm}')) {ajax('SimpleAttributeUpdate.deleteAction?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${action}');} return false;" 
	          ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
	          alt="${simpleAttributeUpdateContainer.text.deleteActionImageAlt }"/></a>
            
            <a href="#" onclick="ajax('SimpleAttributeUpdate.editAction?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${action}'); return false;" 
            ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
            alt="${simpleAttributeUpdateContainer.text.editActionImageAlt }"/></a>
            
            
            
	            ${action}
            
            <br />
          </c:forEach>
        </td>
      </tr>
    </table>
    </form>
    <br />
  </div>
</div>

<!-- End: simpleAttributeUpdate/attributeActionEditPanel.jsp -->
