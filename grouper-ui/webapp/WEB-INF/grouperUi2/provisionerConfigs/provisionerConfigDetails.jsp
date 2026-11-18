<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousProvisionerConfigDetailsBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsDetailsDescription'] }</h1></div>
           <div class="span2 pull-right">
             <c:set var="buttonSize" value="btn-medium" />
             <c:set var="buttonBlock" value="btn-block" />
             
             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
       </div>
     </div>
     
     
     <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
         
            <table class="table table-condensed table-striped">
              <tbody>
              
              <tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerConfigIdLabel']}</strong></td>
				    <td>
				     ${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId)}
				    </td>
				</tr>
              
               	<c:set var="syncDetails" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.syncDetails}" />
               	
               	<c:choose>
				    <c:when test="${empty syncDetails}">
				    	<tr>
	           		   		<th colspan="2">
	           		   			<h3>${textContainer.text['provisionerConfigsDetailsNoDetailsAvailable']}</h3>
	           		   		</th>
          		   		</tr>
				    </c:when>
				    <c:otherwise>
				        
				        <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderLastFullSyncStartTimestamp']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.lastFullSyncStartTimestamp)}
             		   	   </td>
             		    </tr>
             		    
				        <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderFullSyncLastRunTimestamp']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.lastFullSyncTimestamp)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderIncrementalSyncLastRunTimestamp']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.lastIncrementalSyncTimestamp)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   	    ${textContainer.text['provisionerConfigsTableHeaderLastFullMetadataSyncStartTimestamp']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.lastFullMetadataSyncStartTimestamp)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderLastFullMetadataSyncRunTimestamp']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.lastFullMetadataSyncTimestamp)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderGroupCount']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.groupCount)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderUserCount']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.userCount)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderMembershipCount']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.membershipCount)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderExceptionCount']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.exceptionCount)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderTargetErrorCount']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.targetErrorCount)}
             		   	   </td>
             		    </tr>
             		    
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${textContainer.text['provisionerConfigsTableHeaderValidationErrorCount']}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(syncDetails.validationErrorCount)}
             		   	   </td>
             		    </tr>
             		    
             		    
             		    
             		    <c:forEach items="${syncDetails.syncJobs}" var="syncJob">
             		    	<tr>
	             		   		<th colspan="2">
	             		   		  <h4>${textContainer.text['provisionerConfigsDetailsSyncJobSyncType']}: ${grouper:escapeHtml(syncJob.gcGrouperSyncJob.syncType)}</h4>
	             		   		</th>
             		   		</tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncJobLastUpdatedTimestamp']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncJob.lastUpdated)}
	             		   	   </td>
	             		    </tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncJobPercentComplete']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncJob.percentComplete)}
	             		   	   </td>
	             		    </tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncJobLastSyncTimestamp']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncJob.lastSyncTimestamp)}
	             		   	   </td>
	             		    </tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncJobLastTimeWorkWasDoneTimestamp']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncJob.lastTimeWorkWasDone)}
	             		   	   </td>
	             		    </tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncJobHeartbeat']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncJob.heartbeat)}
	             		   	   </td>
	             		    </tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncJobErrorMessage']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncJob.errorMessage)}
	             		   	   </td>
	             		    </tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncJobErrorTimestamp']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncJob.errorTimestamp)}
	             		   	   </td>
	             		    </tr>
             		   		<tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogDescription']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.description)}
	             		   	   </td>
	             		    </tr>
             		   		<tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogOwnerId']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.grouperSyncOwnerId)}
	             		   	   </td>
	             		    </tr>
             		   		<tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogJobTookMillis']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.jobTookMillis)}
	             		   	   </td>
	             		    </tr>
             		   		<tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogLastUpdated']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.lastUpdated)}
	             		   	   </td>
	             		    </tr>
             		   		<tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogRecordsChanged']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.recordsChanged)}
	             		   	   </td>
	             		    </tr>
             		   		<tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogRecordsProcessed']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.recordsProcessed)}
	             		   	   </td>
	             		    </tr>
             		   		<tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogRecordsServer']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.server)}
	             		   	   </td>
	             		    </tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogStatus']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.statusDb)}
	             		   	   </td>
	             		    </tr>
	             		    <tr>
	             		   	  <td style="vertical-align: top; white-space: nowrap;">
	             		   	    <strong><label>
	             		   		  ${textContainer.text['provisionerConfigsDetailsSyncLogSyncTimestamp']}
	             		   		</label></strong>
	             		   	  </td>
	             		   	  <td>
	             		   	 	${grouper:escapeHtml(syncJob.gcGrouperSyncLog.syncTimestamp)}
	             		   	   </td>
	             		    </tr>
             		    </c:forEach>
				    </c:otherwise>
				</c:choose>
              </tbody>
            </table>
            
            <div class="span6">
                   
              <a class="btn btn-cancel" role="button"
                   onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                   >${textContainer.text['provisionerConfigEditFormCancelButton'] }</a>
            
            </div>
            
	  </div>
	</div>
