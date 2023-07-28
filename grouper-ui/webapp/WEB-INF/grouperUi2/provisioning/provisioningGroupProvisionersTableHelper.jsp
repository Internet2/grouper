<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<c:choose>
     <c:when test="${grouperRequestContainer.provisioningContainer.hasProvisioningOnThisObjectOrParent}">
     	
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
          
          <c:forEach items="${grouperRequestContainer.provisioningContainer.guiGrouperProvisioningAttributeValues}" var="guiGrouperProvisioningAttributeValue" >
	        
          <c:set var="grouperProvisioningAttributeValue" 
	            value="${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue}" />
	            
          <c:if test="${grouperProvisioningAttributeValue.directAssignment || 
                  		not empty grouperProvisioningAttributeValue.ownerStemId }">
                    
              <tr>
              <td style="white-space: nowrap;">
                ${guiGrouperProvisioningAttributeValue.externalizedName}
              </td>
              <td style="white-space: nowrap;">
                ${guiGrouperProvisioningAttributeValue.lastTimeWorkWasDone}
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${guiGrouperProvisioningAttributeValue.provisionable}">
                    ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
                  </c:when>
                  <c:otherwise>
                    ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
                  </c:otherwise>
                 </c:choose>
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${guiGrouperProvisioningAttributeValue.inTarget}">
                    ${textContainer.text['provisioningConfigTableHeaderInTargetYesLabel']}
                  </c:when>
                  <c:otherwise>
                    ${textContainer.text['provisioningConfigTableHeaderInTargetNoLabel']}
                  </c:otherwise>
                 </c:choose>
              </td>
              <td style="white-space: nowrap;">
                <c:choose>
                
                  <c:when test="${fn:length(guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.metadataNameValues) > 0}">
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
                         
                         	<li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningConfigurationOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsViewConfiguration'] }</a></li>
                         	
                         	<li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningTargetLogsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsViewLogs'] }</a></li>
                         	
                         	<li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningTargetDetailsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsViewDetails'] }</a></li>

                          <c:if test="${guiGrouperProvisioningAttributeValue.canAssignProvisioning}">          
                            <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.editProvisioningOnGroup2&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsEditProvisioning'] }</a></li>
                          </c:if>

							<%-- <c:if test="${grouperRequestContainer.provisioningContainer.canRunDaemon}">          
                         	  <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.runGroupSync&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsRunGroupSync'] }</a></li>
                         	</c:if>      --%>             	
                         </ul>
                       </div>
                 </td>
              
                 </tr>
                    
               </c:if>
	       
         </c:forEach>
          
          </tbody>
        </table>
     
    </c:when>
	    <c:otherwise>
	    <p>${textContainer.text['provisioningNoneAllConfigured']}</p>
	    </c:otherwise>
    </c:choose>