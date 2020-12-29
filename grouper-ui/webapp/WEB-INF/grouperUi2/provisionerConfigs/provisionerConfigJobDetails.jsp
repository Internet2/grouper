<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousProvisionerConfigurationsJobDetailsBreadcrumb'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousProvisionerConfigurationsJobDetailsMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                  
                    <p style="margin-top: -1em; margin-bottom: 1em">
                    ${textContainer.text['provisionerConfigIdLabel']}: 
                    ${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId)}
                    </p>
                  </div>
                </div>
              </div>
              
            </div>
              
			<table class="table table-condensed table-striped">
                <tbody>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobsTableHeaderSyncType'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.syncType}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobsTableHeaderJobState'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.jobStateDb}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobsTableHeaderPercentComplete'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.percentComplete}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobDetailsLastSyncIndex'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.lastSyncIndex}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobDetailsLastSyncStartTime'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.lastSyncStart}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobDetailsLastSyncTime'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.lastSyncTimestamp}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobDetailsLastTimeWorkWasDone'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.lastTimeWorkWasDone}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobDetailsLastUpdated'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.lastUpdated}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobDetailsErrorMessage'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.errorMessage}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                     <tr>
                       <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerJobDetailsErrorTime'] }</strong></td>
                       <td>
                       ${grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.errorTimestamp}
                         <br />
                         <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                       </td>
                     </tr>
                </tbody>
            </table>
