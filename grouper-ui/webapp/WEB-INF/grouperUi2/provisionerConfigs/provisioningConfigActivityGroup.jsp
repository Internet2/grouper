<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

<div class="row-fluid">
   <c:choose>
     <c:when test="${fn:length(grouperRequestContainer.provisionerConfigurationContainer.activityForGroup) > 0}">
       <div>
       <table class="table table-hover table-bordered table-striped table-condensed data-table">
         <thead>        
           <tr>
             <th>${textContainer.text['provisionerActivityTableHeaderGroupId']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderGroupName']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderGroupIdIndex']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionable']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTarget']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetInsertOrExists']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetEnd']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionableStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionableEnd']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastUpdated']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastGroupSyncStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastGroupSync']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastGroupMetadataSyncStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastGroupMetadataSync']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderGroupAttributeValueCache0']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderGroupAttributeValueCache1']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderGroupAttributeValueCache2']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderGroupAttributeValueCache3']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderMetadataUpdated']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorMessage']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorTimestamp']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorCode']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastTimeWorkWasDone']}</th>
           </tr>
           </thead>
           <tbody>
             <c:set var="i" value="0" />
             <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.activityForGroup}" var="grouperSyncGroup">
             
               <tr>
                  <td style="white-space: nowrap;">
                	${grouperSyncGroup.groupId}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncGroup.groupName}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	 ${grouperSyncGroup.groupIdIndex}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	<c:choose>
	                  <c:when test="${grouperSyncGroup.provisionable}">
	                    ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
	                  </c:when>
	                  <c:otherwise>
	                    ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
	                  </c:otherwise>
                 	</c:choose>
                  </td>
                  
                  <td style="white-space: nowrap;">
	                  <c:choose>
	                    <c:when test="${grouperSyncGroup.inTarget}">
	                      ${textContainer.text['provisioningConfigTableHeaderInTargetYesLabel']}
	                    </c:when>
	                    <c:otherwise>
	                      ${textContainer.text['provisioningConfigTableHeaderInTargetNoLabel']}
	                    </c:otherwise>
	                 </c:choose>
                  </td>
                  
                  <td style="white-space: nowrap;">
                      <c:choose>
		                <c:when test="${grouperSyncGroup.inTargetInsertOrExists}">
		                  ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsTrueLabel']}
		                </c:when>
		                <c:otherwise>
		                  ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsFalseLabel']}
		                </c:otherwise>
		              </c:choose>
                      <br />
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncGroup.inTargetStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncGroup.inTargetEnd}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.provisionableStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.provisionableEnd}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.lastUpdated}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.lastGroupSyncStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.lastGroupSync}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.lastGroupMetadataSyncStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.lastGroupMetadataSync}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.groupAttributeValueCache0}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.groupAttributeValueCache1}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.groupAttributeValueCache2}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.groupAttributeValueCache3}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.metadataUpdated}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouper:escapeHtml(grouperSyncGroup.errorMessage)}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.errorTimestamp}
                  </td>
                  
                  <td style="white-space: nowrap;">
                  
                  <c:if test="${not empty grouperSyncGroup.errorCode}">
                    <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${grouperSyncGroup.errorCode}"></c:set>
                    ${textContainer.text[errorText]}
                   </c:if>
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.lastTimeWorkWasDone}
                  </td>
                  
                  </tr>
               </c:forEach>
            
            </tbody>
        </table>
       </div>
     </c:when>
     <c:otherwise>
       <div class="row-fluid">
         <div class="span9">
          <p><b>
          ${textContainer.text['provisionerConfigNoRecentActivityFound'] } 
          </b></p>
         </div>
       </div>
     </c:otherwise>
   </c:choose>
   
 </div>