<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('groupEditPageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}
            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />
            <grouper:browserPage jspName="groupEdit" />
            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-group"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['groupEditTitle'] }</small></h1>
              </div>

            </div>
            <script language="javascript">
              $(document).ready(function() {
                $('#groupId').on('input',function(e){
                  if ($('#originalExtension').val() == $('#groupId').val()) {
                    $('#alternateNameDiv').show("slow");
                    $('#setAlternateNameIfRenameDiv').hide("slow");
                  } else {
                    $('#alternateNameDiv').hide("slow");
                    $('#setAlternateNameIfRenameDiv').show("slow");
                  }
                });
              });
            </script>
            <div class="row-fluid">
              <div class="span12">
                <form id="editGroupForm" class="form-horizontal">

                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                  <input type="hidden" id="originalExtension" value="${grouperRequestContainer.groupContainer.guiGroup.group.extension}" />
                  
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
                  <div id="alternateNameDiv" class="control-group">
                    <label for="groupAlternateName" class="control-label">${textContainer.text['groupCreateAlternateNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="groupAlternateName" name="alternateName"
                        value="${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.alternateName)}" /><span
                        class="help-block">${textContainer.text['groupCreateAlternateNameDescription'] }</span>
                    </div>
                  </div>
                  <div id="setAlternateNameIfRenameDiv" class="control-group" style="display: none">
                    <label for="groupRenameUpdateAlternateName" class="control-label">${textContainer.text['groupRenameUpdateAlternateNameLabel'] }</label>
                    <div class="controls">
                      <input type="checkbox" id="groupRenameUpdateAlternateName" name="setAlternateNameIfRename" checked="checked" value="true" /><span class="help-block">${textContainer.text['groupRenameUpdateAlternateNameDescription']}</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="groupDescription" class="control-label">${textContainer.text['groupCreateDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="groupDescription" name=description rows="3" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.description)}</textarea><span 
                        class="help-block">${textContainer.text['groupCreateDescriptionDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="groupEnabledDate" class="control-label">${textContainer.text['groupCreateEnabledDateLabel'] }</label>
                    <div class="controls">
                      <input type="datetime-local" name="enabledDate" placeholder="${textContainer.text['groupCreateDatePlaceholder'] }" value="${grouperRequestContainer.groupContainer.guiGroup.enabledDateLabel }" id="groupEnabledDate"><span class="help-block">${textContainer.text['groupCreateEnabledDateDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="groupDisabledDate" class="control-label">${textContainer.text['groupCreateDisabledDateLabel'] }</label>
                    <div class="controls">
                      <input type="datetime-local" name="disabledDate" placeholder="${textContainer.text['groupCreateDatePlaceholder'] }" value="${grouperRequestContainer.groupContainer.guiGroup.disabledDateLabel }" id="groupDisabledDate"><span class="help-block">${textContainer.text['groupCreateDisabledDateDescription'] }</span>
                    </div>
                  </div>
                  
                  <c:forEach items="${grouperRequestContainer.groupContainer.groupTypesForEdit}" var="groupTypeForEdit">
                  
                    <div class="control-group ${groupTypeForEdit.markerAttributeDefName.id}__class" 
                      style="display: ${groupTypeForEdit.initiallyVisible ? 'block' : 'none'}">
                      <label for="${groupTypeForEdit.attributeName}Id" class="control-label">
                        ${grouper:escapeHtml(groupTypeForEdit.label)}</label>
                      <div class="controls">
                        
                        <c:if test="${groupTypeForEdit.formElementType == 'TEXTFIELD'}">
                          <c:choose>
                            <c:when test="${groupTypeForEdit.attributeDefName.attributeDef.valueType == 'timestamp'}">
                              <input type="text" name="${groupTypeForEdit.configId}__name"
                                placeholder="${textContainer.text['groupCreateDatePlaceholder'] }"
                                value="${grouper:escapeHtml(groupTypeForEdit.value)}"
                                id="${groupTypeForEdit.configId}__id">
                            </c:when>
                            <c:otherwise>
                             <input type="text" name="${groupTypeForEdit.configId}__name"
                                value="${grouper:escapeHtml(groupTypeForEdit.value)}"
                                id="${groupTypeForEdit.configId}__id">
                            </c:otherwise>
                          </c:choose>
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

                        <span class="help-block">${textContainer.text['groupCreatePrivilegeDescription']}</span>
                      </div>
                    </div>
                    <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
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
                    </c:if>
                    <c:if test="${grouperRequestContainer.groupContainer.cannotAddSelfUserCanView}">
                      <div class="control-group">
                        <label for="groupCreateCannotAddSelfId" class="control-label">${textContainer.text['groupCreateCannotAddSelfLabel'] }</label>
                        <div class="controls">
                          <c:choose>
                             <c:when test="${grouperRequestContainer.groupContainer.cannotAddSelfUserCanEdit}">
                                <select name="groupCreateCannotAddSelfName"
                                  id="groupCreateCannotAddSelfId" style="width: 30em">
                                    <option value="true"
                                      ${grouperRequestContainer.groupContainer.cannotAddSelfAssignedToGroup ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['groupCreateCannotAddSelfTrue']}</option>
                                    <option value="false"
                                      ${grouperRequestContainer.groupContainer.cannotAddSelfAssignedToGroup ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['groupCreateCannotAddSelfFalse']}</option>
                                </select>
                             </c:when>
                             <c:otherwise>
                                ${grouperRequestContainer.groupContainer.cannotAddSelfAssignedToGroup ? textContainer.textEscapeXml['groupCreateCannotAddSelfTrue'] : textContainer.textEscapeXml['groupCreateCannotAddSelfFalse'] }
                             </c:otherwise>
                           </c:choose>
                            <br />
                            <span class="help-block">${textContainer.text['groupCreateCannotAddSelfDescription'] }</span>
                        </div>
                      </div>
                    </c:if>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" id="editGroupSaveButton" onclick="ajax('../app/UiV2Group.groupEditSubmit', {formIds: 'editGroupForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" class="btn btn-cancel">${textContainer.text['groupCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            
