<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  
                
         <div class="row-fluid">
         <div class="span9" style="margin-bottom: 20px;">
           <div class="lead">${textContainer.text['membershipAttributeAssignmentsTitle'] }</div>
           <span>${textContainer.text['membershipAttributeAssignmentsDescription'] }</span>
         </div>
         <div class="span3" id="membershipAttributeMoreActionsButtonContentsDivId">
           <%@ include file="membershipAttributeMoreActionsButtonContents.jsp"%>
         </div>
        </div>
        <div class="row-fluid">
         <div id="assign-membership-attribute-block-container" class="well hide">
          <form id="assignAttributeMembershipForm" class="form-horizontal">
             <input name="groupId" type="hidden" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
             <input name="subjectId" type="hidden" value="${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" />
             
             <div class="control-group">
               <label for="assignment-type" class="control-label">${textContainer.text['membershipAttributeAssignmentType'] }</label>
               <div class="controls">
                 <label class="radio">
                   <input type="radio" name="assignmentType" id="assignment-type-any-mem" value="any_mem" onclick="getElementById('attribute-assignment-type').value = 'any_mem'">${textContainer.text['membershipAttributeAssignmentAnyMembershipType'] }
                 </label>
                 <label class="radio">
                   <input type="radio" name="assignmentType" id="assignment-type-imm-mem" value="imm_mem" onclick="getElementById('attribute-assignment-type').value = 'imm_mem'" >${textContainer.text['membershipAttributeAssignmentImmediateMembershipType'] }
                 </label>
               </div>
             </div>
             
             <div class="control-group">
               <label class="control-label">${textContainer.text['membershipAssignAttributeAttributeDefNameLabel'] }</label>
               <div class="controls">
                                       
                 <grouper:combobox2 idBase="attributeDefNameCombo" style="width: 30em"
                   filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
                   value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                   />
                 <span class="help-block">${textContainer.text['membershipAssignAttributeAttributeDefNameDescription'] }</span>
             
               </div>
             </div>
             
             <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2MembershipAttributeAssignment.assignAttributeSubmit', {formIds: 'assignAttributeMembershipForm'}); return false;">${textContainer.text['membershipAssignAttributeButton'] }</a> 
             </div>
           </form>
         
         </div>
         </div>
         <!-- This div is filled with the table of existing member attribute assignments -->
         <div id="viewAttributeAssignments">
           
         </div>
                  
