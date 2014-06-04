<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionLimitEdit.jsp -->
<div class="simplePermissionLimitEdit">

  <div class="section">
  
    <grouper:subtitle key="simplePermissionAssign.limitEditSubtitle" 
      infodotValue="${grouper:message('simplePermissionAssign.limitEditSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simplePermissionLimitEditForm" name="simplePermissionLimitEditFormName">
      <input name="limitAssignId" type="hidden" value="${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      <table class="formTable formTableSpaced">

          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="${permissionUpdateRequestContainer.attributeAssignTypeLabelKey}" />
            </td>
            <td class="formTableRight">
              <grouper:message 
                 value="${grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderPermissionName" />
            </td>
            <td class="formTableRight">
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
            <td class="formTableRight">
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
              <grouper:message key="simplePermissionUpdate.limitEditEnabledDate" />
            </td>
            <td class="formTableRight"><input type="text" name="enabledDate"  id="enabledDateId"
            value="${permissionUpdateRequestContainer.guiAttributeAssignAssign == null ? permissionUpdateRequestContainer.guiAttributeAssign.enabledDate : permissionUpdateRequestContainer.guiAttributeAssignAssign.enabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simpleAttributeUpdate.assignEditEnabledDisabledDateMask" /></span>
            </td>
          </tr>

          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.limitEditDisabledDate" />
            </td>
            <td class="formTableRight"><input type="text" name="disabledDate"  id="disabledDateId"
            value="${permissionUpdateRequestContainer.guiAttributeAssignAssign == null ? permissionUpdateRequestContainer.guiAttributeAssign.disabledDate : permissionUpdateRequestContainer.guiAttributeAssignAssign.disabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simpleAttributeUpdate.assignEditEnabledDisabledDateMask" /></span>
            </td>
          </tr>

          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simplePermissionAssign.limitEditCancelButton" /></button> 
              &nbsp;
              <%-- edit assignment button --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimplePermissionUpdate.assignLimitEditSubmit', {formIds: 'simplePermissionLimitEditForm, simplePermissionFilterForm'}); return false;" 
              value="${grouper:message('simplePermissionAssign.limitEditSubmitButton', true, false)}" />
            </td>
          </tr>

      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simplePermissionLimitEdit.jsp -->