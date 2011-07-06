<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionUpdate/simplePermissionAssignmentPanel.jsp -->

<%-- the success message will go here --%>
<div id="permissionAddLimitMessageId"></div>

<div class="section" style="min-width: 900px" id="permissionAddLimitPanel">

  <grouper:subtitle key="simplePermissionUpdate.addLimitPanelSubtitle" 
    infodotValue="${grouper:message('simplePermissionUpdate.addLimitPanelSubtitleInfodot', true, false)}" />

  <div class="sectionBody">
    <form id="attributePermissionAddLimitFormId" name="attributePermissionAddLimitFormName" onsubmit="return false;" >

      <input type="hidden" name="permissionAssignType" 
                value="${permissionUpdateRequestContainer.permissionType.name}" />
      <input name="attributeAssignId" type="hidden" 
                value="${permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.attributeAssignId }" />

      <table class="formTable formTableSpaced" cellspacing="2" style="margin-top: 0px; margin-bottom: 0px">

        <tr class="formTableRow">
          <td class="formTableLeft" style="white-space: nowrap;">
            <grouper:message key="simplePermissionUpdate.addLimitRole" />
          </td>
          <td class="formTableRight">
            <grouper:message valueTooltip="${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.role.displayName)}" 
               value="${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.role.displayExtension)}"  />
          
          </td>
        </tr>
        <c:choose>
          <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
            <tr class="formTableRow">
              <td class="formTableLeft" style="white-space: nowrap;">
                <grouper:message key="simplePermissionUpdate.addLimitSubject" />
              </td>
              <td class="formTableRight">
                <grouper:message valueTooltip="${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.screenLabelLongIfDifferent)}" 
                   value="${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.screenLabelShort)}"  />
              
              </td>
            </tr>
  
  
          </c:when>
        </c:choose>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="attributeDefinition">
              <grouper:message key="simplePermissionUpdate.addLimitPermissionName" />
            </label>
          </td>
          <td class="formTableRight">
            <grouper:message valueTooltip="${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.attributeDefName.displayName)}" 
               value="${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.attributeDefName.displayExtension)}"  />
          </td>
        </tr>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="attributeDefinition">
              <grouper:message key="simplePermissionUpdate.addLimitPermissionAction" />
            </label>
          </td>
          <td class="formTableRight">
            <grouper:message value="${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.action)}"  />
          </td>
        </tr>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="permissionResource">
              <grouper:message key="simplePermissionAssign.permissionResource" />
            </label>
            <sup class="requiredIndicator">*</sup>
          </td>
          <td class="formTableRight">
             <grouper:combobox comboDefaultText="${permissionUpdateRequestContainer.defaultAttributeNameDisplayName}" 
               comboDefaultValue="${permissionUpdateRequestContainer.defaultAttributeNameId}"
               filterOperation="SimplePermissionUpdateFilter.filterPermissionResources" 
               id="permissionAddAssignAttributeName" additionalFormElementNames="permissionAddAssignAttributeDef"
               width="700"/>
          </td>
        </tr>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="group">
              <grouper:message key="simplePermissionAssign.assignRole" />
            </label>
            <sup class="requiredIndicator">*</sup>
          </td>
          <td class="formTableRight">
             <grouper:combobox comboDefaultText="${permissionUpdateRequestContainer.defaultRoleDisplayName}" 
               comboDefaultValue="${permissionUpdateRequestContainer.defaultRoleId}"
               filterOperation="SimplePermissionUpdateFilter.filterRoles" 
               id="permissionAddAssignRoleId" 
               width="700"/>
          </td>
        </tr>
        <c:choose>
          <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
            <tr class="formTableRow">
              <td class="formTableLeft" style="vertical-align: middle">
                <label for="member">
                  <grouper:message key="simplePermissionAssign.assignMember" />
                </label>
                <sup class="requiredIndicator">*</sup>
              </td>
              <td class="formTableRight">
                 <grouper:combobox 
                   filterOperation="SimplePermissionUpdateFilter.filterSubjects" 
                   id="permissionAddAssignMemberId" additionalFormElementNames="permissionAddAssignRoleId"
                   width="700"/>
              </td>
            </tr>
          </c:when>
        </c:choose>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="member">
              <grouper:message key="simplePermissionAssign.assignAction" />
            </label>
            <sup class="requiredIndicator">*</sup>
          </td>
          <td class="formTableRight">
             <grouper:combobox  comboDefaultText="${permissionUpdateRequestContainer.defaultAction}" comboDefaultValue="${permissionUpdateRequestContainer.defaultAction}"
               filterOperation="SimplePermissionUpdateFilter.filterActions" 
               id="permissionAddAssignAction" 
               additionalFormElementNames="permissionAddAssignAttributeDef,permissionAddAssignAttributeName"
               width="700"/>
          </td>
        </tr>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="member">
              <grouper:message key="simplePermissionAssign.assignAllowedLabel" />
            </label>
            <sup class="requiredIndicator">*</sup>
          </td>
          <td class="formTableRight">
           <input type="radio" name="permissionAddAllowed" value="allow" checked="checked" } /><grouper:message key="simplePermissionAssign.assignAllowedAllow" />
           &nbsp;
           <input type="radio" name="permissionAddAllowed" value="disallow" /><grouper:message key="simplePermissionAssign.assignAllowedDisallow" />
          </td>
        </tr>
        <tr>
         <td colspan="2">
      
           <input class="blueButton" type="submit" 
            onclick="ajax('../app/SimplePermissionUpdate.assignPermissionCancelButton'); return false;" 
            value="${grouper:message('simplePermissionAssign.assignPermissionCancelButton', true, false) }" style="margin-top: 2px" />
         
           <input class="blueButton" type="submit" 
            onclick="ajax('../app/SimplePermissionUpdate.assignPermission', {formIds: 'simplePermissionFilterForm, attributePermissionAssignmentFormId'}); return false;" 
            value="${grouper:message('simplePermissionAssign.assignPermissionSubmitButton', true, false) }" style="margin-top: 2px" />
         
         </td>
        </tr>
      </table>

    </form>
  </div>
</div>

<!-- End: simplePermissionUpdate/simplePermissionAssignmentPanel.jsp -->
