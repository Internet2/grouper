<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${permissionUpdateRequestContainer.guiPermissionEntry.guiRole.group.parentUuid}" />

<div class="row-fluid">
  <div class="span12">
    <form id="simplePermissionEditForm" name="simplePermissionEditFormName" class="form-horizontal">
      <c:set var="guiPermissionEntry" value="${permissionUpdateRequestContainer.guiPermissionEntry}"/>
      <c:set var="permissionEntry" value="${guiPermissionEntry.permissionEntry}"/>

      <%-- signify stash the type of assignment --%>
      <input type="hidden" name="permissionAssignType" 
                value="${permissionUpdateRequestContainer.permissionType.name}" />

      <input type="hidden" name="permissionAssignmentId" 
                value="${permissionEntry.attributeAssignId}" />
                
     <input type="hidden" name="memberId" 
                value="${permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.memberId}" />
      
      <div class="control-group">
        <c:set var="permissionType" value="permissionUpdateRequestContainer.permissionType.${permissionUpdateRequestContainer.permissionType.name}" />
        <label class="control-label no-padding">${textContainer.text['permissionUpdateRequestContainer.permissionType'] }</label>
        <div class="controls">
          <span>${textContainer.text[permissionType] }</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.assignHeaderRoleName'] }</label>
        <div class="controls">
          <span>${guiPermissionEntry.guiRole.shortLinkWithIcon}</span>
        </div>
      </div>
      
      <c:choose>
        <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
          <div class="control-group">
            <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.assignHeaderEntity'] }</label>
            <div class="controls">
              <span>${grouper:escapeHtml(guiPermissionEntry.screenLabelShort)}</span>
            </div>
          </div>
        </c:when>
      </c:choose>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.assignHeaderResource'] }</label>
        <div class="controls">
          <span>${guiPermissionEntry.guiAttributeDefName.shortLinkWithIcon}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.assignHeaderAllowed'] }</label>
        <div class="controls">
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
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.assignHeaderDefinition'] }</label>
        <div class="controls">
          <span>${guiPermissionEntry.guiAttributeDef.shortLinkWithIcon}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionAssign.assignEditId'] }</label>
        <div class="controls">
          <span>${permissionEntry.attributeAssignId}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simplePermissionUpdate.assignEditEnabledDate'] }</label>
        <div class="controls">
          <input type="text" name="enabledDate"  id="enabledDateId"
                 value="${guiPermissionEntry.enabledDate}" />
          <span>${textContainer.text['simplePermissionUpdate.assignEditEnabledDisabledDateMask']}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simplePermissionUpdate.assignEditDisabledDate'] }</label>
        <div class="controls">
          <input type="text" name="disabledDate"  id="disabledDateId"
                 value="${guiPermissionEntry.disabledDate}" />
          <span>${textContainer.text['simplePermissionUpdate.assignEditEnabledDisabledDateMask']}</span>
        </div>
      </div>
      
      <div class="form-actions">
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2SubjectPermission.permissionEditSubmit', {formIds: 'simplePermissionEditForm'}); return false;">${textContainer.text['simplePermissionAssign.assignEditSubmitButton'] }</a> 
        <a href="#" class="btn btn-cancel" role="button" onclick="ajax('../app/UiV2SubjectPermission.subjectPermission', {formIds: 'simplePermissionEditForm'}); return false;">${textContainer.text['groupAssignPermissionCancelButton'] }</a>
      </div>
      
    </form>
  </div>
</div>