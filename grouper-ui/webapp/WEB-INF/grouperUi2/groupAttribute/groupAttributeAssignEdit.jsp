<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerGroup.parentUuid}" />

<div class="row-fluid">
  <div class="span12">
    
    <form id="simpleAttributeAssignEditForm" name="simpleAttributeAssignEditFormName" class="form-horizontal">
      
      <input name="groupId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerGroupId }" />
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
        <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      </c:if>
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null}">
        <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id }" />
      </c:if>
      
      <div class="control-group">
        <c:set var="attributeAssignTypeLabelKey" value="${attributeUpdateRequestContainer.attributeAssignTypeLabelKey}"></c:set>
        <label class="control-label no-padding">${textContainer.text[attributeAssignTypeLabelKey] }
          <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
            ${textContainer.text['simpleAttributeUpdate.assignMetadataLabelSuffix']}
          </c:if>
        </label>
        <div class="controls">
          <span>${grouper:escapeHtml(attributeUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeName'] }
          <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
            ${textContainer.text['simpleAttributeUpdate.assignMetadataLabelSuffix'] }
          </c:if>
        </label>
        <div class="controls">
          <span>${attributeUpdateRequestContainer.guiAttributeAssign.guiAttributeDefName.shortLinkWithIcon}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeDef'] }
          <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
            ${textContainer.text['simpleAttributeUpdate.assignMetadataLabelSuffix'] }
          </c:if>
        </label>
        <div class="controls">
          <span>${attributeUpdateRequestContainer.guiAttributeAssign.guiAttributeDef.shortLinkWithIcon}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeAssign.assignEditId'] }</label>
        <div class="controls">
          <span>
            ${attributeUpdateRequestContainer.guiAttributeAssignAssign == null ? attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id : attributeUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id}
          </span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeUpdate.assignEditEnabledDate'] }</label>
        <div class="controls">
          <input type="text" name="enabledDate"  id="enabledDateId"
            value="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null ? attributeUpdateRequestContainer.guiAttributeAssign.enabledDate : attributeUpdateRequestContainer.guiAttributeAssignAssign.enabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simpleAttributeUpdate.assignEditEnabledDisabledDateMask" /></span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeUpdate.assignEditDisabledDate'] }</label>
        <div class="controls">
          <input type="text" name="disabledDate"  id="disabledDateId"
            value="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null ? attributeUpdateRequestContainer.guiAttributeAssign.disabledDate : attributeUpdateRequestContainer.guiAttributeAssignAssign.disabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message key="simpleAttributeUpdate.assignEditEnabledDisabledDateMask" /></span>
        </div>
      </div>
      
      <div class="form-actions">
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2GroupAttributeAssignment.assignEditSubmit', {formIds: 'simpleAttributeAssignEditForm'}); return false;">${textContainer.text['simpleAttributeAssign.assignEditSubmitButton'] }</a>
        <a href="#" onclick="return guiV2link('operation=UiV2GroupAttributeAssignment.viewAttributeAssignments&groupId=${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerGroupId}');"
                           class="btn">${textContainer.text['simpleAttributeAssign.assignEditCancelButton']}</a> 
      </div>
      
    </form>
    
  </div>
</div>