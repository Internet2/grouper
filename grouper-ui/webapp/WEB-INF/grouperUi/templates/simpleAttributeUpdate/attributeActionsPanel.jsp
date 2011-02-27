<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/attributeActionsPanel.jsp -->

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.actionsSectionHeader" />

  <div class="sectionBody">
    <form id="attributeActionsFormId" name="attributeActionsFormName" onsubmit="return false;" >
    
    <%-- signify which attribute def we are talking about --%>
    <input type="hidden" name="attributeDefToEditId" 
              value="${attributeUpdateRequestContainer.attributeDefToEdit.id}" />
    <table class="formTable formTableSpaced" cellspacing="2">
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="changeActions">
            <grouper:message key="simpleAttributeUpdate.changeActions" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
        
          <input type="text" name="changeActions" style="width: 400px" />
          
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdate.newActions', {formIds: 'attributeActionsFormId'}); return false;" 
          value="${simpleAttributeUpdateContainer.text.addActions}" style="margin-top: 2px" />
          
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdate.replaceActions', {formIds: 'attributeActionsFormId'}); return false;" 
          value="${simpleAttributeUpdateContainer.text.replaceActions}" style="margin-top: 2px" />
          
        </td>
      </tr>
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top">
          <grouper:message key="simpleAttributeUpdate.actions" />
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeUpdateRequestContainer.actions}" var="action">
	          <a href="#" onclick="if (confirm('${simpleAttributeUpdateContainer.text.deleteActionConfirm}')) {ajax('SimpleAttributeUpdate.deleteAction?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${action}');} return false;" 
	          ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
	          alt="${simpleAttributeUpdateContainer.text.deleteActionImageAlt }"/></a>
            
            <%-- only show the edit button if this is a permission --%>
            <c:if test="${attributeUpdateRequestContainer.attributeDefToEdit.attributeDefTypeDb == 'perm'}">
              <a href="#" onclick="ajax('SimpleAttributeUpdate.editAction?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${action}'); return false;" 
              ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
              alt="${simpleAttributeUpdateContainer.text.editActionImageAlt }"/></a>
            </c:if>            
            
            
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

<div id="attributeActionEditPanel">
</div>

<!-- End: simpleAttributeUpdate/attributeActionsPanel.jsp -->
