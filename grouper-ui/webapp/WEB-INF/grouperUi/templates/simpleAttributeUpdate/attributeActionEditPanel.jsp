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
            <grouper:message key="simpleAttributeUpdate.actionEdit" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          ${attributeUpdateRequestContainer.action}
        </td>
      </tr>
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleAttributeUpdate.editActionChange" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
        
          <select name="actionAddImply">
            <option value="" ></option>
            <c:forEach items="${attributeUpdateRequestContainer.newActionsCanImply}" var="newActionCanImply">
              <option>${newActionCanImply}</option>
            </c:forEach>
          </select>
        
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdate.addActionEditImplies?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${attributeUpdateRequestContainer.action}', {formIds: 'attributeActionEditFormId'}); return false;" 
          value="${simpleAttributeUpdateContainer.text.addActionEditImplies} ${attributeUpdateRequestContainer.action}" style="margin-top: 2px" />
        </td>
      </tr>
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
        </td>
        <td class="formTableRight" style="white-space: nowrap;">

          <select name="actionAddImpliedBy">
            <option value="" ></option>
            <c:forEach items="${attributeUpdateRequestContainer.newActionsCanImpliedBy}" var="newActionCanImpliedBy">
              <option>${newActionCanImpliedBy}</option>
            </c:forEach>
          </select>
        
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeUpdate.addActionEditImpliedBy?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${attributeUpdateRequestContainer.action}', {formIds: 'attributeActionEditFormId'}); return false;" 
          value="${simpleAttributeUpdateContainer.text.addActionEditImpliedBy} ${attributeUpdateRequestContainer.action}" style="margin-top: 2px" />
        
        </td>
      </tr>
                     
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleAttributeUpdate.actionsImply">
            <grouper:param>${attributeUpdateRequestContainer.action}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeUpdateRequestContainer.actionsThatImply}" var="actionThatImplies">
            
	            ${actionThatImplies}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleAttributeUpdate.immediateActionsImply">
            <grouper:param>${attributeUpdateRequestContainer.action}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeUpdateRequestContainer.actionsThatImplyImmediate}" var="actionThatImpliesImmediate">
            
            <a href="#" onclick="if (confirm('${simpleAttributeUpdateContainer.text.deleteActionImpliesConfirm}')) {ajax('SimpleAttributeUpdate.deleteActionImplies?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${attributeUpdateRequestContainer.action}&actionImplies=${actionThatImpliesImmediate}');} return false;" 
	            ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
	            alt="${simpleAttributeUpdateContainer.text.deleteActionImpliesImageAlt }"/></a>
            
            ${actionThatImpliesImmediate}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleAttributeUpdate.actionEdit" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          ${attributeUpdateRequestContainer.action}
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleAttributeUpdate.immediateActionsImpliedBy">
            <grouper:param>${attributeUpdateRequestContainer.action}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeUpdateRequestContainer.actionsImpliedByImmediate}" var="actionThatImpiedByImmediate">
            
            <a href="#" onclick="if (confirm('${simpleAttributeUpdateContainer.text.deleteActionImpliedByConfirm}')) {ajax('SimpleAttributeUpdate.deleteActionImpliedBy?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&action=${attributeUpdateRequestContainer.action}&actionImpliedBy=${actionThatImpiedByImmediate}');} return false;" 
              ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
              alt="${simpleAttributeUpdateContainer.text.deleteActionImpliedByImageAlt }"/></a>
            
            ${actionThatImpiedByImmediate}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleAttributeUpdate.actionsImpliedBy">
            <grouper:param>${attributeUpdateRequestContainer.action}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeUpdateRequestContainer.actionsImpliedBy}" var="actionImpliedBy">
            
              ${actionImpliedBy}
            
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
