<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group ${buttonBlock}">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreProvisionerConfigsActions']}" id="more-action-button" class="btn ${buttonSize} ${buttonBlock} dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#provisioner-configs-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#provisioner-configs-more-options li').first().focus();return true;});">
                          ${textContainer.text['provisionerConfigsMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="provisioner-configs-more-options">
                        
                        <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewAssignments&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableAssignmentsActionOption'] }</a></li>
                      
                        <c:if test="${not empty guiProvisionerConfiguration.fullSyncJobName}">
                           <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.viewLogs&jobName=${guiProvisionerConfiguration.fullSyncJobName}'); return false;">${textContainer.text['provisionerConfigsTableViewFullSyncDaemonLogsActionOption'] }</a></li>
                        </c:if>
                       
                         <c:if test="${not empty guiProvisionerConfiguration.incrementalSyncJobName}">
                            <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.viewLogs&jobName=${guiProvisionerConfiguration.incrementalSyncJobName}'); return false;">${textContainer.text['provisionerConfigsTableViewIncrementalSyncDaemonLogsActionOption'] }</a></li>
                         </c:if>
                      
                       <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.canEditProvisionerConfiguration}">
                         <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.diagnostics&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableDiagnosticsActionOption'] }</a></li>
                       </c:if>
                       <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.canEditProvisionerConfiguration}">
                         <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.editProvisionerConfiguration&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableEditDetailsActionOption'] }</a></li>
                       </c:if>
                       
                       <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.groupsProvisionable&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableGroupsProvisionableActionOption'] }</a></li>
                       
                       <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerActivity&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableViewActivityActionOption'] }</a></li>
                       <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerErrors&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableViewErrorsOption'] }</a></li>
                       <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerJobs&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableViewJobsActionOption'] }</a></li>
                       <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerLogs&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableViewLogsActionOption'] }</a></li>
                       
                       <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                            >${textContainer.text['provisionerConfigMoreActionsViewButton'] }</a></li>
                       <%-- <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.runFullSync&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">${textContainer.text['provisionerConfigsTableRunFullSyncActionOption'] }</a></li> --%>
                       <li>&nbsp;</li>
                       
                       <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.canEditProvisionerConfiguration}">
                         
                         <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['provisionerConfigConfirmAllCacheDelete']}')) { return guiV2link('operation=UiV2ProvisionerConfiguration.deleteAllCache&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');}">${textContainer.text['provisionerConfigsTableRemoveAllCachedValuesDetailsActionOption'] }</a></li>
                         <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['provisionerConfigConfirmGroupMemberCacheDelete']}')) { return guiV2link('operation=UiV2ProvisionerConfiguration.deleteGroupMemberCache&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');}">${textContainer.text['provisionerConfigsTableDeleteGroupMemberCacheValuesDetailsActionOption'] }</a></li>
                         <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['provisionerConfigConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2ProvisionerConfiguration.deleteProvisionerConfiguration&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');}">${textContainer.text['provisionerConfigsTableDeleteDetailsActionOption'] }</a></li>
                       </c:if>
                      
                      </ul>

                    </div>