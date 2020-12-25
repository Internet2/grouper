<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<c:choose>
	<c:when test="${fn:length(grouperRequestContainer.provisioningContainer.guiGrouperSyncObjects) > 0}">
     	
     	<table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>        
            <tr>
              <th>${textContainer.text['provisioningConfigTableHeaderProvisionerName']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderLastTimeWorkWasDone']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderProvisionable']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderInTarget']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderHasDirectSettings']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderActions']}</th>              
            </tr>
          </thead>
          <tbody>
          
          <c:forEach items="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObjects}" var="guiGrouperSyncObject" >
          	<c:set value="${guiGrouperSyncObject.gcGrouperSyncMembership}" var="gcGrouperSyncMembership"></c:set>
              <tr>
              <td style="white-space: nowrap;">
                ${guiGrouperSyncObject.targetName}
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${gcGrouperSyncMembership.inTargetStart == null and gcGrouperSyncMembership.inTargetEnd == null}">
                  </c:when>
                  <c:when test="${gcGrouperSyncMembership.inTargetStart != null and gcGrouperSyncMembership.inTargetEnd == null}">
                  	${gcGrouperSyncMembership.inTargetStart}
                  </c:when>
                  <c:when test="${gcGrouperSyncMembership.inTargetStart == null and gcGrouperSyncMembership.inTargetEnd != null}">
                  	${gcGrouperSyncMembership.inTargetEnd}
                  </c:when>
                  <c:when test="${gcGrouperSyncMembership.inTargetStart > gcGrouperSyncMembership.inTargetEnd}">
                    ${gcGrouperSyncMembership.inTargetStart}
                  </c:when>
                  <c:when test="${gcGrouperSyncMembership.inTargetEnd > gcGrouperSyncMembership.inTargetStart}">
                    ${gcGrouperSyncMembership.inTargetEnd}
                  </c:when>
                 </c:choose>
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${gcGrouperSyncMembership.grouperSyncGroup.provisionable && gcGrouperSyncMembership.grouperSyncMember.provisionable}">
                    ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
                  </c:when>
                  <c:otherwise>
                    ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
                  </c:otherwise>
                 </c:choose>
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${gcGrouperSyncMembership.inTarget}">
                    ${textContainer.text['provisioningConfigTableHeaderInTargetYesLabel']}
                  </c:when>
                  <c:otherwise>
                    ${textContainer.text['provisioningConfigTableHeaderInTargetNoLabel']}
                  </c:otherwise>
                 </c:choose>
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${guiGrouperSyncObject.hasDirectSettings}">
                    ${textContainer.text['provisioningConfigTableHeaderHasDirectSettingsYesLabel']}
                  </c:when>
                  <c:otherwise>
                    ${textContainer.text['provisioningConfigTableHeaderHasDirectSettingsNoLabel']}
                  </c:otherwise>
                 </c:choose>
              </td>
              
              <td>
                   <div class="btn-group">
                         <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                           aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                           ${textContainer.text['provisioningConfigTableActionsButton'] }
                           <span class="caret"></span>
                         </a>
                         <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                         	<li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningTargetDetailsOnGroupMembership&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperSyncObject.targetName}&groupSyncMembershipId=${gcGrouperSyncMembership.id}&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}');">${textContainer.text['provisioningConfigTableActionsViewDetails'] }</a></li>
                         </ul>
                       </div>
                 </td>
              
                 </tr>
                    
         </c:forEach>
          
          </tbody>
        </table>
     
    </c:when>
    <c:otherwise>
    	<p>${textContainer.text['provisioningNoneConfiguredMembership']}</p>
    </c:otherwise>
    </c:choose>