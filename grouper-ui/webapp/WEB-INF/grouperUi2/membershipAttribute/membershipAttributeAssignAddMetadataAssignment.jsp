<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="row-fluid">
  <div class="span12">
    
    <form id="simpleAttributeMetadataAddForm" name="simpleAttributeMetadataAddFormName" class="form-horizontal">
       
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignType == 'imm_mem'}">
        <input name="subjectId" id="subjectId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerMembership.member.subjectId}" />
        <input name="groupId" id="groupId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerMembership.ownerGroup.id}" />
      </c:if>
      
      <c:if test="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignType == 'any_mem'}">
        <input name="subjectId" id="subjectId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerMember.subjectId}" />
        <input name="groupId" id="groupId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerGroup.id}" />
      </c:if>
      
      <input name="attributeAssignId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id }" />
      
      <div class="control-group">
        <c:set var="attributeAssignTypeLabelKey" value="${attributeUpdateRequestContainer.attributeAssignTypeLabelKey}"></c:set>
        <label class="control-label no-padding">${textContainer.text[attributeAssignTypeLabelKey] }
            ${textContainer.text['simpleAttributeUpdate.assignMetadataLabelSuffix']}
        </label>
        <div class="controls">
          <span>${grouper:escapeHtml(attributeUpdateRequestContainer.guiAttributeAssign.screenLabelLong)}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeName'] }
            ${textContainer.text['simpleAttributeUpdate.assignMetadataLabelSuffix'] }
        </label>
        <div class="controls">
          <span>${attributeUpdateRequestContainer.guiAttributeAssign.guiAttributeDefName.shortLinkWithIcon}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeDef'] }
            ${textContainer.text['simpleAttributeUpdate.assignMetadataLabelSuffix'] }
        </label>
        <div class="controls">
          <span>${attributeUpdateRequestContainer.guiAttributeAssign.guiAttributeDef.shortLinkWithIcon}</span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">
        ${textContainer.text['simpleAttributeAssign.assignEditId'] }
        ${textContainer.text['simpleAttributeUpdate.assignMetadataLabelSuffix'] }
        </label>
        <div class="controls">
          <span>
            ${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.id}
          </span>
        </div>
      </div>
      
      <div class="control-group">
        <label class="control-label no-padding">${textContainer.text['simpleAttributeAssign.attributeName'] }</label>
        <div class="controls">
          <input type="hidden" name="attributeAssignType" value="${attributeUpdateRequestContainer.attributeAssignType.assignmentOnAssignmentType}" />
          <grouper:combobox2 idBase="attributeAssignAssignAttributeCombo" style="width: 30em"
           filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
           additionalFormElementNames="attributeAssignType"
           />
        </div>
      </div>
      
      <div class="form-actions">
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2MembershipAttributeAssignment.assignMetadataAddSubmit', {formIds: 'simpleAttributeMetadataAddForm'}); return false;">${textContainer.text['simpleAttributeAssign.assignAddValueSubmitButton'] }</a>
        <a href="#" onclick="return guiV2link(buildCancelLink())" class="btn">${textContainer.text['simpleAttributeAssign.assignAddValueCancelButton']}</a> 
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