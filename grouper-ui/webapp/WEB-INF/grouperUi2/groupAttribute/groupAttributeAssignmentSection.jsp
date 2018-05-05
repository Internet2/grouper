<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  
                
         <div class="row-fluid">
         <div class="span9" style="margin-bottom: 20px;">
           <div class="lead">${textContainer.text['groupAttributeAssignmentsTitle'] }</div>
           <span>${textContainer.text['groupAttributeAssignmentsDescription'] }</span>
         </div>
         <div class="span3" id="groupAttributeMoreActionsButtonContentsDivId">
           <c:if test="${grouperRequestContainer.groupContainer.canUpdateAttributes}">
            <%@ include file="groupAttributeMoreActionsButtonContents.jsp"%>
           </c:if>
         </div>
        </div>
        <div class="row-fluid">
         <div id="assign-group-attribute-block-container" class="well hide">
          <form id="assignAttributeGroupForm" class="form-horizontal">
             <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
             <div class="control-group">
               <label class="control-label">${textContainer.text['groupAssignAttributeAttributeDefNameLabel'] }</label>
               <div class="controls">
                 <grouper:combobox2 idBase="attributeDefNameCombo" style="width: 30em"
                   filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
                   value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                   />
                 <span class="help-block">${textContainer.text['groupAssignAttributeAttributeDefNameDescription'] }</span>
               
               </div>
             </div>
             
             <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2GroupAttributeAssignment.assignAttributeSubmit', {formIds: 'assignAttributeGroupForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
             </div>
           </form>
         
         </div>
         </div>
         <!-- This div is filled with the table of existing group attribute assignments -->
         <div id="viewAttributeAssignments">
           
         </div>
                  
