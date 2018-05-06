<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  
                
         <div class="row-fluid">
         <div class="span9" style="margin-bottom: 20px;">
           <div class="lead">${textContainer.text['subjectPermissionsTitle'] }</div>
           <span>${textContainer.text['subjectPermissionsDescription'] }</span>
         </div>
         <div class="span3" id="subjectPermissionMoreActionsButtonContentsDivId">
           <%@ include file="subjectPermissionMoreActionsButtonContents.jsp"%>
         </div>
        </div>
        <div class="row-fluid">
         <div id="assign-permission-block-container" class="well hide">
           <form id="assignPermissionSubjectForm" class="form-horizontal">
            <input type="hidden" name="memberId" value="${grouperRequestContainer.permissionContainer.guiMember.member.id}" />
          
          <div class="control-group">
            <label class="control-label">${textContainer.text['subjectAssignPermissionPermissionDefLabel'] }</label>
            <div class="controls">
              <input type="hidden" name="attributeDefType" value="perm" /> 	                    	
              <grouper:combobox2 idBase="permissionDefCombo" style="width: 30em"
                filterOperation="UiV2AttributeDef.attributeDefFilter"
                additionalFormElementNames="attributeDefType"
                />
              <span class="help-block">${textContainer.text['subjectAssignPermissionPermissionDefDescription'] }</span>
            
            </div>
          </div>
          <div class="control-group">
            <label class="control-label">${textContainer.text['subjectAssignPermissionResourceLabel'] }</label>
            <div class="controls">
            	                    	
              <grouper:combobox2 idBase="permissionResourceNameCombo" style="width: 30em"
                filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
                additionalFormElementNames="permissionDefComboName,attributeDefType"
                />
              <span class="help-block">${textContainer.text['subjectAssignPermissionResourceDescription'] }</span>
            
            </div>
          </div>
          
          <div class="control-group">
            <label class="control-label">${textContainer.text['subjectAssignPermissionActionLabel'] }</label>
            <div class="controls">
            	                    	
              <grouper:combobox2 idBase="permissionActionCombo" style="width: 30em"
                filterOperation="UiV2GroupPermission.permissionActionNameFilter"
                additionalFormElementNames="permissionDefComboName,permissionResourceNameComboName"
                />
              <span class="help-block">${textContainer.text['subjectAssignPermissionActionDescription'] }</span>
            
            </div>
          </div>
          
          <div class="control-group">
            <label class="control-label">${textContainer.text['subjectAssignPermissionRoleLabel'] }</label>
            <div class="controls">
              <input type="hidden" name="typeOfGroup" value="role" />                   	
               <grouper:combobox2 idBase="subjectRoleCombo" style="width: 30em"
                 filterOperation="../app/UiV2Group.groupRoleAssignPermissionFilter"
                 additionalFormElementNames="typeOfGroup"/>
                                    
              <span class="help-block">${textContainer.text['subjectAssignPermissionRoleDescription'] }</span>
            
            </div>
          </div>
          
          <div class="control-group">
            <label for="permission-allow" class="control-label">${textContainer.text['subjectAssignPermissionAllowedLabel'] }</label>
              <div class="controls">
                <label class="radio">
                  <input type="radio" name="permissionAddAllowed" id="permission-allow" value="ALLOWED" checked="checked">
                  ${textContainer.text['subjectAssignPermissionAllowLabel'] }
                </label>
                <label class="radio">
                  <input type="radio" name="permissionAddAllowed" id="permission-disallow" value="DISALLOWED">${textContainer.text['subjectAssignPermissionDisallowLabel'] }
                </label><span class="help-block">${textContainer.text['subjectAssignPermissionAllowedDescription'] }</span>
              </div>
          </div>
          
          <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2SubjectPermission.assignSubjectPermissionSubmit', {formIds: 'assignPermissionSubjectForm'}); return false;">${textContainer.text['subjectAssignPermissionSaveButton'] }</a> 
          </div>
        </form>
         
         </div>
         </div>
         <!-- This div is filled with the table of existing permissions -->
         <div id="viewPermissions">
           
         </div>
                  
