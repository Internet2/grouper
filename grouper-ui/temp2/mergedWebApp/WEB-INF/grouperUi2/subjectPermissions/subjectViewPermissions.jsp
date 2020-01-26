<%@ include file="../assetsJsp/commonTaglib.jsp"%>

	<script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcommon.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcombo.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcalendar.js"></script>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxmenu.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/menu/ext/dhtmlxmenu_ext.js"></script>
    <link rel="stylesheet" type="text/css" href="../../grouperExternal/public/assets/dhtmlx/menu/skins/dhtmlxmenu_dhx_blue.css" />

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/ext/dhtmlxcombo_extra.js"></script>
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
                        <a class="permissionMenuButton" href="#"
                          ><img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                          id="permissionMenuButton_${guiPermissionId}__${grouperRequestContainer.permissionUpdateRequestContainer.permissionType.name}" 
                          alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/></a>
                        
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
                            
                              <a class="limitAssignValueButton" href="#">
                                <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                                 id="limitAssignValueButton_${guiPermissionLimitBeanContainer.permissionLimitBean.limitAssign.id}_${limitAssignValue.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
                              </a>
                            
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

          <%-- attach a menu for each row --%>
          <grouper:menu menuId="permissionsMenu"
            operation="UiV2SubjectPermission.assignmentMenu"
            structureOperation="UiV2SubjectPermission.assignmentMenuStructure" 
            contextZoneJqueryHandle=".permissionMenuButton" contextMenu="true" />
          
          <%-- attach a menu for each direct limit row --%>
          <grouper:menu menuId="limitMenu"
            operation="UiV2SubjectPermission.limitMenu" 
            structureOperation="UiV2SubjectPermission.limitMenuStructure" 
            contextZoneJqueryHandle=".limitMenuButton" contextMenu="true" />
            
          <%-- attach a menu for each limit value --%>
          <grouper:menu menuId="limitValueMenu"
            operation="UiV2SubjectPermission.limitValueMenu"
            structureOperation="UiV2SubjectPermission.limitValueMenuStructure" 
            contextZoneJqueryHandle=".limitAssignValueButton" contextMenu="true" />
          
        </table>
        <div>
          <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2SubjectPermission.saveMultiplePermissionSubmit', {formIds: 'attributePermissionsFormId'}); return false;">${textContainer.text['subjectAssignPermissionSaveButton'] }</a> 
        </div>
        </form>
      </c:otherwise>
    </c:choose>