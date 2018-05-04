<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  
                
         <div class="row-fluid">
         <div class="span9" style="margin-bottom: 20px;">
           <div class="lead">${textContainer.text['attributeDefAttributeAssignmentsTitle'] }</div>
           <span>${textContainer.text['attributeDefAttributeAssignmentsDescription'] }</span>
         </div>
         <div class="span3" id="attributeDefAttributeMoreActionsButtonContentsDivId">
           <%@ include file="attributeDefAttributeMoreActionsButtonContents.jsp"%>
         </div>
        </div>
        <div class="row-fluid">
         <div id="assign-attribute-def-attribute-block-container" class="well hide">
          <form id="assignAttributeForm" class="form-horizontal">
             <input type="hidden" name="attributeDefId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" />
             <div class="control-group">
               <label class="control-label">${textContainer.text['attributeDefAssignAttributeAttributeDefNameLabel'] }</label>
               <div class="controls">
                 <grouper:combobox2 idBase="attributeDefNameCombo" style="width: 30em"
                   filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
                   value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                   />
                 <span class="help-block">${textContainer.text['attributeDefAssignAttributeAttributeDefNameDescription'] }</span>
               
               </div>
             </div>
             
             <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2AttributeDefAttributeAssignment.assignAttributeSubmit', {formIds: 'assignAttributeForm'}); return false;">${textContainer.text['attributeDefAssignSaveButton'] }</a> 
             </div>
           </form>
         
         </div>
         </div>
         <!-- This div is filled with the table of existing attributeDef assignments -->
         <div id="viewAttributeAssignments">
           
         </div>
                  
