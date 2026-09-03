<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('attributeDefEditTitle', grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.displayName)}


            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i aria-hidden="true" class="fa fa-cog"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.extension)}
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
                    <label for="attributeDefTypeId" class="control-label">${textContainer.text['attributeDefCreateTypeLabel'] }</label>
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
                    <span class="control-label" id="assignToLabelId">${textContainer.text['attributeDefLabelAssignTo'] }</span>
                    <div class="controls">
                       <table class="attributeDefAssignToTable">
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToAttributeDef" id="attributeDefToEditAssignToAttributeDefId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.attributeDef']}"
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToAttributeDefDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox" for="attributeDefToEditAssignToAttributeDefId">
                             ${textContainer.text['attributeDefAssignTo.attributeDef']}
                             </label>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToAttributeDefAssign" id="attributeDefToEditAssignToAttributeDefAssignId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.attributeDefAssign']}" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               onclick="showHideMarkerSection()"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToAttributeDefAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox" for="attributeDefToEditAssignToAttributeDefAssignId">
                             ${textContainer.text['attributeDefAssignTo.attributeDefAssign']}
                             </label>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToStem" id="attributeDefToEditAssignToStemId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.stem']}" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToStemDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox" for="attributeDefToEditAssignToStemId">
                             ${textContainer.text['attributeDefAssignTo.stem']}
                             </label>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToStemAssign" id="attributeDefToEditAssignToStemAssignId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.stemAssign']}" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               onclick="showHideMarkerSection()"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToStemAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox" for="attributeDefToEditAssignToStemAssignId">
                             ${textContainer.text['attributeDefAssignTo.stemAssign']}
                             </label>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToGroup" id="attributeDefToEditAssignToGroupId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.group']}" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToGroupDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox" for="attributeDefToEditAssignToGroupId">
                             ${textContainer.text['attributeDefAssignTo.group']}
                             </label>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToGroupAssign" id="attributeDefToEditAssignToGroupAssignId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.groupAssign']}" 
                               class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               onclick="showHideMarkerSection()"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToGroupAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox" for="attributeDefToEditAssignToGroupAssignId">
                             ${textContainer.text['attributeDefAssignTo.groupAssign']}
                             </label>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToMember" id="attributeDefToEditAssignToMemberId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.member']}" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToMemberDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox" for="attributeDefToEditAssignToMemberId">
                             ${textContainer.text['attributeDefAssignTo.member']}
                             </label>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToMemberAssign" id="attributeDefToEditAssignToMemberAssignId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.memberAssign']}"
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               onclick="showHideMarkerSection()"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToMemberAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox" for="attributeDefToEditAssignToMemberAssignId">
                             ${textContainer.text['attributeDefAssignTo.memberAssign']}
                             </label>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToMembership" id="attributeDefToEditAssignToMembershipId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.membership']}" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToEffMembershipDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToServiceHideCheckbox" for="attributeDefToEditAssignToMembershipId">
                             ${textContainer.text['attributeDefAssignTo.membership']}
                             </label>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToMembershipAssign" id="attributeDefToEditAssignToMembershipAssignId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.membershipAssign']}"
                               class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               onclick="showHideMarkerSection()"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToEffMembershipAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox" for="attributeDefToEditAssignToMembershipAssignId">
                             ${textContainer.text['attributeDefAssignTo.membershipAssign']}
                             </label>
                           </td>
                         </tr>
                         <tr>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToLeft">
                             <input type="checkbox" name="attributeDefToEditAssignToImmediateMembership" id="attributeDefToEditAssignToImmediateMembershipId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.immediateMembership']}" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToImmMembershipDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox" for="attributeDefToEditAssignToImmediateMembershipId">
                             ${textContainer.text['attributeDefAssignTo.immediateMembership']}
                             </label>
                           </td>
                           <td class="attributeAssignAssignToTd attributeAssignAssignToRight">
                             <input type="checkbox" name="attributeDefToEditAssignToImmediateMembershipAssign" id="attributeDefToEditAssignToImmediateMembershipAssignId" aria-label="${textContainer.textEscapeXml['attributeDefAssignTo.immediateMembershipAssign']}" 
                               class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox"
                               onclick="showHideMarkerSection()"
                               value="true" ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToImmMembershipAssnDb == 'T' ? 'checked="checked"' : '' } />
                             <label class="assignToCheckbox assignToLimitHideCheckbox assignToPermHideCheckbox assignToServiceHideCheckbox assignToTypeHideCheckbox" for="attributeDefToEditAssignToImmediateMembershipAssignId">
                             ${textContainer.text['attributeDefAssignTo.immediateMembershipAssign']}
                             </label>
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
                    
                      <input type="checkbox" name="attributeDefMultiAssignable" id="attributeDefMultiAssignable" value="true"
                         ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.multiAssignableDb == 'T' ? 'checked="checked"' : '' }
                      />
                      <span class="help-block">${textContainer.text['attributeDefMultiAssignableDescription'] }</span>
                    </div>
                  </div>
                  
                  
                  <div class="control-group">
                    <label for="attributeDefValueTypeId" class="control-label">${textContainer.text['attributeDefCreateValueTypeLabel'] }</label>
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
                    
                      <input type="checkbox" name="attributeDefMultiValued" id="attributeDefMultiValued"
                        ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.multiValuedDb == 'T' ? 'checked="checked"' : '' }
                      value="true" />
                      <span class="help-block">${textContainer.text['attributeDefMultiValuedDescription'] }</span>
                    </div>
                  </div>
                  
                  <div class="markerScopeSection" style="display: ${grouperRequestContainer.attributeDefContainer.showAttributeDefMarkerSection == true ? 'block' : 'none'}">
                    <div class="control-group manageMarkerScopeFieldClass">
                      <label for="manageMarkerScopeId" class="control-label">${textContainer.text['attributeDefManageMarkerScope'] }</label>
                      <div class="controls">
                        <input type="checkbox" id="manageMarkerScopeId" name="attributeDefManageMarkerScope"
                          onclick="if($(this).is(':checked')) {$('.markerAttributeDefNameClass').show(300);} else { $('.markerAttributeDefNameClass').hide(200); }"
                          ${grouperRequestContainer.attributeDefContainer.attributeDefScope != null ? 'checked="checked"' : '' }
                          value="true" />
                        <span class="help-block">${textContainer.text['attributeDefManageMarkerScopeDescription'] }</span>
                      </div>
                    </div>
                    
                     <div class="control-group markerAttributeDefNameClass"
                      style="display: ${grouperRequestContainer.attributeDefContainer.attributeDefScope != null ? 'block' : 'none'}">
                      <label for="markerAttributeDefNameId" id="markerAttributeDefNameLabelId" class="control-label">${textContainer.text['markerAttributeDefName'] }</label>
                      <div class="controls">
                        <input type="text" id="markerAttributeDefNameId" name="markerAttributeDefName" 
                          value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.attributeDefScope.scopeString)}" /> 
                        <span class="help-block">${textContainer.text['markerAttributeDefNameDescription'] }</span>
                      </div>
                    </div>
                    
                  </div>
                  
                  <p class="shownAdvancedProperties"><a href="#" 
                    onclick="$('.hiddenAdvancedProperties').show('slow'); $('.shownAdvancedProperties').hide('slow'); return false;" 
                    >${textContainer.text['attributeDefCreateAdvanced'] } <i aria-hidden="true" class="fa fa-angle-down"></i></a></p>
                  <p class="hiddenAdvancedProperties" style="display: none"
                    onclick="$('.hiddenAdvancedProperties').hide('slow'); $('.shownAdvancedProperties').show('slow'); return false;" 
                    ><a href="#" >${textContainer.text['attributeDefCreateHideAdvanced'] } <i aria-hidden="true" class="fa fa-angle-up"></i></a></p>
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
                  <div class="form-actions"><button type="button" class="btn btn-primary" onclick="ajax('../app/UiV2AttributeDef.attributeDefEditSubmit', {formIds: 'editAttributeDefForm'}); return false;">${textContainer.text['attributeDefEditSaveButton'] }</button> 
                  <button type="button" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}');" class="btn btn-cancel">${textContainer.text['attributeDefCreateCancelButton'] }</button></div>
                </form>
              </div>
            </div>
    
<script type="text/javascript">
      function showHideMarkerSection() {
        var attributeDefAssignIdChecked = $("#attributeDefToEditAssignToAttributeDefAssignId").is(":checked")
        var stemAssignIdChecked = $("#attributeDefToEditAssignToStemAssignId").is(":checked")
        var groupAssignIdChecked = $("#attributeDefToEditAssignToGroupAssignId").is(":checked")
        var memberAssignIdChecked = $("#attributeDefToEditAssignToMemberAssignId").is(":checked")
        var membershipAssignIdChecked = $("#attributeDefToEditAssignToMembershipAssignId").is(":checked")
        var immediateMembershipAssignIdChecked = $("#attributeDefToEditAssignToImmediateMembershipAssignId").is(":checked")
        
        if (attributeDefAssignIdChecked || stemAssignIdChecked || groupAssignIdChecked || memberAssignIdChecked
         ||	membershipAssignIdChecked || immediateMembershipAssignIdChecked) {
        	$('.markerScopeSection').show(300);
        } else {
        	$('.markerScopeSection').hide(200);
        }
        
      }
</script>        
            
            
