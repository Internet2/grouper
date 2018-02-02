<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${permissionUpdateRequestContainer.guiPermissionEntry.guiRole.group.parentUuid}" />

<div class="row-fluid">
  <div class="span12">
    
    <form id="attributePermissionAddLimitFormId" class="form-horizontal" name="attributePermissionAddLimitFormName" onsubmit="return false;" >
      <input type="hidden" name="permissionAssignType" 
                value="${permissionUpdateRequestContainer.permissionType.name}" />
      <input name="permissionAssignmentId" type="hidden" 
                value="${permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.attributeAssignId }" />
                
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.addLimitRole'] }</label>
        <div class="controls">
          ${permissionUpdateRequestContainer.guiPermissionEntry.guiRole.shortLinkWithIcon}
          <%-- <span class="help-block">${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.screenLabelLongIfDifferent}</span> --%>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.addLimitPermissionName'] }</label>
        <div class="controls">
          ${permissionUpdateRequestContainer.guiPermissionEntry.guiAttributeDefName.shortLinkWithIcon}
          <%-- <span class="help-block">${textContainer.text['grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.screenLabelLongIfDifferent'] }</span> --%>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simplePermissionUpdate.addLimitPermissionAction'] }</label>
        <div class="controls">
          ${grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.permissionEntry.action)}
          <%-- <span class="help-block">${textContainer.text['grouper:escapeHtml(permissionUpdateRequestContainer.guiPermissionEntry.screenLabelLongIfDifferent'] }</span> --%>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simplePermissionUpdate.addLimitDefinition'] }</label>
        <div class="controls">
          <input type="hidden" name="attributeDefType" value="limit" /> 	                    	
          <grouper:combobox2 idBase="limitDefCombo" style="width: 30em"
            filterOperation="UiV2AttributeDef.attributeDefFilter"
            additionalFormElementNames="attributeDefType"
          />
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simplePermissionUpdate.addLimitName'] }</label>
        <div class="controls">
          <grouper:combobox2 idBase="limitResourceNameCombo" style="width: 30em"
            filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
            additionalFormElementNames="limitDefComboName,attributeDefType"
           />
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simplePermissionAssign.addLimitValue'] }</label>
        <div class="controls">
          <input type="text" name="addLimitValue" />
        </div>
      </div>
      
      <div class="form-actions">
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Permission.assignLimitSubmit', {formIds: 'attributePermissionAddLimitFormId'}); return false;">${textContainer.text['groupAssignPermissionSaveButton'] }</a> 
        <a href="#" class="btn btn-cancel" role="button" onclick="ajax('../app/UiV2Permission.groupViewPermissions', {formIds: 'attributePermissionAddLimitFormId'}); return false;">${textContainer.text['groupAssignPermissionCancelButton'] }</a> 
      </div>
                  
    </form>
    
  </div>
</div>