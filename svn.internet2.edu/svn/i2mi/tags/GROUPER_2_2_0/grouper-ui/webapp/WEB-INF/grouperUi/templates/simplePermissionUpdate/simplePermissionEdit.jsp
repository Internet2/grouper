<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionEdit.jsp -->
<div class="simplePermissionEdit">

  <div class="section">
  
    <grouper:subtitle key="simplePermissionAssign.assignEditSubtitle" 
      infodotValue="${grouper:message('simplePermissionAssign.assignEditSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">
    <form id="simplePermissionEditForm" name="simplePermissionEditFormName">

      <c:set var="guiPermissionEntry" value="${permissionUpdateRequestContainer.guiPermissionEntry}"/>
      <c:set var="permissionEntry" value="${guiPermissionEntry.permissionEntry}"/>

      <%-- signify stash the type of assignment --%>
      <input type="hidden" name="permissionAssignType" 
                value="${permissionUpdateRequestContainer.permissionType.name}" />

      <input type="hidden" name="permissionAssignmentId" 
                value="${permissionEntry.attributeAssignId}" />

      <table class="formTable formTableSpaced">
      
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="permissionUpdateRequestContainer.permissionType" />
              
            </td>
            <td class="formTableRight">
              <grouper:message 
                 key="permissionUpdateRequestContainer.permissionType.${permissionUpdateRequestContainer.permissionType.name}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderRoleName" />
            </td>
            <td class="formTableRight">
              <grouper:message valueTooltip="${grouper:escapeHtml(permissionEntry.role.displayName)}" 
                value="${grouper:escapeHtml(permissionEntry.role.displayExtension)}"  />
            </td>
          </tr>
          <c:choose>
            <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
              <tr class="formTableRow">
                <td class="formTableLeft" style="white-space: nowrap;">
                  <grouper:message key="simplePermissionUpdate.assignHeaderEntity" />
                </td>
                <td class="formTableRight">
                  <grouper:message valueTooltip="${grouper:escapeHtml(guiPermissionEntry.screenLabelLongIfDifferent)}" 
                     value="${grouper:escapeHtml(guiPermissionEntry.screenLabelShort)}"  />
                </td>
              </tr>
            </c:when>
          </c:choose>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderResource" />
            </td>
            <td class="formTableRight">
              <grouper:message valueTooltip="${grouper:escapeHtml(permissionEntry.attributeDefName.displayName)}" 
                 value="${grouper:escapeHtml(permissionEntry.attributeDefName.displayExtension)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderAllowed" />
            </td>
            <td class="formTableRight">
              <c:choose>
                <c:when test="${permissionEntry.disallowed}">
                  <img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" 
                    onmouseover="Tip('${grouper:escapeJavascript(navMap['simplePermissionAssign.assignAllowedDisallow'])}')" 
                    onmouseout="UnTip()"
                  />                    
                </c:when>
                <c:otherwise>
                  <img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
                    onmouseover="Tip('${grouper:escapeJavascript(navMap['simplePermissionAssign.assignAllowedAllow'])}')" 
                    onmouseout="UnTip()"
                    />
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderDefinition" />
            </td>
            <td class="formTableRight">
              <grouper:message valueTooltip="${grouper:escapeHtml(permissionEntry.attributeDef.name)}" 
                 value="${grouper:escapeHtml(permissionEntry.attributeDef.extension)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionAssign.assignEditId" />
            </td>
            <td class="formTableRight">
              ${permissionEntry.attributeAssignId}
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignEditEnabledDate" />
            </td>
            <td class="formTableRight"><input type="text" name="enabledDate"  id="enabledDateId"
            value="${guiPermissionEntry.enabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simplePermissionUpdate.assignEditEnabledDisabledDateMask" /></span>
            </td>
          </tr>
          
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignEditDisabledDate" />
            </td>
            <td class="formTableRight"><input type="text" name="disabledDate"  id="disabledDateId"
            value="${guiPermissionEntry.disabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simplePermissionUpdate.assignEditEnabledDisabledDateMask" /></span>
            </td>
          </tr>
          
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simplePermissionAssign.assignEditCancelButton" /></button> 
              &nbsp;
              <%-- edit assignment button  --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimplePermissionUpdate.assignEditSubmit', {formIds: 'simplePermissionEditForm, simplePermissionFilterForm'}); return false;" 
              value="${grouper:message('simplePermissionAssign.assignEditSubmitButton', true, false)}" />
            </td>
          </tr>
      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simpleAttributeAssignEdit.jsp -->


