<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionLimitValueEdit.jsp -->
<div class="simplePermissionLimitEditValue">

  <div class="section">
  
    <grouper:subtitle key="simplePermissionUpdate.limitValueEditSubtitle" 
      infodotValue="${grouper:message('simplePermissionUpdate.limitValueEditSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simplePermissionLimitValueEditForm" name="simplePermissionLimitValueEditFormName">
      <input name="limitAssignId" type="hidden" value="${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      <input name="limitAssignValueId" type="hidden" value="${permissionUpdateRequestContainer.attributeAssignValue.id }" />
      <table class="formTable formTableSpaced">
      
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="${permissionUpdateRequestContainer.attributeAssignTypeLabelKey}" />
            </td>
            <td class="formTableRight" style="white-space: nowrap;">
              <grouper:message 
                 value="${grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderPermissionName" />
            </td>
            <td class="formTableRight" style="white-space: nowrap;">
              <grouper:message value="${grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeDefName.displayName)}" />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionAssign.limitAction" />
            </td>
            <td class="formTableRight">
              ${permissionUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignAction.name}
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionAssign.limitName" />
            </td>
            <td class="formTableRight" style="white-space: nowrap;">
              ${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.attributeDefName.displayName}
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionAssign.limitEditId" />
            </td>
            <td class="formTableRight">
              ${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id}
            </td>
          </tr>

          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simpleAttributeUpdate.assignValueEdit" />
            </td>
            <td class="formTableRight"><input type="text" name="valueToEdit"  id="valueToEditId" 
              value="${permissionUpdateRequestContainer.attributeAssignValue.valueFriendly}" /></td>
          </tr>

          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simplePermissionUpdate.limitValueEditCancelButton" /></button> 
              &nbsp;
              <%-- add member button --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimplePermissionUpdate.limitValueEditSubmit', {formIds: 'simplePermissionLimitValueEditForm, simplePermissionFilterForm'}); return false;" 
              value="${grouper:message('simplePermissionUpdate.limitValueEditSubmitButton', true, false)}" />
            </td>
          </tr>
          
      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simplePermissionLimitValueEdit.jsp -->


