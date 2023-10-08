<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

<div class="row-fluid">
   <c:choose>
     <c:when test="${fn:length(grouperRequestContainer.provisionerConfigurationContainer.provisionerActivity) > 0}">
       <div>
       <table class="table table-hover table-bordered table-striped table-condensed data-table">
         <thead>        
         
           <tr>
             <th>${textContainer.text['provisionerActivityTableHeaderType']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderAction']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderName']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionable']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTarget']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetInsertOrExists']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderInTargetEnd']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionableStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderProvisionableEnd']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastUpdated']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastObjectSyncStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastObjectSync']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastObjectMetadataSyncStart']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastObjectMetadataSync']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderObjectAttributeValueCache0']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderObjectAttributeValueCache1']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderObjectAttributeValueCache2']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderObjectAttributeValueCache3']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderMetadataUpdated']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorMessage']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorTimestamp']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderErrorCode']}</th>
             <th>${textContainer.text['provisionerActivityTableHeaderLastTimeWorkWasDone']}</th>
           </tr>
           </thead>
           <tbody>
             <c:set var="i" value="0" />
             <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.provisionerActivity}" var="provisionerActivity">
             
               <tr>
                  <td style="white-space: nowrap;">
                  ${provisionerActivity.type}
                  </td>
                  
                  <td style="white-space: nowrap;">
                  ${provisionerActivity.action}
                  </td>
                  
                  <td style="white-space: nowrap;">
                   ${provisionerActivity.name}
                  </td>
                  
                  <td style="white-space: nowrap;">
                  <c:choose>
                    <c:when test="${provisionerActivity.provisionable}">
                      ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
                    </c:when>
                    <c:otherwise>
                      ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
                    </c:otherwise>
                  </c:choose>
                  </td>
                  
                  <td style="white-space: nowrap;">
                    <c:choose>
                      <c:when test="${provisionerActivity.inTarget}">
                        ${textContainer.text['provisioningConfigTableHeaderInTargetYesLabel']}
                      </c:when>
                      <c:otherwise>
                        ${textContainer.text['provisioningConfigTableHeaderInTargetNoLabel']}
                      </c:otherwise>
                   </c:choose>
                  </td>
                  
                  <td style="white-space: nowrap;">
                      <c:choose>
                    <c:when test="${provisionerActivity.inTargetInsertOrExists}">
                      ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsTrueLabel']}
                    </c:when>
                    <c:otherwise>
                      ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsFalseLabel']}
                    </c:otherwise>
                  </c:choose>
                      <br />
                  </td>
                  
                  <td style="white-space: nowrap;">
                  ${provisionerActivity.inTargetStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                  ${provisionerActivity.inTargetEnd}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.provisionableStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.provisionableEnd}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.lastUpdated}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.lastObjectSyncStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.lastObjectSync}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.lastObjectMetadataSyncStart}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.lastObjectMetadataSync}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.objectAttributeValueCache0}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.objectAttributeValueCache1}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.objectAttributeValueCache2}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.objectAttributeValueCache3}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.metadataUpdated}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${grouper:escapeHtml(provisionerActivity.errorMessage)}
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.errorTimestamp}
                  </td>
                  
                  <td style="white-space: nowrap;">
                  
                  <c:if test="${not empty provisionerActivity.errorCode}">
                    <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${provisionerActivity.errorCode}"></c:set>
                    ${textContainer.text[errorText]}
                   </c:if>
                  </td>
                  
                  <td style="white-space: nowrap;">
                    ${provisionerActivity.lastTimeWorkWasDone}
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