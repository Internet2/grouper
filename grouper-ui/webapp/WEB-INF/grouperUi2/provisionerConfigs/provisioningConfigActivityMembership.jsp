<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

<div class="row-fluid">
   <c:choose>
     <c:when test="${fn:length(grouperRequestContainer.provisionerConfigurationContainer.activityForMembership) > 0}">
       <div>
       <table class="table table-hover table-bordered table-striped table-condensed data-table">
         <thead>        
           <tr>
           	
           	 <th>${textContainer.text['provisionerActivityTableHeaderGroupName']}</th>
           	 <th>${textContainer.text['provisionerActivityTableHeaderSubjectId']}</th>
           	 <th>${textContainer.text['provisionerActivityTableHeaderSubjectIdentifier']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTarget']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetInsertOrExists']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetEnd']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastUpdated']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderMembershipId']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderMembershipId2']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderMetadataUpdated']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorMessage']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorTimestamp']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorCode']}</th>
           </tr>
           </thead>
           <tbody>
             <c:set var="i" value="0" />
             <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.activityForMembership}" var="grouperSyncMembership">
             	
               <tr>
               		
                  <td style="white-space: nowrap;">
                	${grouperSyncMembership.grouperSyncGroup.groupName}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncMembership.grouperSyncMember.subjectId}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncMembership.grouperSyncMember.subjectIdentifier}
                  </td>
               		
                  <td style="white-space: nowrap;">
	                  <c:choose>
	                    <c:when test="${grouperSyncMembership.inTarget}">
	                      ${textContainer.text['provisioningConfigTableHeaderInTargetYesLabel']}
	                    </c:when>
	                    <c:otherwise>
	                      ${textContainer.text['provisioningConfigTableHeaderInTargetNoLabel']}
	                    </c:otherwise>
	                 </c:choose>
                  </td>
                  
                  <td style="white-space: nowrap;">
                      <c:choose>
		                <c:when test="${grouperSyncMembership.inTargetInsertOrExists}">
		                  ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsTrueLabel']}
		                </c:when>
		                <c:otherwise>
		                  ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsFalseLabel']}
		                </c:otherwise>
		              </c:choose>
                      <br />
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncMembership.inTargetStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncMembership.inTargetEnd}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMembership.lastUpdated}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMembership.membershipId}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMembership.membershipId2}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMembership.metadataUpdated}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouper:escapeHtml(grouperSyncMembership.errorMessage)}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMembership.errorTimestamp}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    <c:if test="${not empty grouperSyncMembership.errorCode}">
                     <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${grouperSyncMembership.errorCode}"></c:set>
                     ${textContainer.text[errorText]}
                    </c:if>
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