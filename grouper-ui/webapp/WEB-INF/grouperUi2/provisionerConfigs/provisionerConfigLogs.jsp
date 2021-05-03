<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousProvisionerConfigurationsLogsBreadcrumb'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsLogsMainDescription'] }</h1></div>
                  <div class="span2 pull-right">
                    <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
              
            </div>
              
              
			<div class="row-fluid">
			
			    <c:choose>
			      <c:when test="${fn:length(grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerLogs) > 0}">
			        <div>
			        <form class="form-inline form-small" name="provisionerConfigLogsFormName" id="provisionerConfigLogsFormId">
			        <table class="table table-hover table-bordered table-striped table-condensed data-table">
			          <thead>        
			            <tr>
			              <th>${textContainer.text['provisionerLogsTableHeaderSyncStartTimestamp']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderSyncTimestamp']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderLogType']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderOwner']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderStatus']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderRecordsProcessed']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderRecordsChanged']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderJobTookMillis']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderServer']}</th>
			              <th>${textContainer.text['provisionerLogsTableHeaderDescription']}</th>
			              <%-- <th>${textContainer.text['provisionerLogsTableHeaderActions']}</th> --%>
			            </tr>
			            </thead>
			            <tbody>
			              <c:set var="i" value="0" />
			              <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerLogs}" var="guiProvisionerLog">
			              
			                <tr>
			                	
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.gcGrouperSyncLogWithOwner.gcGrouperSyncLog.syncTimestampStart}
			                   </td>
			                	
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.gcGrouperSyncLogWithOwner.gcGrouperSyncLog.syncTimestamp}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.gcGrouperSyncLogWithOwner.logType}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.owner}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.gcGrouperSyncLogWithOwner.gcGrouperSyncLog.statusDb}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.gcGrouperSyncLogWithOwner.gcGrouperSyncLog.recordsProcessed}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.gcGrouperSyncLogWithOwner.gcGrouperSyncLog.recordsChanged}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.gcGrouperSyncLogWithOwner.gcGrouperSyncLog.jobTookMillis}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${guiProvisionerLog.gcGrouperSyncLogWithOwner.gcGrouperSyncLog.server}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                   <grouper:abbreviateTextarea text="${guiProvisionerLog.description}" 
							 	showCharCount="30" cols="20" rows="3"/>
			                   </td>
			                   
			                   </tr>
			              </c:forEach>
			             
			             </tbody>
			         </table>
			         </form>
			        </div>
			        <div class="data-table-bottom gradient-background">
		                <grouper:paging2 guiPaging="${grouperRequestContainer.provisionerConfigurationContainer.guiPaging}" 
		                	formName="provisionerConfigLogsPagingForm" ajaxFormIds="provisionerConfigLogsFormId"
		                    refreshOperation="../app/UiV2ProvisionerConfiguration.viewProvisionerLogs?provisionerConfigId=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}" />
	            	</div>
			      </c:when>
			      <c:otherwise>
			        <div class="row-fluid">
			          <div class="span9">
				          <p><b>
				          ${textContainer.text['provisionerConfigNoLogsFound'] } 
				          ${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}
				          </b></p>
			          </div>
			        </div>
			      </c:otherwise>
			    </c:choose>
			    
			  </div>
