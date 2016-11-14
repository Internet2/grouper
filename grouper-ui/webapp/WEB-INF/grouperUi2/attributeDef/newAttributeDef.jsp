<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['attributeDefNewBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['attributeDefNewTitle'] }</h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="group-search-label">${textContainer.text['attributeDefCreateSearchForFolderTitle'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="${textContainer.textEscapeXml['attributeDefCreateSearchPlaceholder'] }" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchGroupFormSubmit', {formIds: 'stemSearchFormId'}); return false;">${textContainer.text['attributeDefCreateSearchButton'] }</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['attributeDefCreateSearchClose'] }</button>
                  </div>
                </div>
                <form id="addAttributeDefForm" class="form-horizontal">
                  <div class="control-group">
                    <label for="folder-path" class="control-label">${textContainer.text['attributeDefCreateFolderLabel'] }</label>
                    <div class="controls">
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em" 
                        filterOperation="../app/UiV2Stem.createAttributeDefParentFolderFilter"
                        value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.objectStemId)}"
                        />
                      <span class="help-block">${textContainer.text['attributeDefCreateIntoFolderDescription'] }</span>
                    
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="attributeDefId" class="control-label">${textContainer.text['attributeDefCreateIdLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="attributeDefId" name="extension"  /> 
                      <span class="help-block">${textContainer.text['attributeDefCreateIdDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="attributeDefDescription" class="control-label">${textContainer.text['attributeDefCreateDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="attributeDefDescription" name=description rows="3" cols="40" class="input-block-level"></textarea><span class="help-block">${textContainer.text['attributeDefCreateDescriptionDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="attributeDefType" class="control-label">${textContainer.text['attributeDefCreateTypeLabel'] }</label>
                    <div class="controls">
                      <select name="attributeDefType" id="attributeDefTypeId" 
                          onchange="ajax('../app/UiV2AttributeDef.attributeDefTypeChanged', {formIds: 'addAttributeDefForm'}); return false;">
                        <option value="" ></option>
                        <option value="attr" >${textContainer.text['attributeDefType_attr']}</option>
                        <option value="service" >${textContainer.text['attributeDefType_service']}</option>
                        <option value="limit" >${textContainer.text['attributeDefType_limit']}</option>
                        <option value="perm" >${textContainer.text['attributeDefType_perm']}</option>
                        <option value="type" >${textContainer.text['attributeDefType_type']}</option>
                      </select>
                    
                      <span class="help-block">${textContainer.text['attributeDefCreateTypeDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="attributeDefValueType" class="control-label" id="assignToLabelId">${textContainer.text['attributeDefLabelAssignTo'] }</label>
                    <div class="controls">
                       <table class="attributeDefAssignToTable">
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToAttributeDef" id="attributeDefToEditAssignToAttributeDefId"
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToAttributeDefDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.attributeDef']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToAttributeDefAssign" id="attributeDefToEditAssignToAttributeDefAssignId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToAttributeDefAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.attributeDefAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToStem" id="attributeDefToEditAssignToStemId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToStemDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.stem']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToStemAssign" id="attributeDefToEditAssignToStemAssignId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToStemAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.stemAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToGroup" id="attributeDefToEditAssignToGroupId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToGroupDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.group']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToGroupAssign" id="attributeDefToEditAssignToGroupAssignId" 
                               class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToGroupAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.groupAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToMember" id="attributeDefToEditAssignToMemberId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToMemberDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.member']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToMemberAssign" id="attributeDefToEditAssignToMemberAssignId"
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToMemberAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.memberAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToMembership" id="attributeDefToEditAssignToMembershipId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToEffMembershipDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.membership']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToMembershipAssign" id="attributeDefToEditAssignToMembershipAssignId"
                               class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToEffMembershipAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.membershipAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToImmediateMembership" id="attributeDefToEditAssignToImmediateMembershipId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToImmMembershipDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.immediateMembership']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToImmediateMembershipAssign" id="attributeDefToEditAssignToImmediateMembershipAssignId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${attributeUpdateRequestContainer.attributeDefToEdit.assignToImmMembershipAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.immediateMembershipAssign']}
                             </span>
                           </td>
                         </tr>
                       </table>
                      <br />
                      <span class="help-block">${textContainer.text['attributeDefLabelAssignToDescription'] }</span>
                    </div>
                  </div>                  
                  
                  <div class="control-group">
                    <label for="attributeDefMultiAssignable" class="control-label">${textContainer.text['attributeDefMultiAssignable'] }</label>
                    <div class="controls">
                    
                      <input type="checkbox" name="attributeDefMultiAssignable" value="true" />
                      <span class="help-block">${textContainer.text['attributeDefMultiAssignableDescription'] }</span>
                    </div>
                  </div>
                  
                  
                  <div class="control-group">
                    <label for="attributeDefValueType" class="control-label">${textContainer.text['attributeDefCreateValueTypeLabel'] }</label>
                    <div class="controls">
                      <select name="attributeDefValueType" id="attributeDefValueTypeId"
                          onchange="ajax('../app/UiV2AttributeDef.attributeDefValueTypeChanged', {formIds: 'addAttributeDefForm'}); return false;">
                      >
                        <option value="" ></option>
             <option value="marker" >${textContainer.text['attributeDefValueType_marker'] }</option>
             <option value="floating" >${textContainer.text['attributeDefValueType_floating'] }</option>
             <option value="integer" >${textContainer.text['attributeDefValueType_integer'] }</option>
             <option value="memberId" >${textContainer.text['attributeDefValueType_memberId'] }</option>
             <option value="string" >${textContainer.text['attributeDefValueType_string'] }</option>
             <option value="timestamp" >${textContainer.text['attributeDefValueType_timestamp'] }</option>
                      </select> 
                    
                      <span class="help-block">${textContainer.text['attributeDefCreateValueTypeDescription'] }</span>
                    </div>
                  </div>

                  <div class="control-group multiAssignFieldClass">
                    <label for="attributeDefMultiValued" class="control-label">${textContainer.text['attributeDefMultiValued'] }</label>
                    <div class="controls">
                    
                      <input type="checkbox" name="attributeDefMultiValued" value="true" />
                      <span class="help-block">${textContainer.text['attributeDefMultiValuedDescription'] }</span>
                    </div>
                  </div>
                  
                  <p class="shownAdvancedProperties"><a href="#" 
                    onclick="$('.hiddenAdvancedProperties').show('slow'); $('.shownAdvancedProperties').hide('slow'); return false;" 
                    >${textContainer.text['attributeDefCreateAdvanced'] } <i class="fa fa-angle-down"></i></a></p>
                  <p class="hiddenAdvancedProperties" style="display: none"
                    onclick="$('.hiddenAdvancedProperties').hide('slow'); $('.shownAdvancedProperties').show('slow'); return false;" 
                    ><a href="#" >${textContainer.text['attributeDefCreateHideAdvanced'] } <i class="fa fa-angle-up"></i></a></p>
                  <div class="hiddenAdvancedProperties" style="display: none">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['attributeDefCreateAssignPrivilegesToEveryone'] }</label>
                      <div class="controls">

                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrAdmins" value="true" 
                            ${grouperRequestContainer.attributeDefContainer.configDefaultAttributeDefsCreateGrantAllAdmin ? 'checked="checked"' : '' }
                          />${textContainer.text['priv.attrAdminUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrUpdaters" value="true"
                            ${grouperRequestContainer.attributeDefContainer.configDefaultAttributeDefsCreateGrantAllUpdate ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrUpdateUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrReaders" value="true"
                            ${grouperRequestContainer.attributeDefContainer.configDefaultAttributeDefsCreateGrantAllRead ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrReadUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrViewers" value="true"
                            ${grouperRequestContainer.attributeDefContainer.configDefaultAttributeDefsCreateGrantAllView ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrViewUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrOptins" value="true"
                            ${grouperRequestContainer.attributeDefContainer.configDefaultAttributeDefsCreateGrantAllOptin ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrOptinUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrOptouts" value="true"
                            ${grouperRequestContainer.attributeDefContainer.configDefaultAttributeDefsCreateGrantAllOptout ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrOptoutUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrDefAttrReaders" value="true"
                            ${grouperRequestContainer.attributeDefContainer.configDefaultAttributeDefsCreateGrantAllAttrRead ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrDefAttrReadUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrDefAttrUpdaters" value="true"
                            ${grouperRequestContainer.attributeDefContainer.configDefaultAttributeDefsCreateGrantAllAttrUpdate ? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrDefAttrUpdateUpper'] }
                        </label>

                        <span class="help-block">${textContainer.text['attributeDefCreatePrivilegeDescription']}</span>
                      </div>
                    </div>
                    <%-- div class="control-group">
                      <label for="group-type" class="control-label">${textContainer.text['attributeDefCreateTypeLabel'] }</label>
                      <div class="controls">
                        <label class="radio">
                          <input type="radio" name="groupType" id="group-type-group" value="group" checked>${textContainer.text['groupCreateTypeGroup'] }
                        </label>
                        <label class="radio">
                          <input type="radio" name="groupType" id="group-type-role" value="role">${textContainer.text['groupCreateTypeRole'] }
                        </label><span class="help-block">${textContainer.text['groupCreateTypeDescription'] }</span>
                      </div>
                    </div --%>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2AttributeDef.newAttributeDefSubmit', {formIds: 'addAttributeDefForm'}); return false;">${textContainer.text['attributeDefCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');" class="btn btn-cancel" role="button">${textContainer.text['attributeDefCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            