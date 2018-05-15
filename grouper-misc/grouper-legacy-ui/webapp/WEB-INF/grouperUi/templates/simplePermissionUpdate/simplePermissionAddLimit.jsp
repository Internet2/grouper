<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionUpdate/simplePermissionAssignmentPanel.jsp -->

<%-- the success message will go here --%>
<div id="permissionAddLimitMessageId"></div>
  <script type="text/javascript">
    //hide this after it shows for a while
    function hidePermissionAddLimitMessage() {
      $("#permissionAddLimitMessageId").hide('slow');
    }
  </script>

<div class="section" style="min-width: 900px" id="permissionAddLimitPanel">

  <grouper:subtitle key="simplePermissionUpdate.addLimitPanelSubtitle" 
    infodotValue="${grouper:message('simplePermissionUpdate.addLimitPanelSubtitleInfodot', true, false)}" />

  <div class="sectionBody">
    <form id="attributePermissionAddLimitFormId" name="attributePermissionAddLimitFormName" onsubmit="return false;" >

      <input type="hidden" name="permissionAssignType" 
                value="${permissionUpdateRequestContainer.permissionType.name}" />
      <input name="permissionAssignmentId" type="hidden" 
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
              <grouper:message key="simplePermissionUpdate.addLimitDefinition" />
            </label>
          </td>
          <td class="formTableRight">
             <grouper:combobox 
               filterOperation="SimplePermissionUpdateFilter.filterLimitDefinitions" 
               id="permissionAddLimitDef" 
               width="700"/>
          </td>
        </tr>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="permissionResource">
              <grouper:message key="simplePermissionUpdate.addLimitName" />
            </label>
            <sup class="requiredIndicator">*</sup>
          </td>
          <td class="formTableRight">
             <grouper:combobox 
               filterOperation="SimplePermissionUpdateFilter.filterLimitNames" 
               id="permissionAddLimitName" additionalFormElementNames="permissionAddLimitDef"
               width="700"/>
          </td>
        </tr>
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="group">
              <grouper:message key="simplePermissionAssign.addLimitValue" />
            </label>
          </td>
          <td class="formTableRight">
            <input type="text" name="addLimitValue" />
          </td>
        </tr>
        <tr>
         <td colspan="2">
      
           <input class="blueButton" type="submit" 
            onclick="ajax('../app/SimplePermissionUpdate.addLimitCancelButton'); return false;" 
            value="${grouper:message('simplePermissionAssign.addLimitCancelButton', true, false) }" style="margin-top: 2px" />
         
           <input class="blueButton" type="submit" 
            onclick="ajax('../app/SimplePermissionUpdate.addLimitSubmit', {formIds: 'simplePermissionFilterForm, attributePermissionAddLimitFormId'}); return false;" 
            value="${grouper:message('simplePermissionAssign.addLimitSubmitButton', true, false) }" style="margin-top: 2px" />
         
         </td>
        </tr>
      </table>

    </form>
  </div>
</div>

<!-- End: simplePermissionUpdate/simplePermissionAssignmentPanel.jsp -->
