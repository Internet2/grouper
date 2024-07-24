<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="row-fluid">
  <div class="span12">
    
    <form id="simpleAttributeAssignEditForm" name="simpleAttributeAssignEditFormName" class="form-horizontal">
      
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
          <input type="date" name="enabledDate"  id="enabledDateId"
            value="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null ? attributeUpdateRequestContainer.guiAttributeAssign.enabledDate : attributeUpdateRequestContainer.guiAttributeAssignAssign.enabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled">
              ${textContainer.text['simpleAttributeUpdate.assignEditEnabledDisabledDateMask'] }
            </span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeUpdate.assignEditDisabledDate'] }</label>
        <div class="controls">
          <input type="date" name="disabledDate"  id="disabledDateId"
            value="${attributeUpdateRequestContainer.guiAttributeAssignAssign == null ? attributeUpdateRequestContainer.guiAttributeAssign.disabledDate : attributeUpdateRequestContainer.guiAttributeAssignAssign.disabledDate}" 
            style="width: 8em" />
            <span class="simpleMembershipUpdateDisabled">
              ${textContainer.text['simpleAttributeUpdate.assignEditEnabledDisabledDateMask'] }
            </span>
        </div>
      </div>
      
      <div class="form-actions">
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2MembershipAttributeAssignment.assignEditSubmit', {formIds: 'simpleAttributeAssignEditForm'}); return false;">${textContainer.text['simpleAttributeAssign.assignEditSubmitButton'] }</a>
        <a href="#" onclick="return guiV2link(buildCancelLink())" class="btn">${textContainer.text['simpleAttributeAssign.assignEditCancelButton']}</a> 
      </div>
      
    </form>
    
  </div>
</div>
<script type="text/javascript">
  function buildCancelLink() {
    
    var subjectId = $("#subjectId").val();
    var groupId = $("#groupId").val();
    
    var cancelLink = "operation=UiV2MembershipAttributeAssignment.viewAttributeAssignments&subjectId=";
    cancelLink = cancelLink + subjectId;
    cancelLink = cancelLink + "&groupId=";
    cancelLink = cancelLink + groupId;
    return cancelLink;
  }
</script>