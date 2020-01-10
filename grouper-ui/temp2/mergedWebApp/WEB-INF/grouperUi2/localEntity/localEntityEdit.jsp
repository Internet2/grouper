<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-cloud-download"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['localEntityEditTitle'] }</small></h1>
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
                    <label for="groupDescription" class="control-label">${textContainer.text['groupCreateDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="groupDescription" name=description rows="3" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.description)}</textarea><span 
                        class="help-block">${textContainer.text['groupCreateDescriptionDescription'] }</span>
                    </div>
                  </div>
                  <p class="shownAdvancedProperties"><a href="#" 
                    onclick="$('.hiddenAdvancedProperties').show('slow'); $('.shownAdvancedProperties').hide('slow'); return false;" 
                    >${textContainer.text['groupCreateAdvanced'] } <i class="fa fa-angle-down"></i></a></p>
                  <p class="hiddenAdvancedProperties" style="display: none"
                    onclick="$('.hiddenAdvancedProperties').hide('slow'); $('.shownAdvancedProperties').show('slow'); return false;" 
                    ><a href="#" >${textContainer.text['groupCreateHideAdvanced'] } <i class="fa fa-angle-up"></i></a></p>
                  <div class="hiddenAdvancedProperties" style="display: none">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['groupCreateAssignPrivilegesToEveryone'] }</label>
                      <div class="controls">
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_viewers" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllView ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.viewUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_groupAttrReaders" value="true"
                            ${grouperRequestContainer.groupContainer.guiGroup.grantAllAttrRead ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.groupAttrReadUpper'] }
                        </label>

                        <span class="help-block">${textContainer.text['groupCreatePrivilegeDescription']}</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2LocalEntity.localEntityEditSubmit', {formIds: 'editGroupForm'}); return false;">${textContainer.text['localEntityCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&sourceId=grouperEntities&subjectId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" class="btn btn-cancel">${textContainer.text['localEntityCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            
