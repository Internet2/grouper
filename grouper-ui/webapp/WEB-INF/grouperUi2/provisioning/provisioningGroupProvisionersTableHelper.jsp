<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<c:choose>
     <c:when test="${grouperRequestContainer.provisioningContainer.guiGrouperProvisioningAttributeValues.size() > 0}">
     	
     	<table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>        
            <tr>
              <th>${textContainer.text['provisioningConfigTableHeaderActions']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderProvisionerName']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderProvisionable']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderInTarget']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderMetadata']}</th>
               <th>${textContainer.text['provisioningConfigTableHeaderDirectAssignment']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderParentFolderIsProvisionable']}</th>
              <th>${textContainer.text['provisioningConfigTableHeaderLastTimeWorkWasDone']}</th> 
                        
            </tr>
          </thead>
          <tbody>
          
          <c:forEach items="${grouperRequestContainer.provisioningContainer.guiGrouperProvisioningAttributeValues}" var="guiGrouperProvisioningAttributeValue" >
	        
          <c:set var="grouperProvisioningAttributeValue" 
	            value="${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue}" />
	            
              <tr>
              
               <td>
                   <div class="btn-group">
                         <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                           aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                           ${textContainer.text['provisioningConfigTableActionsButton'] }
                           <span class="caret"></span>
                         </a>
                         <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                         
                           <c:if test="${grouperProvisioningAttributeValue.directAssignment == false && guiGrouperProvisioningAttributeValue.provisionable == false}">
                          
                           <c:if test="${guiGrouperProvisioningAttributeValue.canAssignProvisioning}">         
                             <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.provisioningToOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsProvisionTo'] }</a></li>
                           </c:if>
                          </c:if>
                          
                          <c:if test="${grouperProvisioningAttributeValue.directAssignment || guiGrouperProvisioningAttributeValue.provisionable }">
                            
                           <c:if test="${guiGrouperProvisioningAttributeValue.canAssignProvisioning}">     
                            <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.editProvisioningOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsEditProvisioning'] }</a></li>   
                           </c:if>
                          </c:if>
                          
                          <c:if test="${grouperProvisioningAttributeValue.directAssignment && guiGrouperProvisioningAttributeValue.provisionable && !guiGrouperProvisioningAttributeValue.parentWillMakeThisProvisionable}">
                            <c:if test="${guiGrouperProvisioningAttributeValue.canAssignProvisioning}">
                             <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.removeProvisioningOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsDoNotProvision'] }</a></li>
                           </c:if>
                          </c:if>
                          
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningConfigurationOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsViewConfiguration'] }</a></li>
                          
                          <c:if test="${guiGrouperProvisioningAttributeValue.provisionable}">
                            <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningTargetDetailsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsViewDetails'] }</a></li>
                          </c:if>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningTargetLogsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.targetName}');">${textContainer.text['provisioningConfigTableActionsViewLogs'] }</a></li>
                            
                         </ul>
                       </div>
                 </td>
              
              <td style="white-space: nowrap;">
                ${guiGrouperProvisioningAttributeValue.externalizedName}
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
                <c:if test="${fn:length(guiGrouperProvisioningAttributeValue.metadataNameValuesExternalized) > 0}">
                  
                    <c:forEach items="${guiGrouperProvisioningAttributeValue.metadataNameValuesExternalized}" var="metadataNameValue">
                      <span> <b> ${metadataNameValue.key}: </b> ${metadataNameValue.value} </span>
                      <br/>
                    </c:forEach>
                </c:if>
              </td>
              
               <td style="white-space: nowrap;">
                 <c:if test="${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.directAssignment}">
                   ${textContainer.text['provisioningConfigTableHeaderHasDirectSettingsYesLabel']}
                 </c:if>
                  <c:if test="${!guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue.directAssignment}">
                   ${textContainer.text['provisioningConfigTableHeaderHasDirectSettingsNoLabel']}
                 </c:if>
              </td>
              
              <td style="white-space: nowrap;">
               <c:choose>
               
               <c:when test="${guiGrouperProvisioningAttributeValue.parentWillMakeThisProvisionable}">
                 ${textContainer.text['provisioningConfigTableHeaderParentFolderIsProvisionableYesLabel']}
               </c:when>
                <c:otherwise>
                 ${textContainer.text['provisioningConfigTableHeaderParentFolderIsProvisionableNoLabel']}
               </c:otherwise>
               
               </c:choose>
               
              </td>
              
              <td style="white-space: nowrap;">
                ${guiGrouperProvisioningAttributeValue.lastTimeWorkWasDone}
              </td>
              
                 </tr>
                    
         </c:forEach>
          
          </tbody>
        </table>
     
    </c:when>
	    <c:otherwise>
	    <p>${textContainer.text['provisioningNoTargets']}</p>
	    </c:otherwise>
    </c:choose>