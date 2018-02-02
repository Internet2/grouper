<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.permissionContainer.guiGroup.group.parentUuid}" />

            <div class="row-fluid">
              <div class="span12">
                
                <form id="assignPermissionGroupForm" class="form-horizontal">

                  <input type="hidden" name="groupId" value="${grouperRequestContainer.permissionContainer.guiGroup.group.id}" />
                  
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['groupAssignPermissionPermissionDefLabel'] }</label>
                    <div class="controls">
                      <input type="hidden" name="attributeDefType" value="perm" /> 	                    	
                      <grouper:combobox2 idBase="permissionDefCombo" style="width: 30em"
                        filterOperation="UiV2AttributeDef.attributeDefFilter"
                        additionalFormElementNames="attributeDefType"
                        />
                      <span class="help-block">${textContainer.text['groupAssignPermissionPermissionDefDescription'] }</span>
                    
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['groupAssignPermissionResourceLabel'] }</label>
                    <div class="controls">
                    	                    	
                      <grouper:combobox2 idBase="permissionResourceNameCombo" style="width: 30em"
                        filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
                        additionalFormElementNames="permissionDefComboName,attributeDefType"
                        />
                      <span class="help-block">${textContainer.text['groupAssignPermissionResourceDescription'] }</span>
                    
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['groupAssignPermissionActionLabel'] }</label>
                    <div class="controls">
                    	                    	
                      <grouper:combobox2 idBase="permissionActionCombo" style="width: 30em"
                        filterOperation="UiV2Permission.permissionActionNameFilter"
                        additionalFormElementNames="permissionDefComboName,permissionResourceNameComboName"
                        />
                      <span class="help-block">${textContainer.text['groupAssignPermissionActionDescription'] }</span>
                    
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="permission-allow" class="control-label">${textContainer.text['groupAssignPermissionAllowedLabel'] }</label>
                      <div class="controls">
                        <label class="radio">
                          <input type="radio" name="permissionAddAllowed" id="permission-allow" value="ALLOWED" checked="checked">
                          ${textContainer.text['groupAssignPermissionAllowLabel'] }
                        </label>
                        <label class="radio">
                          <input type="radio" name="permissionAddAllowed" id="permission-disallow" value="DISALLOWED">${textContainer.text['groupAssignPermissionDisallowLabel'] }
                        </label><span class="help-block">${textContainer.text['groupAssignPermissionAllowedDescription'] }</span>
                      </div>
                  </div>
                  
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Permission.assignGroupPermissionSubmit', {formIds: 'assignPermissionGroupForm'}); return false;">${textContainer.text['groupAssignPermissionSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Permission.groupPermission?groupId=${grouperRequestContainer.permissionContainer.guiGroup.group.id}');" class="btn btn-cancel" role="button">${textContainer.text['groupAssignPermissionCancelButton'] }</a></div>
                </form>
              </div>
            </div>