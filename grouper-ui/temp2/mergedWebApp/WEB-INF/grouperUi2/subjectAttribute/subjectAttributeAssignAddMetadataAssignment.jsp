<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="row-fluid">
  <div class="span12">
    
    <form id="simpleAttributeMetadataAddForm" name="simpleAttributeMetadataAddFormName" class="form-horizontal">
      
      <input name="subjectId" type="hidden" value="${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerMember.subjectId}" />
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
        <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2SubjectAttributeAssignment.assignMetadataAddSubmit', {formIds: 'simpleAttributeMetadataAddForm'}); return false;">${textContainer.text['simpleAttributeAssign.assignAddValueSubmitButton'] }</a>
        <a href="#" onclick="return guiV2link('operation=UiV2SubjectAttributeAssignment.viewAttributeAssignments&subjectId=${attributeUpdateRequestContainer.guiAttributeAssign.attributeAssign.ownerMember.subjectId}');"
                           class="btn">${textContainer.text['simpleAttributeAssign.assignAddValueCancelButton']}</a> 
      </div>
      
    </form>
    
  </div>
</div>