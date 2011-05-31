<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionEdit.jsp -->
<div class="simplePermissionEdit">

  <div class="section">
  
    <grouper:subtitle key="simplePermissionAssign.analyzeSubtitle" 
      infodotValue="${grouper:message('simplePermissionAssign.analyzeSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">

      <c:set var="guiPermissionEntry" value="${permissionUpdateRequestContainer.guiPermissionEntry}"/>
      <c:set var="permissionEntry" value="${guiPermissionEntry.permissionEntry}"/>
  
      <c:set var="guiPermissionId" value="${permissionEntry.roleId}__${permissionEntry.memberId}__${permissionEntry.attributeDefNameId}__${permissionEntry.action}" />
      
        <table class="formTable formTableSpaced">
      
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="permissionUpdateRequestContainer.permissionType" />
              
            </td>
            <td class="formTableRight">
              <grouper:message 
                 key="permissionUpdateRequestContainer.permissionType.${permissionUpdateRequestContainer.permissionType.name}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderRoleName" />
            </td>
            <td class="formTableRight">
              <grouper:message valueTooltip="${grouper:escapeHtml(permissionEntry.role.displayName)}" 
                value="${grouper:escapeHtml(permissionEntry.role.displayExtension)}"  />
            </td>
          </tr>
          <c:choose>
            <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
              <tr class="formTableRow">
                <td class="formTableLeft" style="white-space: nowrap;">
                  <grouper:message key="simplePermissionUpdate.assignHeaderEntity" />
                </td>
                <td class="formTableRight">
                  <grouper:message valueTooltip="${grouper:escapeHtml(guiPermissionEntry.screenLabelLongIfDifferent)}" 
                     value="${grouper:escapeHtml(guiPermissionEntry.screenLabelShort)}"  />
                </td>
              </tr>
            </c:when>
          </c:choose>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderAction" />
            </td>
            <td class="formTableRight">
              <grouper:message 
                 value="${grouper:escapeHtml(permissionEntry.action)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderResource" />
            </td>
            <td class="formTableRight">
              <grouper:message valueTooltip="${grouper:escapeHtml(permissionEntry.attributeDefName.displayName)}" 
                 value="${grouper:escapeHtml(permissionEntry.attributeDefName.displayExtension)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderDefinition" />
            </td>
            <td class="formTableRight">
              <grouper:message valueTooltip="${grouper:escapeHtml(permissionEntry.attributeDef.name)}" 
                 value="${grouper:escapeHtml(permissionEntry.attributeDef.extension)}"  />
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionAssign.assignEditId" />
            </td>
            <td class="formTableRight">
              ${permissionEntry.attributeAssignId}
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignEditEnabledDate" />
            </td>
            <td class="formTableRight">
              <c:if test="${guiPermissionEntry.enabledDate != null}">
                ${guiPermissionEntry.enabledDate}
                <span class="simpleMembershipUpdateDisabled"
                  ><grouper:message key="simplePermissionUpdate.assignEditEnabledDisabledDateMask" /></span>
              </c:if>
            </td>
          </tr>
          
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignEditDisabledDate" />
            </td>
            <td class="formTableRight">
              <c:if test="${guiPermissionEntry.disabledDate != null}">
                ${guiPermissionEntry.disabledDate}
                <span class="simpleMembershipUpdateDisabled"
                  ><grouper:message key="simplePermissionUpdate.assignEditEnabledDisabledDateMask" /></span>
              </c:if>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.analyzeResult" />
            </td>
            <td class="formTableRight">
              <c:choose>
                <c:when test="${guiPermissionEntry.immediate && !guiPermissionEntry.effective}"
                  ><c:set var="tooltipName" value="simplePermissionAssign.immediateTooltip" /></c:when
                  ><c:when test="${!guiPermissionEntry.immediate && guiPermissionEntry.effective}"
                  ><c:set var="tooltipName" value="simplePermissionAssign.effectiveTooltip" /></c:when
                  ><c:otherwise><c:set var="tooltipName" value="simplePermissionAssign.immediateAndEffectiveTooltip" /></c:otherwise>
              </c:choose>
              <c:choose>
                <c:when test="${guiPermissionEntry.allowed}"
                ><img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
                  onmouseover="Tip('${grouper:escapeJavascript(navMap[tooltipName])}')" 
                  onmouseout="UnTip()"
                  /></c:when>
                <c:otherwise><img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" 
                    onmouseover="Tip('${grouper:escapeJavascript(navMap['simplePermissionAssign.unassignedTooltip'])}')" 
                    onmouseout="UnTip()"
                  /></c:otherwise>
              
              </c:choose>              
            
            </td>
          </tr>
        </table>
      </div>
    </div>
        
    <div class="section">
    
      <grouper:subtitle key="simplePermissionAssign.analyzeRelevantAssignmentsSubtitle" 
        infodotValue="${grouper:message('simplePermissionAssign.analyzeRelevantAssignmentsSubtitleInfodot', true, false)}"/>
    
      <div class="sectionBody">
        
          <table cellspacing="2" class="formTable" width="700">
          <tr>
            <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
              <grouper:message key="permissionUpdateRequestContainer.permissionType" />
              
            </th>
            
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
              <grouper:message key="simplePermissionUpdate.assignHeaderAction" />
            </th>
            
            <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderPermissionResource" />
            </th>
            <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderPermissionDefinition" />
            </th>
            <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
              <grouper:message key="simplePermissionUpdate.assignHeaderOwnerUuid" />
            </th>
          </tr>
          <c:forEach items="${guiPermissionEntry.rawPermissionEntries}" var="permissionEntry">
            <c:set var="row" value="0" />
            <c:set var="attributeAssign" value="${permissionEntry.attributeAssign}"/>

            <tr  ${row % 2 == 1 ? 'class="alternate"' : ''} style="vertical-align: top">
                  
              <td style="white-space: nowrap;">
                <grouper:message 
                   key="permissionUpdateRequestContainer.permissionType.${permissionEntry.permissionType.name}" />
              </td>
              <td style="white-space: nowrap;">
                <grouper:message valueTooltip="${grouper:escapeHtml(permissionEntry.role.displayName)}" 
                   value="${grouper:escapeHtml(permissionEntry.role.displayExtension)}"  />
              </td>
              <c:choose>
                <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
                  <td style="white-space: nowrap;">
                    <grouper:message valueTooltip="${grouper:escapeHtml(guiPermissionEntry.stringLabelLongIfDifferentFromGuiSubject[permissionEntry.member.subject])}" 
                       value="${grouper:escapeHtml(guiPermissionEntry.stringLabelShortFromGuiSubject[permissionEntry.member.subject])}"  />
                  </td>
                </c:when>
              </c:choose>
                <td>
                  <grouper:message value="${grouper:escapeHtml(attributeAssign.attributeAssignAction.name)}" />
                </td>

                <td>
                  <grouper:message value="${grouper:escapeHtml(attributeAssign.attributeDefName.displayExtension)}" 
                    valueTooltip="${grouper:escapeJavascript(attributeAssign.attributeDefName.displayName)}" />
                </td>
                
                <td>
                  <grouper:message value="${grouper:escapeHtml(attributeAssign.attributeDef.extension)}" 
                    valueTooltip="${grouper:escapeJavascript(attributeAssign.attributeDef.name)}" />
                </td>
              <td style="white-space: nowrap;">
                ${grouper:abbreviate(permissionEntry.attributeAssignId, 8, true, true)}
              </td>
            </tr>                  

            <c:set var="row" value="${row + 1}" />
          </c:forEach>

        </table>
        
    </div>
  </div>
  
  
    <div class="section">
  
    <grouper:subtitle key="simplePermissionAssign.analyzeRelevantAssignmentsSubtitle" 
      infodotValue="${grouper:message('simplePermissionAssign.analyzeRelevantAssignmentsSubtitleInfodot', true, false)}"/>
  
    <div class="sectionBody">

      <table class="formTable formTableSpaced">
    
        <tr class="formTableRow">
          <td class="formTableLeft" style="white-space: nowrap;">
            <grouper:message key="simplePermissionAssign.analyzeImmediateAssignment" />
          </td>
          <td class="formTableRight">
            <grouper:message  
               key="${guiPermissionEntry.guiPermissionAnalyze.immediateAssignmentKey}"  />
          </td>
        </tr>
        </table>
        <table class="formTable formTableSpaced">

          <tr>
            <td colspan="2" align="left"  class="buttonRow">
  
              <button class="simplemodal-close blueButton"><grouper:message key="simplePermissionAssign.analyzeOkButton" /></button> 
            </td>
          </tr>
      </table>
      </div>
    </div>
  
  
</div>
<!-- End: simpleAttributeAssignEdit.jsp -->


