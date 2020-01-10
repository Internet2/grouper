<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.permissionContainer.guiGroup.group.parentUuid}" />

<div class="row-fluid">
  <div class="span12">
    <form id="simplePermissionLimitValueEditForm" name="simplePermissionLimitValueEditFormName" class="form-horizontal">
      <input name="limitAssignId" type="hidden" value="${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      <input name="limitAssignValueId" type="hidden" value="${permissionUpdateRequestContainer.attributeAssignValue.id }" />
      <input name="memberId" type="hidden" value="${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.ownerAttributeAssign.ownerMemberId}" />
      
      <div class="control-group">
        <c:set var="attributeAssignTypeLabelKey" value="${permissionUpdateRequestContainer.attributeAssignTypeLabelKey}"></c:set>
        <label class="control-label no-padding">${textContainer.text[attributeAssignTypeLabelKey] }</label>
        <div class="controls">
          ${grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionUpdate.assignHeaderPermissionName'] }</label>
        <div class="controls">
          ${grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeDefName.displayName)}
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionAssign.limitAction'] }</label>
        <div class="controls">
          ${permissionUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignAction.name}
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionAssign.limitName'] }</label>
        <div class="controls">
          ${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.attributeDefName.displayName}
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simplePermissionAssign.limitEditId'] }</label>
        <div class="controls">
          ${permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id}
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label">${textContainer.text['simpleAttributeUpdate.assignValueEdit'] }</label>
        <div class="controls">
          <input type="text" name="valueToEdit"  id="valueToEditId" 
              value="${permissionUpdateRequestContainer.attributeAssignValue.valueFriendly}" />
        </div>
      </div>
      
      <div class="form-actions">
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2SubjectPermission.limitValueEditSubmit', {formIds: 'simplePermissionLimitValueEditForm'}); return false;">${textContainer.text['simplePermissionUpdate.limitValueEditSubmitButton'] }</a> 
        <a href="#" class="btn btn-cancel" role="button" onclick="ajax('../app/UiV2SubjectPermission.subjectPermission', {formIds: 'simplePermissionLimitValueEditForm'}); return false;">${textContainer.text['simplePermissionUpdate.limitValueEditCancelButton'] }</a>
      </div>
      
    </form>
    
  </div>
</div>