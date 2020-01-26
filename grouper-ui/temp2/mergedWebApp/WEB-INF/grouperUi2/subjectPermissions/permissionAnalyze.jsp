<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.permissionContainer.guiGroup.group.parentUuid}" />

<div class="row-fluid">
  <div class="span12">  
    <div id="permissionAnalyzeResutsId">
      
      <c:set var="guiPermissionEntry" value="${permissionUpdateRequestContainer.guiPermissionEntry}"/>
      <c:set var="permissionEntry" value="${guiPermissionEntry.permissionEntry}"/>
  
      <c:set var="guiPermissionId" value="${permissionEntry.roleId}__${permissionEntry.memberId}__${permissionEntry.attributeDefNameId}__${permissionEntry.action}" />
      
      <table class="table table-condensed table-striped">
      	
      	<thead>
      	  <tr>
      	    <th colspan="2">
      	      <span style="color: #1c6070;">${textContainer.text['simplePermissionAssign.analyzeSubtitle']}</span>
      	      <span style="display: block; font-weight: normal;">${textContainer.text['simplePermissionAssign.analyzeSubtitleInfodot']}</span>
      	    </th>
      	  </tr>
      	</thead>
      
        <tbody>
          <tr>
          	<c:set var="permissionType" value="permissionUpdateRequestContainer.permissionType.${permissionUpdateRequestContainer.permissionType.name}" />
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['permissionUpdateRequestContainer.permissionType']}</strong></td>
            <td>
              ${textContainer.text[permissionType]}
            </td>
          </tr>

          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['simplePermissionUpdate.assignHeaderRoleName']}</strong></td>
            <td>
              <span>${guiPermissionEntry.guiRole.shortLinkWithIcon}</span>
            </td>
          </tr>
          
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['simplePermissionUpdate.assignHeaderAction']}</strong></td>
            <td>
              ${grouper:escapeHtml(permissionEntry.action)}
              <br />
            </td>
          </tr>

          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['simplePermissionUpdate.assignHeaderResource']}</strong></td>
            <td>
              <span>${guiPermissionEntry.guiAttributeDefName.shortLinkWithIcon}</span>
            </td>
          </tr>

          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['simplePermissionUpdate.assignHeaderDefinition']}</strong></td>
            <td>
              <span>${guiPermissionEntry.guiAttributeDef.shortLinkWithIcon}</span>
            </td>
          </tr>


          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['simplePermissionAssign.assignEditId']}</strong></td>
            <td>
              <span>${permissionEntry.attributeAssignId}</span>
              <br />
            </td>
          </tr>

          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['simplePermissionUpdate.assignEditEnabledDate']}</strong></td>
            <td>
              <c:if test="${guiPermissionEntry.enabledDate != null}">
                ${guiPermissionEntry.enabledDate}
                <span class="simpleMembershipUpdateDisabled">
                  ${textContainer.text['simplePermissionUpdate.assignEditEnabledDisabledDateMask']}
                </span>
              </c:if>
            </td>
          </tr>

          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['simplePermissionUpdate.assignEditDisabledDate']}</strong></td>
            <td>
              <c:if test="${guiPermissionEntry.disabledDate != null}">
                ${guiPermissionEntry.disabledDate}
                <span class="simpleMembershipUpdateDisabled">
                  ${textContainer.text['simplePermissionUpdate.assignEditEnabledDisabledDateMask']}
                </span>
              </c:if>
            </td>
          </tr>


          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['simplePermissionUpdate.analyzeResult']}</strong></td>
            <td>
              <c:choose>
                <c:when test="${guiPermissionEntry.immediate && !guiPermissionEntry.effective}">
                  <c:set var="tooltipName" value="simplePermissionAssign.immediateTooltip" />
                </c:when>
                <c:when test="${!guiPermissionEntry.immediate && guiPermissionEntry.effective}">
                  <c:set var="tooltipName" value="simplePermissionAssign.effectiveTooltip" />
                </c:when>
                <c:otherwise>
                  <c:set var="tooltipName" value="simplePermissionAssign.immediateAndEffectiveTooltip" />
                </c:otherwise>
              </c:choose>
              <c:choose>
                <c:when test="${guiPermissionEntry.allowed}">
                  <img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
                  onmouseover="Tip('${grouper:escapeJavascript(navMap[tooltipName])}')" 
                  onmouseout="UnTip()" />
                </c:when>
                <c:otherwise>
                  <img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" 
                    onmouseover="Tip('${grouper:escapeJavascript(navMap['simplePermissionAssign.unassignedTooltip'])}')" 
                    onmouseout="UnTip()"/>
                </c:otherwise>
              </c:choose>
            </td>
          </tr>

        </tbody>
      </table>
    </div>
    
    <div id="permissionAnalyzeResultReasonId">
      <table class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th colspan="9">
              <span style="color: #1c6070;">${textContainer.text['simplePermissionAssign.analyzeRelevantAssignmentsSubtitle']}</span>
      	      <span style="display: block; font-weight: normal;">${textContainer.text['simplePermissionAssign.analyzeRelevantAssignmentsSubtitleInfodot']}</span>
            </th>
          </tr>
          <tr>           
            <th>${textContainer.text['permissionUpdateRequestContainer.permissionType']}</th>
            <th>${textContainer.text['simplePermissionUpdate.assignHeaderOwnerRole']}</th>
            <c:choose>
              <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
                  <th>${textContainer.text['simplePermissionUpdate.assignHeaderOwnerMember']}</th>
              </c:when>
            </c:choose>
            <th>${textContainer.text['simplePermissionUpdate.assignHeaderAction']}</th>
            <th>${textContainer.text['simplePermissionUpdate.assignHeaderPermissionResource']}</th>
            <th>${textContainer.text['simplePermissionUpdate.assignHeaderAllowed']}</th>
            <th>${textContainer.text['simplePermissionAssign.assignHeaderScore']}</th>
            <th>${textContainer.text['simplePermissionAssign.whyRankNotOne']}</th>
            <th>${textContainer.text['simplePermissionUpdate.assignHeaderPermissionDefinition']}</th>
            <th>${textContainer.text['simplePermissionUpdate.assignHeaderOwnerUuid']}</th>
          </tr>
        </thead>
        <tbody>
          
          <c:forEach items="${guiPermissionEntry.rawGuiPermissionEntries}" var="guiPermissionEntry">
            <c:set var="row" value="0" />
            <c:set var="permissionEntry" value="${guiPermissionEntry.permissionEntry}"/>
            <c:set var="attributeAssign" value="${permissionEntry.attributeAssign}"/>
          	
          	<tr>
          	  <td>
          	    <c:set var="permissionTypeEntry" value="permissionUpdateRequestContainer.permissionType.${permissionEntry.permissionType.name}" />
                ${textContainer.text[permissionTypeEntry]}
          	  </td>
          	  <td>${guiPermissionEntry.guiRole.shortLinkWithIcon}</td>
          	  <c:choose>
                <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
                  <td style="white-space: nowrap;">
                    <c:choose>
                      <c:when test="${guiPermissionEntry.permissionEntry.permissionType == 'role_subject'}">
                        ${grouper:escapeHtml(guiPermissionEntry.stringLabelShortFromGuiSubject[permissionEntry.member.subject])}    
                      </c:when>
                     </c:choose>
                  </td>
                </c:when>
              </c:choose>
              <td>${grouper:escapeHtml(attributeAssign.attributeAssignAction.name)}</td>
              <td>${guiPermissionEntry.guiAttributeDefName.shortLinkWithIcon}</td>
              <td>
                <c:choose>
                  <c:when test="${attributeAssign.disallowed}">
                    <img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" 
                      onmouseover="Tip('${grouper:escapeJavascript(navMap['simplePermissionAssign.assignAllowedDisallow'])}')" 
                      onmouseout="UnTip()"
                    />                    
                  </c:when>
                  <c:otherwise>
                    <img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
                      onmouseover="Tip('${grouper:escapeJavascript(navMap['simplePermissionAssign.assignAllowedAllow'])}')" 
                      onmouseout="UnTip()"
                      />
                  </c:otherwise>
                </c:choose>
              </td>
              
              <td>${permissionEntry.permissionHeuristics.friendlyScore }</td>
              <td>${grouper:abbreviate(guiPermissionEntry.compareWithBest, 60, true, true)}</td>
              <td>${guiPermissionEntry.guiAttributeDef.shortLinkWithIcon}</td>
              <td>${grouper:abbreviate(permissionEntry.attributeAssignId, 8, true, true)}</td>
          	</tr>
            <c:set var="row" value="${row + 1}" />
          </c:forEach>
        </tbody>
      </table>
    </div>
  
  </div>
</div>