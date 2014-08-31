<%@ include file="../assetsJsp/commonTaglib.jsp"%>


            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-cog"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.extension)}
                <br /><small>${textContainer.text['attributeDefEditTitle'] }</small></h1>
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                <form id="editAttributeDefForm" class="form-horizontal">
                
                  <input type="hidden" name="attributeDefId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" />
                
                  <div class="control-group">
                    <label for="attributeDefId" class="control-label">${textContainer.text['attributeDefCreateIdLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="attributeDefId" name="extension" 
                        value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.extension)}" /> 
                      <span class="help-block">${textContainer.text['attributeDefCreateIdDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="attributeDefDescription" class="control-label">${textContainer.text['attributeDefCreateDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="attributeDefDescription" name=description rows="3" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.description)}</textarea><span class="help-block">${textContainer.text['attributeDefCreateDescriptionDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="attributeDefType" class="control-label">${textContainer.text['attributeDefCreateTypeLabel'] }</label>
                    <div class="controls">
                      <select name="attributeDefType" id="attributeDefTypeId" 
                          onchange="ajax('../app/UiV2AttributeDef.attributeDefTypeChanged', {formIds: 'editAttributeDefForm'}); return false;">
                        <option value="" ></option>
                        <option value="attr" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'attr' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefType_attr']}</option>
                        <option value="service" ${ (grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'domain' || grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'service' ) ? 'selected="selected"' : '' } >${textContainer.text['attributeDefType_service']}</option>
                        <option value="limit" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'limit' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefType_limit']}</option>
                        <option value="perm" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'perm' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefType_perm']}</option>
                        <option value="type" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'type' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefType_type']}</option>
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
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToAttributeDefDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.attributeDef']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToAttributeDefAssign" id="attributeDefToEditAssignToAttributeDefAssignId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToAttributeDefAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.attributeDefAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToStem" id="attributeDefToEditAssignToStemId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToStemDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.stem']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToStemAssign" id="attributeDefToEditAssignToStemAssignId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToStemAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.stemAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToGroup" id="attributeDefToEditAssignToGroupId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToGroupDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.group']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToGroupAssign" id="attributeDefToEditAssignToGroupAssignId" 
                               class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToGroupAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.groupAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToMember" id="attributeDefToEditAssignToMemberId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToMemberDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.member']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToMemberAssign" id="attributeDefToEditAssignToMemberAssignId"
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToMemberAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.memberAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToMembership" id="attributeDefToEditAssignToMembershipId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToEffMembershipDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.membership']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToMembershipAssign" id="attributeDefToEditAssignToMembershipAssignId"
                               class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToEffMembershipAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.membershipAssign']}
                             </span>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToImmediateMembership" id="attributeDefToEditAssignToImmediateMembershipId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToImmMembershipDb == 'T' ? 'checked="checked"' : '' } />
                             <span class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox">
                             ${textContainer.text['attributeDefAssignTo.immediateMembership']}
                             </span>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToImmediateMembershipAssign" id="attributeDefToEditAssignToImmediateMembershipAssignId" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToImmMembershipAssnDb == 'T' ? 'checked="checked"' : '' } />
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
                    
                      <input type="checkbox" name="attributeDefMultiAssignable" value="true" 
                         ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.multiAssignableDb == 'T' ? 'checked="checked"' : '' }
                      />
                      <span class="help-block">${textContainer.text['attributeDefMultiAssignableDescription'] }</span>
                    </div>
                  </div>
                  
                  
                  <div class="control-group">
                    <label for="attributeDefValueType" class="control-label">${textContainer.text['attributeDefCreateValueTypeLabel'] }</label>
                    <div class="controls">
                      <select name="attributeDefValueType" id="attributeDefValueTypeId"
                          onchange="ajax('../app/UiV2AttributeDef.attributeDefValueTypeChanged', {formIds: 'editAttributeDefForm'}); return false;">
                      >
             <option value="marker" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'marker' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefValueType_marker'] }</option>
             <option value="floating" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'floating' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefValueType_floating'] }</option>
             <option value="integer" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'integer' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefValueType_integer'] }</option>
             <option value="memberId" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'memberId' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefValueType_memberId'] }</option>
             <option value="string" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'string' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefValueType_string'] }</option>
             <option value="timestamp" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'timestamp' ? 'selected="selected"' : '' } >${textContainer.text['attributeDefValueType_timestamp'] }</option>
                      </select> 
                    
                      <span class="help-block">${textContainer.text['attributeDefCreateValueTypeDescription'] }</span>
                    </div>
                  </div>

                  <div class="control-group multiAssignFieldClass">
                    <label for="attributeDefMultiValued" class="control-label">${textContainer.text['attributeDefMultiValued'] }</label>
                    <div class="controls">
                    
                      <input type="checkbox" name="attributeDefMultiValued" 
                        ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.multiValuedDb == 'T' ? 'checked="checked"' : '' }
                      value="true" />
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
                            ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.grantAllAdmin? 'checked="checked"' : '' }
                          />${textContainer.text['priv.attrAdminUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrUpdaters" value="true"
                            ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.grantAllUpdate? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrUpdateUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrReaders" value="true"
                            ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.grantAllRead? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrReadUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrViewers" value="true"
                            ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.grantAllView? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrViewUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrOptins" value="true"
                            ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.grantAllOptin? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrOptinUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrOptouts" value="true"
                            ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.grantAllOptout? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrOptoutUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrDefAttrReaders" value="true"
                            ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.grantAllAttrRead? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrDefAttrReadUpper'] }
                        </label>
                        <label class="checkbox inline">
                          <input type="checkbox" name="privileges_attrDefAttrUpdaters" value="true"
                            ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.grantAllAttrUpdate? 'checked="checked"' : '' }                          
                          />${textContainer.text['priv.attrDefAttrUpdateUpper'] }
                        </label>

                        <span class="help-block">${textContainer.text['attributeDefCreatePrivilegeDescription']}</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2AttributeDef.attributeDefEditSubmit', {formIds: 'editAttributeDefForm'}); return false;">${textContainer.text['attributeDefEditSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}');" class="btn btn-cancel">${textContainer.text['attributeDefCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            