<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  
                <c:choose>
                  <c:when test="${grouperRequestContainer.groupContainer.guiGroup.typeRole}">
                    <div class="row-fluid">
                    <div class="span9" style="margin-bottom: 20px;">
                      <div class="lead">${textContainer.text['groupPermissionsTitle'] }</div>
                      <span>${textContainer.text['groupPermissionsDescription'] }</span>
                    </div>
                    <div class="span3" id="groupPermissionMoreActionsButtonContentsDivId">
                      <%@ include file="groupPermissionMoreActionsButtonContents.jsp"%>
                    </div>
                   </div>
                   <div class="row-fluid">
                    <div id="assign-permission-block-container" class="well hide">
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
			                        filterOperation="UiV2GroupPermission.permissionActionNameFilter"
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
			                  
			                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2GroupPermission.assignGroupPermissionSubmit', {formIds: 'assignPermissionGroupForm'}); return false;">${textContainer.text['groupAssignPermissionSaveButton'] }</a> 
			                  </div>
			                </form>
                    
                    </div>
                    </div>
                    <!-- This div is filled with the table of existing permissions -->
                    <div id="viewPermissions">
                      
                    </div>
                  </c:when>
                  <c:otherwise>
                  <div class="row-fluid">
                    <div class="span9">
                      <span>${textContainer.text['groupPermissionsGroupNotRoleText'] }</span>
                      <a href="#" class="btn btn-primary"
                        onclick="return guiV2link('operation=UiV2Group.convertGroupToRole&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;">${textContainer.text['groupPermissionsGroupToRoleButton'] }
                      </a>
                    </div>
                    </div>
                  </c:otherwise>
                </c:choose>
