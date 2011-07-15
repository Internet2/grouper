<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionUpdate/simplePermissionAssignments.jsp -->


<c:if test="${permissionUpdateRequestContainer.assignmentStatusMessage != null}">
  <div class="noteMessage" id="permissionAssignMessage">${permissionUpdateRequestContainer.assignmentStatusMessage}</div>
  <script>
    //hide this after it shows for a while
    function hidePermissionAssignMessage() {
      $("#permissionAssignMessage").hide('slow');
    }
    setTimeout("hidePermissionAssignMessage()", 5000);
  </script>
</c:if>

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simplePermissionUpdate.assignAssignments" />

  <div class="sectionBody">
    <form id="attributePermissionsFormId" name="attributePermissionsFormName" onsubmit="return false;" >

      <%-- signify stash the type of assignment --%>
      <input type="hidden" name="permissionAssignType" 
                value="${permissionUpdateRequestContainer.permissionType.name}" />

      <%-- message if no permissions --%>  
      <c:choose>
        <c:when test="${fn:length(permissionUpdateRequestContainer.guiPermissionEntryActionsContainers) == 0}">
          <grouper:message key="simplePermissionUpdate.assignNoResultsFound" />
        
        </c:when>
        <c:otherwise>
          <%-- paging summary shows which records, and page size --%>
          <%-- div class="pagingSummary">
            <grouper:message key="simplePermissionUpdate.pagingLabelPrefix" />
            <grouper:paging 
              refreshOperation="SimplePermissionUpdate.permissionEditPanelPrivileges?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&showIndirectPrivileges=${attributeUpdateRequestContainer.showIndirectPrivilegesComputed}" 
              showSummaryOrButtons="true" pagingName="simplePermissionUpdateAssignments"  />
              
          </div --%>
    
          <table cellspacing="2" class="formTable" width="700">
            <c:forEach items="${permissionUpdateRequestContainer.guiPermissionEntryActionsContainers}" var="guiPermissionEntryActionsContainer">
              <c:set var="row" value="0" />
              <c:forEach items="${guiPermissionEntryActionsContainer.guiPermissionEntryContainers}" var="guiPermissionEntryContainer">
              
              
              
              
                <c:if test="${permissionUpdateRequestContainer.showHeader[row]}">
                  <tr>
                    <c:choose>
                      <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
                        <th></th>
                      </c:when>
                    </c:choose>                    
                    <th></th>
                    <th></th>
                    <th style="background-color: #DFEFF4;" colspan="${permissionUpdateRequestContainer.allActionsSize }"><grouper:message key="simplePermissionUpdate.assignHeaderActions" /></th>                    
                    <th></th>
                  </tr>
                  <tr>
                    
                    <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                      <grouper:message key="simplePermissionUpdate.assignHeaderOwnerRole" />
                    </th>
                    <c:choose>
                      <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
                        <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                          <grouper:message key="simplePermissionUpdate.assignHeaderOwnerMember" />
                        </th>
                      </c:when>
                    </c:choose>
                    <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                      <grouper:message key="simplePermissionUpdate.assignHeaderPermissionResource" />
                    </th>
                    <c:forEach items="${permissionUpdateRequestContainer.allActions}" var="action">
                      <th class="privilegeHeader" style="white-space: nowrap;">
                        ${grouper:escapeHtml(action)}
                      </th>
                    </c:forEach>
                    <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                      <grouper:message key="simplePermissionUpdate.assignHeaderPermissionDefinition" />
                    </th>
                  </tr>
                </c:if>
  
                <tr  ${row % 2 == 1 ? 'class="alternate"' : ''} style="vertical-align: top">
                  <td style="white-space: nowrap;">
                    <grouper:message valueTooltip="${grouper:escapeHtml(guiPermissionEntryContainer.role.displayName)}" 
                       value="${grouper:escapeHtml(guiPermissionEntryContainer.role.displayExtension)}"  />
                  </td>
                  <c:choose>
                    <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
                      <td style="white-space: nowrap;">
                        <grouper:message valueTooltip="${grouper:escapeHtml(guiPermissionEntryContainer.screenLabelLongIfDifferent)}" 
                           value="${grouper:escapeHtml(guiPermissionEntryContainer.screenLabelShort)}"  />
                      </td>
                    </c:when>
                  </c:choose>
                  <td style="white-space: nowrap;">
                    <grouper:message value="${grouper:escapeHtml(guiPermissionEntryContainer.permissionResource.displayExtension)}" 
                      valueTooltip="${grouper:escapeJavascript(guiPermissionEntryContainer.permissionResource.displayName)}" />
                  </td>
                  
                  <c:forEach items="${permissionUpdateRequestContainer.allActions}" var="action">
                    <td align="center" style="white-space: nowrap;">
                      <%-- see if this row has this action, if not then blank --%>
                      <c:if test="${guiPermissionEntryActionsContainer.showAction[action]}">
                        <c:set var="guiPermissionEntry" value="${guiPermissionEntryContainer.actionToGuiPermissionEntryMap[action]}" />
                        <c:set var="guiPermissionEntryChecked" value="${guiPermissionEntry.immediate}" />
                        <c:set var="guiPermissionId" value="${guiPermissionEntryContainer.role.id}__${guiPermissionEntryContainer.memberId}__${guiPermissionEntryContainer.permissionResource.id}__${action}" />
                        <%-- keep the previous state so we know what the user changed --%>
                        <input  name="previousState__${guiPermissionId}"
                          type="hidden" value="${guiPermissionEntryChecked ? 'true' : 'false'}" />
                        <c:choose>
                          <c:when test="${guiPermissionEntry.immediate && !guiPermissionEntry.effective}"
                            ><c:set var="tooltipName" value="simplePermissionAssign.immediateTooltip" /></c:when
                            ><c:when test="${!guiPermissionEntry.immediate && guiPermissionEntry.effective}"
                            ><c:set var="tooltipName" value="simplePermissionAssign.effectiveTooltip" /></c:when
                            ><c:otherwise><c:set var="tooltipName" value="simplePermissionAssign.immediateAndEffectiveTooltip" /></c:otherwise>
                        </c:choose>
                        <input  style="margin-right: -3px" name="permissionCheckbox__${guiPermissionId}" value="true"
                          type="checkbox" ${guiPermissionEntryChecked ? 'checked="checked"' : '' } 
                        /><c:set var="confirmNavName" value="simplePermissionUpdate.permissionImageConfirm${guiPermissionEntryChecked ? 'Allow' : 'Deny'}" />
                        <a href="#" style="margin-left: 5px"
                        onclick="if (confirm('${grouper:message(confirmNavName, true, true) }')) {ajax('../app/SimplePermissionUpdate.permissionPanelImageClick?guiPermissionId=${guiPermissionId}&allow=${guiPermissionEntryChecked ? 'false' : 'true'}', {formIds: 'attributePermissionsFormId'});} return false;"
                        ><c:choose>
                          <c:when test="${guiPermissionEntry.allowed}"
                          ><img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
                            onmouseover="Tip('${grouper:escapeJavascript(navMap[tooltipName])}')" 
                            onmouseout="UnTip()"
                            /></c:when>
                          <c:otherwise><img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" 
                              onmouseover="Tip('${grouper:escapeJavascript(navMap['simplePermissionAssign.unassignedTooltip'])}')" 
                              onmouseout="UnTip()"
                            /></c:otherwise>
                        
                        </c:choose></a>
                        <a class="permissionMenuButton" href="#"
                          ><img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                          id="permissionMenuButton_${guiPermissionId}__${permissionUpdateRequestContainer.permissionType.name}" 
                          alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/></a>
                        
                      </c:if>
                    </td>
                  </c:forEach>
                  <td>
                    <grouper:message value="${grouper:escapeHtml(guiPermissionEntryContainer.permissionDefinition.extension)}" 
                      valueTooltip="${grouper:escapeJavascript(guiPermissionEntryContainer.permissionDefinition.name)}" />
                  </td>
              </tr>
                  
  <%--
                  <td style="white-space: nowrap;">
  --%>
                    <%-- loop through the values --%>
  <%--
                    <c:set var="valueRow" value="0" />
                
                    
                    <c:forEach items="${guiAttributeAssign.attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">
  --%>                  
                      <%-- we need a newline before non-first rows --%>
  <%--
                      <c:if test="${valueRow != 0}">
                        <br />
                      </c:if>
  
                      <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeUpdate.assignValueDeleteConfirm', true, true)}')) {ajax('SimpleAttributeUpdate.assignValueDelete?attributeAssignId=${guiAttributeAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'});} return false;" 
                        ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                        alt="${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, false)}"
                        onmouseover="Tip('${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, true)}')" 
                        onmouseout="UnTip()"/></a>
  
                      <a href="#" onclick="ajax('SimpleAttributeUpdate.assignValueEdit?attributeAssignId=${guiAttributeAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'}); return false;" 
                        ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                        alt="${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, false)}"
                        onmouseover="Tip('${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, true)}')" 
                        onmouseout="UnTip()"/></a>
                      
                      ${grouper:escapeHtml(attributeAssignValue.valueFriendly)}
                      
                      <c:set var="valueRow" value="${valueRow + 1}" />
                    </c:forEach>
                  
                  </td>
                  <td>
                    <grouper:message value="${grouper:escapeJavascript(guiAttributeAssign.attributeAssign.attributeDef.extension)}" 
                      valueTooltip="${grouper:escapeJavascript(guiAttributeAssign.attributeAssign.attributeDef.name)}" />
                  </td>
                  <td>${grouper:abbreviate(guiAttributeAssign.attributeAssign.id, 8, true, true)}</td>
                </tr>
                
                
  --%>
            <c:forEach items="${guiPermissionEntryContainer.guiPermissionLimitBeanContainers}" var="guiPermissionLimitBeanContainer">
              <tr  ${row % 2 == 1 ? 'class="alternate"' : ''} style="vertical-align: top">

                <td colspan="${permissionUpdateRequestContainer.permissionType.name == 'role_subject' ? '2' : '1'}" style="white-space: nowrap; padding-left: 1em">
                  <span class="simpleMembershipUpdateDisabled"><grouper:message key="simplePermissionUpdate.limitLabel" /></span>
                  <c:choose>
                    <c:when test="${guiPermissionLimitBeanContainer.immediate}" >
                      <a href="#" onclick="if (confirm('${grouper:message('simplePermissionUpdate.limitDeleteConfirm', true, true)}')) {ajax('SimplePermissionUpdate.limitDelete?limitAssignId=${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}', {formIds: 'simplePermissionFilterForm'});} return false;" 
                            ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                            alt="${grouper:message('simplePermissionUpdate.deleteLimitAlt', true, false)}"
                            onmouseover="Tip('${grouper:message('simplePermissionUpdate.deleteLimitAlt', true, true)}')" 
                            onmouseout="UnTip()"/></a>

                      <a href="#" onclick="ajax('SimplePermissionUpdate.assignLimitEdit?limitAssignId=${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}', {formIds: 'simplePermissionFilterForm'}); return false;" 
                        ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                        alt="${grouper:message('simplePermissionUpdate.editLimitAlt', true, false)}"
                        onmouseover="Tip('${grouper:message('simplePermissionUpdate.editLimitAlt', true, true)}')" 
                        onmouseout="UnTip()"/></a>

                      <a class="limitMenuButton" href="#"
                        ><img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                        id="limitMenuButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/></a>

                    </c:when>
                    <c:otherwise>
                      <img src="../../grouperExternal/public/assets/images/spacer.gif" height="14px" border="0" 
                            alt=""/>
                      <img src="../../grouperExternal/public/assets/images/spacer.gif" height="14px" border="0" 
                            alt=""/>
                      <img src="../../grouperExternal/public/assets/images/spacer.gif" height="14px" border="0" 
                            alt=""/>
                    </c:otherwise>
                  </c:choose>
                  <span class="simpleMembershipUpdateDisabled">
                  <c:choose>
                    <c:when test="${guiPermissionLimitBeanContainer.hasMultipleActions}"><grouper:message key="simplePermissionUpdate.limitActions" /></c:when>
                    <c:otherwise><grouper:message key="simplePermissionUpdate.limitAction" /></c:otherwise>
                  </c:choose>
                  </span>
                        ${guiPermissionLimitBeanContainer.actionsCommaSeparated}
                </td>
                <td style="white-space: nowrap;">
                  <grouper:message value="${grouper:escapeHtml(guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.attributeDefName.displayExtension)}" 
                    valueTooltip="${grouper:escapeHtml(guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.attributeDefName.displayName)}" />
                </td>
                <td style="white-space: nowrap;" colspan="${permissionUpdateRequestContainer.allActionsSize}">
                  <c:if test="${guiPermissionLimitBeanContainer.hasValues}">

                    <%-- loop through the values --%>
                    <c:set var="valueRow" value="0" />
                
                    
                    <c:forEach items="${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssignValues}" var="limitAssignValue">
                    
                      <%-- we need a newline before non-first rows --%>
                      <c:if test="${valueRow != 0}">
                        <br />
                      </c:if>
  
                      <c:choose>
                        <c:when test="${guiPermissionLimitBeanContainer.immediate}" >
                          <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeUpdate.assignValueDeleteConfirm', true, true)}')) {ajax('SimpleAttributeUpdate.assignValueDelete?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'});} return false;" 
                            ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                            alt="${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, false)}"
                            onmouseover="Tip('${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, true)}')" 
                            onmouseout="UnTip()"/></a>
      
                          <a href="#" onclick="ajax('SimpleAttributeUpdate.assignValueEdit?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}&attributeAssignValueId=${guiAttributeAssignAssign.attributeAssign.id}', {formIds: 'simpleAttributeFilterForm'}); return false;" 
                            ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                            alt="${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, false)}"
                            onmouseover="Tip('${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, true)}')" 
                            onmouseout="UnTip()"/></a>
                        </c:when>
                        <c:otherwise>
                          <img src="../../grouperExternal/public/assets/images/spacer.gif" height="14px" border="0" 
                                alt=""/>
                          <img src="../../grouperExternal/public/assets/images/spacer.gif" height="14px" border="0" 
                                alt=""/>
                        </c:otherwise>
                      </c:choose>
                      
                      <span class="simpleMembershipUpdateDisabled"><grouper:message key="simplePermissionUpdate.limitValueLabel" /></span>
                      ${grouper:escapeHtml(limitAssignValue.valueFriendly)}
                      
                      <c:set var="valueRow" value="${valueRow + 1}" />
                      
                    </c:forEach>
                  
                  
                  
                  </c:if>
                </td>
                <td style="white-space: nowrap;">
                
                  <span class="simpleMembershipUpdateDisabled"><grouper:message key="simplePermissionUpdate.limitAssignedToLabel" 
                    valueTooltip="${guiPermissionLimitBeanContainer.assignedToTooltip}" /></span>
                
                
                </td>
              </tr>
            </c:forEach>
                  <%-- filter out results which dont match the enabled/disabled filter --%>
  <%--
                  <c:if test="${attributeUpdateRequestContainer.enabledDisabled == null || (attributeUpdateRequestContainer.enabledDisabled == guiAttributeAssignAssign.attributeAssign.enabled )}" >
  --%>
                  <%-- see if there are assignments on the assignment --%>
  <%--
                  <tr  ${row % 2 == 1 ? 'class="alternate"' : ''} style="vertical-align: top">
                    <td style="white-space: nowrap;" align="right">
                      <span class="simpleMembershipUpdateDisabled"><grouper:message key="simpleAttributeUpdate.assignMetadata" /></span>
                      <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeUpdate.assignDeleteConfirm', true, true)}')) {ajax('SimpleAttributeUpdate.assignDelete?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}', {formIds: 'simpleAttributeFilterForm'});} return false;" 
                        ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                        alt="${grouper:message('simpleAttributeUpdate.deleteAssignmentAlt', true, false)}"
                        onmouseover="Tip('${grouper:message('simpleAttributeUpdate.deleteAssignmentAlt', true, true)}')" 
                        onmouseout="UnTip()"/></a>
    
                      <a href="#" onclick="ajax('SimpleAttributeUpdate.assignEdit?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}', {formIds: 'simpleAttributeFilterForm'}); return false;" 
                        ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                        alt="${grouper:message('simpleAttributeUpdate.editAssignmentAlt', true, false)}"
                        onmouseover="Tip('${grouper:message('simpleAttributeUpdate.editAssignmentAlt', true, true)}')" 
                        onmouseout="UnTip()"/></a>
                      <a class="assignmentMenuButton" href="#"
                        ><img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                        id="assignmentMenuButton_${guiAttributeAssignAssign.attributeAssign.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/></a>
                    </td>
                    <td style="white-space: nowrap;">
                    </td>
                    <td>
                      <grouper:message value="${grouper:escapeHtml(guiAttributeAssignAssign.attributeAssign.attributeDefName.displayExtension)}" 
                        valueTooltip="${grouper:escapeJavascript(guiAttributeAssignAssign.attributeAssign.attributeDefName.displayName)}" />
                    </td>
                    <td>
                      <grouper:message key="${guiAttributeAssignAssign.enabledDisabledKey}"  />
                    </td>
                    <td style="white-space: nowrap;">
  --%>
                      <%-- loop through the values --%>
  <%--
                      <c:set var="valueRow" value="0" />
                  
                      
                      <c:forEach items="${guiAttributeAssignAssign.attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">
  --%>                    
                        <%-- we need a newline before non-first rows --%>
  <%--
                        <c:if test="${valueRow != 0}">
                          <br />
                        </c:if>
    
                        <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeUpdate.assignValueDeleteConfirm', true, true)}')) {ajax('SimpleAttributeUpdate.assignValueDelete?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'});} return false;" 
                          ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                          alt="${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, false)}"
                          onmouseover="Tip('${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, true)}')" 
                          onmouseout="UnTip()"/></a>
    
                        <a href="#" onclick="ajax('SimpleAttributeUpdate.assignValueEdit?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'}); return false;" 
                          ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                          alt="${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, false)}"
                          onmouseover="Tip('${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, true)}')" 
                          onmouseout="UnTip()"/></a>
                        
                        ${grouper:escapeHtml(attributeAssignValue.valueFriendly)}
                        
                        <c:set var="valueRow" value="${valueRow + 1}" />
                        
                      </c:forEach>
                    
                    </td>
                    <td>
                      <grouper:message value="${grouper:escapeJavascript(guiAttributeAssignAssign.attributeAssign.attributeDef.extension)}" 
                        valueTooltip="${grouper:escapeJavascript(guiAttributeAssignAssign.attributeAssign.attributeDef.name)}" />
                    </td>
                    <td>${grouper:abbreviate(guiAttributeAssignAssign.attributeAssign.id, 8, true, true)}</td>
                  </tr>
                  
                  
                  </c:if>
              
              </c:forEach>              
            </c:forEach>          
          </table> 
--%> 
          <%-- attach a menu for each row --%>
<%--
          <grouper:menu menuId="assignmentMenu"
            operation="SimpleAttributeUpdateMenu.assignmentMenu" 
            structureOperation="SimpleAttributeUpdateMenu.assignmentMenuStructure" 
            contextZoneJqueryHandle=".assignmentMenuButton" contextMenu="true" />
--%>       
          <%-- show the google like paging buttons at the bottom to pick a page to go to --%>
          <%--div class="pagingButtons">
            <grouper:message key="simpleAttributeUpdate.pagingResultPrefix" />
              <grouper:paging showSummaryOrButtons="false" pagingName="simpleAttributeUpdatePrivileges" 
              refreshOperation="SimpleAttributeUpdate.attributeEditPanelPrivileges?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&showIndirectPrivileges=${attributeUpdateRequestContainer.showIndirectPrivilegesComputed}" 
               />
          </div --%>
<%--        
        </c:otherwise>
      </c:choose>
--%>
              <c:set var="row" value="${row + 1}" />
            </c:forEach>
            <tr>
              <th class="privilegeHeader" colspan="${permissionUpdateRequestContainer.allActionsSize + 4}">&nbsp;</th>                    
            </tr>
          </c:forEach>

          <%-- attach a menu for each row --%>
          <grouper:menu menuId="permissionsMenu"
            operation="SimplePermissionUpdateMenu.assignmentMenu" 
            structureOperation="SimplePermissionUpdateMenu.assignmentMenuStructure" 
            contextZoneJqueryHandle=".permissionMenuButton" contextMenu="true" />
          
          <%-- attach a menu for each direct limit row --%>
          <grouper:menu menuId="limitMenu"
            operation="SimplePermissionUpdateMenu.limitMenu" 
            structureOperation="SimplePermissionUpdateMenu.limitMenuStructure" 
            contextZoneJqueryHandle=".limitMenuButton" contextMenu="true" />
          
          
          <tr>
            <td colspan="${permissionUpdateRequestContainer.allActionsSize + 4}">
    
              <input class="redButton" type="submit" 
              onclick="ajax('../app/SimplePermissionUpdate.permissionCancel'); return false;" 
              value="${grouper:escapeJavascript(navMap['simplePermissionUpdate.permissionPanelCancelButton'])}" style="margin-top: 2px" />
             
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimplePermissionUpdate.permissionPanelSubmit', {formIds: 'attributePermissionsFormId, simplePermissionFilterForm'}); return false;" 
              value="${grouper:escapeJavascript(navMap['simplePermissionUpdate.permissionPanelSubmitButton'])}" style="margin-top: 2px" />
           
            </td>
          </tr>
          
        </table>
      </c:otherwise>
    </c:choose>
    <br />
    </form>
  </div>
</div>

<!-- End: simplePermissionUpdate/simplePermissionAssignments.jsp -->
