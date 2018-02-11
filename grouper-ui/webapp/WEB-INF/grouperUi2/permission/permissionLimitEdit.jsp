<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.permissionContainer.guiGroup.group.parentUuid}" />

<div class="row-fluid">
  <div class="span12">
  
    <form id="simplePermissionLimitEditForm" name="simplePermissionLimitEditFormName" class="form-horizontal">
      <input name="limitAssignId" type="hidden" value="${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      
      <div class="control-group">
        <c:set var="attributeAssignTypeLabelKey" value="${permissionUpdateRequestContainer.attributeAssignTypeLabelKey}"></c:set>
        <label class="control-label no-padding">${textContainer.text[attributeAssignTypeLabelKey]}</label>
        <div class="controls">
          <span>${grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.assignHeaderPermissionName']}</label>
        <div class="controls">
          <span>${grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeDefName.displayName)}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionAssign.limitAction']}</label>
        <div class="controls">
          <span>${permissionUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignAction.name}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionAssign.limitName']}</label>
        <div class="controls">
          <span>${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.attributeDefName.displayName}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionAssign.limitEditId']}</label>
        <div class="controls">
          <span>${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simplePermissionUpdate.limitEditEnabledDate']}</label>
        <div class="controls">
          <input type="text" name="enabledDate"  id="enabledDateId"
            value="${permissionUpdateRequestContainer.guiAttributeAssignAssign == null ? permissionUpdateRequestContainer.guiAttributeAssign.enabledDate : permissionUpdateRequestContainer.guiAttributeAssignAssign.enabledDate}" 
          />
          <span>${textContainer.text['simpleAttributeUpdate.assignEditEnabledDisabledDateMask']}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simplePermissionUpdate.limitEditDisabledDate']}</label>
        <div class="controls">
          <input type="text" name="disabledDate"  id="disabledDateId"
            value="${permissionUpdateRequestContainer.guiAttributeAssignAssign == null ? permissionUpdateRequestContainer.guiAttributeAssign.disabledDate : permissionUpdateRequestContainer.guiAttributeAssignAssign.disabledDate}" 
          />
          <span>${textContainer.text['simpleAttributeUpdate.assignEditEnabledDisabledDateMask']}</span>
        </div>
      </div>
      
      <div class="form-actions">
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Permission.permissionLimitEditSubmit', {formIds: 'simplePermissionLimitEditForm'}); return false;">${textContainer.text['simplePermissionAssign.assignEditSubmitButton'] }</a> 
        <a href="#" class="btn btn-cancel" role="button" onclick="ajax('../app/UiV2Permission.groupPermission', {formIds: 'simplePermissionLimitEditForm'}); return false;">${textContainer.text['simplePermissionAssign.assignEditCancelButton'] }</a>
      </div>
      
    </form>
  </div>
</div>