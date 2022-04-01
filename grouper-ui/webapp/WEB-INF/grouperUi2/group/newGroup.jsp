<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] } </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['groupNewBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['groupNewTitle'] }</h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="group-search-label">${textContainer.text['groupCreateSearchForFolderTitle'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="${textContainer.textEscapeXml['groupCreateSearchPlaceholder'] }" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchGroupFormSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemSearchFormId'}); return false;">${textContainer.text['groupCreateSearchButton'] }</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['groupCreateSearchClose'] }</button>
                  </div>
                </div>
                <form id="addGroupForm" class="form-horizontal">
                  <div class="control-group">
                    <label for="folder-path" class="control-label">${textContainer.text['groupCreateFolderLabel'] }</label>
                    <div class="controls">
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em" 
                        filterOperation="../app/UiV2Stem.createGroupParentFolderFilter"
                        value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.objectStemId)}"
                        />
                      <span class="help-block">${textContainer.text['groupCreateIntoFolderDescription'] }</span>
                    
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="groupName" class="control-label">${textContainer.text['groupCreateNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="groupName" name="displayExtension" 
                        onkeyup="syncNameAndId('groupName', 'groupId', 'nameDifferentThanIdId', false, null); return true;"
                       />
                      <span class="help-block">${textContainer.text['groupCreateNameDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="groupId" class="control-label">${textContainer.text['groupCreateIdLabel'] }</label>
                    <div class="controls">
                      <span onclick="syncNameAndId('groupName', 'groupId', 'nameDifferentThanIdId', true, '${textContainer.textEscapeXml['groupNewAlertWhenClickingOnDisabledId']}'); return true;">
                        <input type="text" id="groupId" name="extension" disabled="disabled"  /> 
                      </span>
                      <span style="white-space: nowrap;">
                        <input type="checkbox" name="nameDifferentThanId" id="nameDifferentThanIdId" value="true"
                          onchange="syncNameAndId('groupName', 'groupId', 'nameDifferentThanIdId', false, null); return true;"
                        /> ${textContainer.text['groupNewEditTheId'] }
                      </span>
                      <span class="help-block">${textContainer.text['groupCreateIdDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="groupDescription" class="control-label">${textContainer.text['groupCreateDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="groupDescription" name=description rows="3" cols="40" class="input-block-level"></textarea><span class="help-block">${textContainer.text['groupCreateDescriptionDescription'] }</span>
                    </div>
                  </div>
                  
                  <c:forEach items="${grouperRequestContainer.groupContainer.groupTypesForCreate}" var="groupTypeForEdit">
                  
                    <div class="control-group ${groupTypeForEdit.markerAttributeDefName.id}__class" 
                      style="display: ${groupTypeForEdit.initiallyVisible ? 'block' : 'none'}">
                      <label for="${groupTypeForEdit.attributeName}Id" class="control-label">
                        ${grouper:escapeHtml(groupTypeForEdit.label)}</label>
                      <div class="controls">
                        <c:if test="${groupTypeForEdit.formElementType == 'TEXTFIELD'}">
                          <input type="text" name="${groupTypeForEdit.configId}__name"
                              value="${grouper:escapeHtml(groupTypeForEdit.value)}"
                              id="${groupTypeForEdit.configId}__id">
                        </c:if>
                        
                        <c:if test="${groupTypeForEdit.formElementType == 'CHECKBOX'}">
                         
                          <input type="checkbox" id="${groupTypeForEdit.configId}__id" name="${groupTypeForEdit.configId}__name"
                           ${groupTypeForEdit.value == 'true' ? 'checked="checked"' : ''}
                           value="true"
                           onclick="if($(this).is(':checked')) {$('.${groupTypeForEdit.attributeDefName.id}__class').show(300);} else { $('.${groupTypeForEdit.attributeDefName.id}__class').hide(200); }"
                          />
                        </c:if>
                        <span class="help-block">${grouper:escapeHtml(groupTypeForEdit.description)}</span>
                      </div>
                    </div>
                  
                  </c:forEach>
                  
                  
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
                          <input type="checkbox" name="privileges_readers" value="true"
                            ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllRead ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.readUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_viewers" value="true"
                            ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllView ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.viewUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_optins" value="true"
                            ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllOptin ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.optinUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_optouts" value="true"
                            ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllOptout ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.optoutUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_groupAttrReaders" value="true"
                            ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllAttrRead ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.groupAttrReadUpper'] }
                        </label>

                        <span class="help-block">${textContainer.text['groupCreatePrivilegeDescription']}</span>
                      </div>
                    </div>
                    <div class="control-group">
                      <label for="group-type" class="control-label">${textContainer.text['groupCreateTypeLabel'] }</label>
                      <div class="controls">
                        <label class="radio">
                          <input type="radio" name="groupType" id="group-type-group" value="group" checked>${textContainer.text['groupCreateTypeGroup'] }
                        </label>
                        <label class="radio">
                          <input type="radio" name="groupType" id="group-type-role" value="role">${textContainer.text['groupCreateTypeRole'] }
                        </label><span class="help-block">${textContainer.text['groupCreateTypeDescription'] }</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Group.newGroupSubmit', {formIds: 'addGroupForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');" class="btn btn-cancel" role="button">${textContainer.text['groupCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            
