<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="icon-folder-close"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['groupEditTitle'] }</small></h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form id="editGroupForm" class="form-horizontal">

                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                  
                  <div class="control-group">
                    <label for="groupName" class="control-label">${textContainer.text['groupCreateNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="groupName" name="displayExtension" 
                        value="${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}" /><span 
                        class="help-block">${textContainer.text['groupCreateNameDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="groupId" class="control-label">${textContainer.text['groupCreateIdLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="groupId" name="extension"
                      value="${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.extension)}"
                       /><span class="help-block">${textContainer.text['groupCreateIdDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="group-description" class="control-label">${textContainer.text['groupCreateDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="group-description" name=description rows="3" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.description)}</textarea><span 
                        class="help-block">${textContainer.text['groupCreateDescriptionDescription'] }</span>
                    </div>
                  </div>
                  <p class="shownAdvancedProperties"><a href="#" 
                    onclick="$('.hiddenAdvancedProperties').show('slow'); $('.shownAdvancedProperties').hide('slow'); return false;" 
                    >${textContainer.text['groupCreateAdvanced'] } <i class="icon-angle-down"></i></a></p>
                  <p class="hiddenAdvancedProperties" style="display: none"
                    onclick="$('.hiddenAdvancedProperties').hide('slow'); $('.shownAdvancedProperties').show('slow'); return false;" 
                    ><a href="#" >${textContainer.text['groupCreateHideAdvanced'] } <i class="icon-angle-up"></i></a></p>
                  <div class="hiddenAdvancedProperties" style="display: none">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['groupCreateAssignPrivilegesToEveryone'] }</label>
                      <div class="controls">
                        <label class="checkbox inline">
                          <%--
                          <input type="checkbox" name="privileges_admins" value="true" 
                            ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllAdmin ? 'checked="checked"' : '' } />ADMIN
                          --%>
                          <input type="checkbox" name="privileges_admins" value="true" 
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllAdmin ? 'checked="checked"' : '' }
                          />${textContainer.text['priv.adminUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_updaters" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllUpdate ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.updateUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_readers" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllRead ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.readUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_viewers" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllView ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.viewUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_optins" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllOptin ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.optinUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_optouts" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllOptout ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.optoutUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_groupAttrReaders" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllAttrRead ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.groupAttrReadUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_groupAttrUpdaters" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllAttrUpdate ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.groupAttrUpdateUpper'] }
                        </label>

                        <span class="help-block">${textContainer.text['groupCreatePrivilegeDescription']}</span>
                      </div>
                    </div>
                    <div class="control-group">
                      <label for="group-type" class="control-label">${textContainer.text['groupCreateTypeLabel'] }</label>
                      <div class="controls">
                        <label class="radio">
                          <input type="radio" name="groupType" id="group-type-group" value="group" 
                            ${grouperRequestContainer.groupContainer.guiGroup.group.typeOfGroup.name == 'group' ? 'checked="checked"' : '' }  
                          >${textContainer.text['groupCreateTypeGroup'] }
                        </label>
                        <label class="radio">
                          <input type="radio" name="groupType" id="group-type-role" value="role"
                            ${grouperRequestContainer.groupContainer.guiGroup.group.typeOfGroup.name == 'role' ? 'checked="checked"' : '' }  
                          >${textContainer.text['groupCreateTypeRole'] }
                        </label><span class="help-block">${textContainer.text['groupCreateTypeDescription'] }</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Group.groupEditSubmit', {formIds: 'editGroupForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" class="btn btn-cancel">${textContainer.text['groupCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            