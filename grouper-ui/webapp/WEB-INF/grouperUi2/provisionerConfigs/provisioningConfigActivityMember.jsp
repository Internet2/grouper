<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

<div class="row-fluid">
   <c:choose>
     <c:when test="${fn:length(grouperRequestContainer.provisionerConfigurationContainer.activityForMember) > 0}">
       <div>
       <table class="table table-hover table-bordered table-striped table-condensed data-table">
         <thead>        
           <tr>
             <th>${textContainer.text['provisionerActivityTableHeaderMemberId']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderSourceId']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderSubjectId']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderSubjectIdentifier']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionable']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTarget']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetInsertOrExists']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetEnd']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionableStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionableEnd']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastUpdated']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastUserSyncStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastUserSync']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastUserMetadataSyncStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastUserMetadataSync']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderEntityAttributeValueCache0']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderEntityAttributeValueCache1']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderEntityAttributeValueCache2']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderEntityAttributeValueCache3']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderMetadataUpdated']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorMessage']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorTimestamp']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorCode']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastTimeWorkWasDone']}</th>
           </tr>
           </thead>
           <tbody>
             <c:set var="i" value="0" />
             <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.activityForMember}" var="grouperSyncMember">
             
               <tr>
                  <td style="white-space: nowrap;">
                	${grouperSyncMember.memberId}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncMember.sourceId}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	 ${grouperSyncMember.subjectId}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	 ${grouperSyncMember.subjectIdentifier}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	<c:choose>
	                  <c:when test="${grouperSyncMember.provisionable}">
	                    ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
	                  </c:when>
	                  <c:otherwise>
	                    ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
	                  </c:otherwise>
                 	</c:choose>
                  </td>
                  
                  <td style="white-space: nowrap;">
	                  <c:choose>
	                    <c:when test="${grouperSyncMember.inTarget}">
	                      ${textContainer.text['provisioningConfigTableHeaderInTargetYesLabel']}
	                    </c:when>
	                    <c:otherwise>
	                      ${textContainer.text['provisioningConfigTableHeaderInTargetNoLabel']}
	                    </c:otherwise>
	                 </c:choose>
                  </td>
                  
                  <td style="white-space: nowrap;">
                      <c:choose>
		                <c:when test="${grouperSyncMember.inTargetInsertOrExists}">
		                  ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsTrueLabel']}
		                </c:when>
		                <c:otherwise>
		                  ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsFalseLabel']}
		                </c:otherwise>
		              </c:choose>
                      <br />
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncMember.inTargetStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                	${grouperSyncMember.inTargetEnd}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.provisionableStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.provisionableEnd}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.lastUpdated}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.lastUserSyncStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.lastUserSync}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.lastUserMetadataSyncStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.lastUserMetadataSync}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.entityAttributeValueCache0}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncGroup.entityAttributeValueCache1}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.entityAttributeValueCache2}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.entityAttributeValueCache3}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.metadataUpdated}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouper:escapeHtml(grouperSyncMember.errorMessage)}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.errorTimestamp}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    <c:if test="${not empty grouperSyncMember.errorCode}">
                     <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${grouperSyncMember.errorCode}"></c:set>
                     ${textContainer.text[errorText]}
                    </c:if>
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouperSyncMember.lastTimeWorkWasDone}
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