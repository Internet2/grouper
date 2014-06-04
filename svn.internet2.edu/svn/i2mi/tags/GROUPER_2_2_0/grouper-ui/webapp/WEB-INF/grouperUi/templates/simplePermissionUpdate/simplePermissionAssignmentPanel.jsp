<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionUpdate/simplePermissionAssignmentPanel.jsp -->

<%-- the success message will go here --%>
<div id="permissionAssignMessageId"></div>

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simplePermissionUpdate.assignAssignmentPanelSubtitle" />

  <div class="sectionBody">
    <form id="attributePermissionAssignmentFormId" name="attributePermissionAssignmentFormName" onsubmit="return false;" >

      <input type="hidden" name="permissionAssignType" 
                value="${permissionUpdateRequestContainer.permissionType.name}" />

      <table class="formTable formTableSpaced" cellspacing="2" style="margin-top: 0px; margin-bottom: 0px">
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle; white-space: nowrap;">
            <label for="attributeDefinition">
              <grouper:message key="simplePermissionAssign.attributeDefinition" />
            </label>
          </td>
          <td class="formTableRight">
             <grouper:combobox  comboDefaultText="${permissionUpdateRequestContainer.defaultAttributeDefDisplayName}" 
               comboDefaultValue="${permissionUpdateRequestContainer.defaultAttributeDefId}"
               filterOperation="SimplePermissionUpdateFilter.filterPermissionAttributeDefs" 
               id="permissionAddAssignAttributeDef"  
               width="700"/>
          </td>
        </tr>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle; white-space: nowrap;">
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
                 <grouper:combobox comboDefaultText="${permissionUpdateRequestContainer.defaultMemberDisplayName}" 
                    comboDefaultValue="${permissionUpdateRequestContainer.defaultMemberId}"
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
