<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionLimitAddValue.jsp -->
<div class="simplePermissionLimitAddValue">

  <div class="section">
  
    <grouper:subtitle key="simplePermissionAssign.limitAddValueSubtitle" 
      infodotValue="${grouper:message('simplePermissionAssign.limitAddValueSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simplePermissionLimitAddValueForm" name="simplePermissionLimitAddValueFormName">
      
      <input name="limitAssignId" type="hidden" value="${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      
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
              <grouper:message key="simplePermissionUpdate.limitAddValue" />
            </td>
            <td class="formTableRight"><input type="text" name="valueToAdd"  id="valueToAddId" /></td>
          </tr>
                    
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simplePermissionAssign.limitAddValueCancelButton" /></button> 
              &nbsp;
              <%-- add member button --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimplePermissionUpdate.limitAddValueSubmit', {formIds: 'simplePermissionLimitAddValueForm, simplePermissionFilterForm'}); return false;" 
              value="${grouper:message('simplePermissionAssign.limitAddValueSubmitButton', true, false)}" />
            </td>
          </tr>
          
      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simplePermissionLimitAddValue.jsp -->


