<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- DHTMLX menu includes removed; permissions context menus now use Bootstrap dropdowns --%>

<c:choose>
  <c:when test="${fn:length(grouperRequestContainer.permissionUpdateRequestContainer.guiPermissionEntryActionsContainers) == 0}">
    ${textContainer.text['groupViewPermissionsNoAssignedPermissions']}
  </c:when>
  <c:otherwise>
  <form class="form-horizontal" id="attributePermissionsFormId" name="attributePermissionsFormName" onsubmit="return false;">
   <input type="hidden" name="permissionAssignType" value="${grouperRequestContainer.permissionUpdateRequestContainer.permissionType.name}" />
   <input type="hidden" name="memberId" value="${grouperRequestContainer.permissionContainer.guiMember.member.id}" />
    <table class="table table-hover table-bordered table-striped table-condensed data-table">
        <c:forEach items="${grouperRequestContainer.permissionUpdateRequestContainer.guiPermissionEntryActionsContainers}" var="guiPermissionEntryActionsContainer">
          <c:set var="row" value="0" />
          <c:forEach items="${guiPermissionEntryActionsContainer.guiPermissionEntryContainers}" var="guiPermissionEntryContainer">
          
            <c:if test="${grouperRequestContainer.permissionUpdateRequestContainer.showHeader[row]}">
              <thead>
                <tr>
                  <th></th>
                  <th></th>
                  <th style="background-color: #DFEFF4; text-align: center;" colspan="${grouperRequestContainer.permissionUpdateRequestContainer.allActionsSize }">
                    ${textContainer.text['groupViewPermissionsColumnActionsHeader']}
                  </th>
                  <th></th>
                </tr>
                <tr>
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    ${textContainer.text['groupViewPermissionsColumnRoleName']}
                  </th>
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    ${textContainer.text['groupViewPermissionsColumnResourceName']}
                  </th>
                  <c:forEach items="${grouperRequestContainer.permissionUpdateRequestContainer.allActions}" var="action">
                    <th class="privilegeHeader" style="white-space: nowrap; border-right: #8e8a8f 1px solid; min-width: 40px;">
                      ${grouper:escapeHtml(action)}
                    </th>
                  </c:forEach>
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    ${textContainer.text['groupViewPermissionsColumnPermissionDefinition']}
                  </th>
                </tr>
              </thead>
            </c:if>
  			
  			<tbody>
  			
              <tr style="vertical-align: top">
              <td style="white-space: nowrap;">
                ${guiPermissionEntryContainer.guiRole.shortLinkWithIcon}
              </td>
              
              <td style="white-space: nowrap;">
              	${guiPermissionEntryContainer.guiPermissionResource.shortLinkWithIcon}
              </td>
              
              <c:forEach items="${grouperRequestContainer.permissionUpdateRequestContainer.allActions}" var="action">
                <td align="center" style="white-space: nowrap; border-right: #8e8a8f 1px solid; min-width: 40px;">
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
                    />
                    <a href="#" style="margin-left: 5px"
                    onclick="ajax('../app/UiV2SubjectPermission.permissionPanelImageClick?permissionAssignType=${grouperRequestContainer.permissionUpdateRequestContainer.permissionType.name}&guiPermissionId=${guiPermissionId}&allow=${guiPermissionEntryChecked ? 'false' : 'true'}', {formIds: 'attributePermissionsFormId'}); return false;"
                    ><c:choose><c:when test="${guiPermissionEntry.allowed}"
                      ><img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
                        onmouseover="Tip('${grouper:escapeJavascript(navMap[tooltipName])}')"
                        onmouseout="UnTip()"
                        /></c:when><c:otherwise><img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" 
                          onmouseover="Tip('${grouper:escapeJavascript(navMap['simplePermissionAssign.unassignedTooltip'])}')" 
                          onmouseout="UnTip()"
                        /></c:otherwise></c:choose></a>
                    <div class="btn-group" style="display: inline-block;">
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="dropdown-toggle grouperDropdownToggleIconOnly"
                        aria-haspopup="true" aria-expanded="false" role="menu"
                        onclick="$(this).next('ul').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $(this).next('ul').find('li').first().focus();return true;}); return false;">
                        <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                          id="permissionMenuButton_${guiPermissionId}__${grouperRequestContainer.permissionUpdateRequestContainer.permissionType.name}" 
                          alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
                      </a>
                      <ul class="dropdown-menu dropdown-menu-right">
                        <li>
                          <a href="#" onclick="ajax('../app/UiV2SubjectPermission.assignmentMenu', {requestParams: {menuItemId: 'addLimit', menuIdOfMenuTarget: 'permissionMenuButton_${guiPermissionId}__${grouperRequestContainer.permissionUpdateRequestContainer.permissionType.name}'}}); return false;">${textContainer.text['simplePermissionAssign.addLimit']}</a>
                        </li>
                        <li>
                          <a href="#" onclick="ajax('../app/UiV2SubjectPermission.assignmentMenu', {requestParams: {menuItemId: 'analyzeAssignment', menuIdOfMenuTarget: 'permissionMenuButton_${guiPermissionId}__${grouperRequestContainer.permissionUpdateRequestContainer.permissionType.name}'}}); return false;">${textContainer.text['simplePermissionAssign.assignMenuAnalyzeAssignment']}</a>
                        </li>
                        <li>
                          <a href="#" onclick="ajax('../app/UiV2SubjectPermission.assignmentMenu', {requestParams: {menuItemId: 'editAssignment', menuIdOfMenuTarget: 'permissionMenuButton_${guiPermissionId}__${grouperRequestContainer.permissionUpdateRequestContainer.permissionType.name}'}}); return false;">${textContainer.text['simplePermissionAssign.editAssignment']}</a>
                        </li>
                      </ul>
                    </div>
                    
                  </c:if>
                </td>
              </c:forEach>
              <td>
                ${guiPermissionEntryContainer.guiPermissionDefinition.shortLinkWithIcon}
              </td>
              </tr>
              
              <c:forEach items="${guiPermissionEntryContainer.guiPermissionLimitBeanContainers}" var="guiPermissionLimitBeanContainer">
                <tr  style="vertical-align: top">
  
                <td style="white-space: nowrap; padding-left: 1em">
                  <span class="simpleMembershipUpdateDisabled">
                  ${textContainer.text['simplePermissionUpdate.limitLabel']}</span>
                  <c:choose>
                    <c:when test="${guiPermissionLimitBeanContainer.immediate}" >
                      <div class="btn-group" style="display: inline-block;">
                        <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="dropdown-toggle grouperDropdownToggleIconOnly"
                          aria-haspopup="true" aria-expanded="false" role="menu"
                          onclick="$(this).next('ul').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $(this).next('ul').find('li').first().focus();return true;}); return false;">
                          <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                            id="limitMenuButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-right">
                          <li>
                            <a href="#" onclick="ajax('../app/UiV2SubjectPermission.limitMenu', {requestParams: {menuItemId: 'addValue', menuIdOfMenuTarget: 'limitMenuButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}'}}); return false;">${textContainer.text['simplePermissionAssign.limitMenuAddValue']}</a>
                          </li>
                          <li>
                            <a href="#" onclick="ajax('../app/UiV2SubjectPermission.limitMenu', {requestParams: {menuItemId: 'editLimit', menuIdOfMenuTarget: 'limitMenuButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}'}}); return false;">${textContainer.text['simplePermissionAssign.limitMenuEditLimit']}</a>
                          </li>
                          <li>
                            <a href="#" onclick="ajax('../app/UiV2SubjectPermission.limitMenu', {requestParams: {menuItemId: 'deleteLimit', menuIdOfMenuTarget: 'limitMenuButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}'}}); return false;">${textContainer.text['simplePermissionAssign.limitMenuDeleteLimit']}</a>
                          </li>
                        </ul>
                      </div>
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
                <td style="white-space: nowrap;" colspan="${grouperRequestContainer.permissionUpdateRequestContainer.allActionsSize}">
                  <c:if test="${guiPermissionLimitBeanContainer.hasValues}">
  
                    <%-- loop through the values --%>
                    <c:set var="valueRow" value="0" />
                
                    
                    <c:forEach items="${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssignValues}" var="limitAssignValue">
                    
                      <%-- we need a newline before non-first rows --%>
                      <c:if test="${valueRow != 0}">
                        <br />
                      </c:if>
    
                      <span class="simpleMembershipUpdateDisabled"><grouper:message key="simplePermissionUpdate.limitValueLabel" /></span>
                      ${grouper:escapeHtml(limitAssignValue.valueFriendly)}
                      
                      <c:choose>
                        <c:when test="${guiPermissionLimitBeanContainer.immediate}" >
                        
                          <div class="btn-group" style="display: inline-block;">
                            <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="dropdown-toggle grouperDropdownToggleIconOnly"
                              aria-haspopup="true" aria-expanded="false" role="menu"
                              onclick="$(this).next('ul').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $(this).next('ul').find('li').first().focus();return true;}); return false;">
                              <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                               id="limitAssignValueButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}_${limitAssignValue.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-right">
                              <li>
                                <a href="#" onclick="ajax('../app/UiV2SubjectPermission.limitValueMenu', {requestParams: {menuItemId: 'editLimitValue', menuIdOfMenuTarget: 'limitAssignValueButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}_${limitAssignValue.id}'}}); return false;">${textContainer.text['simplePermissionAssign.limitMenuEditValue']}</a>
                              </li>
                              <li>
                                <a href="#" onclick="ajax('../app/UiV2SubjectPermission.limitValueMenu', {requestParams: {menuItemId: 'deleteLimitValue', menuIdOfMenuTarget: 'limitAssignValueButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}_${limitAssignValue.id}'}}); return false;">${textContainer.text['simplePermissionAssign.limitMenuDeleteValue']}</a>
                              </li>
                            </ul>
                          </div>
                        
                        </c:when>
                        <c:otherwise>
                          <img src="../../grouperExternal/public/assets/images/spacer.gif" height="14px" border="0" 
                                alt=""/>
                          <img src="../../grouperExternal/public/assets/images/spacer.gif" height="14px" border="0" 
                                alt=""/>
                        </c:otherwise>
                      </c:choose>
                      
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
  			
  			</tbody>
          <c:set var="row" value="${row + 1}" />
        </c:forEach>
        <tr>
          <th class="privilegeHeader" colspan="${grouperRequestContainer.permissionUpdateRequestContainer.allActionsSize + 4}">&nbsp;</th>                    
        </tr>
      </c:forEach>

      <%-- DHTMLX menus removed (replaced by Bootstrap dropdowns) --%>
      
    </table>
    <div>
      <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2SubjectPermission.saveMultiplePermissionSubmit', {formIds: 'attributePermissionsFormId'}); return false;">${textContainer.text['subjectAssignPermissionSaveButton'] }</a> 
    </div>
    </form>
  </c:otherwise>
</c:choose>