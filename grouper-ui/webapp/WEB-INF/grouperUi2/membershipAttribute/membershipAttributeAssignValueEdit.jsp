<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="row-fluid">
  <div class="span12">
    
    <form id="simpleAttributeAssignValueEditForm" name="simpleAttributeAssignValueEditFormName" class="form-horizontal">
    
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignType == 'imm_mem'}">
        <input name="subjectId" id="subjectId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerMembership.member.subjectId}" />
        <input name="groupId" id="groupId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerMembership.ownerGroup.id}" />
      </c:if>
      
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignType == 'any_mem'}">
        <input name="subjectId" id="subjectId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerMember.subjectId}" />
        <input name="groupId" id="groupId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerGroup.id}" />
      </c:if>
            
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign != null}">
        <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.id }" />
      </c:if>
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null}">
        <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id }" />
      </c:if>
      <input name="attributeAssignValueId" type="hidden" value="${attributeUpdateRequestContainer.attributeAssignValue.id }" />
      
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
        <label class="control-label no-padding">${textContainer.text['simpleAttributeAssign.assignValueEditId'] }</label>
        <div class="controls">
          <span>${attributeUpdateRequestContainer.attributeAssignValue.id}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeUpdate.assignValueEdit'] }</label>
        <div class="controls">
          <input type="text" name="valueToEdit"  id="valueToEditId"
              value="${attributeUpdateRequestContainer.attributeAssignValue.valueFriendly}" />
        </div>
      </div>
      
      <div class="form-actions">
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2MembershipAttributeAssignment.assignValueEditSubmit', {formIds: 'simpleAttributeAssignValueEditForm'}); return false;">${textContainer.text['simpleAttributeAssign.assignValueEditSubmitButton'] }</a>
        <a href="#" class="btn btn-cancel" role="button" onclick="ajax('../app/UiV2MembershipAttributeAssignment.viewAttributeAssignments', {formIds: 'simpleAttributeAssignValueEditForm'}); return false;">${textContainer.text['simpleAttributeAssign.assignValueEditCancelButton'] }</a>
      </div>
      
    </form>
    
  </div>
</div>