<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsMainDescription'] }</h1></div>
                  <div class="span2 pull-right">
                  
                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreProvisionerConfigsActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#provisioner-configs-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#provisioner-configs-more-options li').first().focus();return true;});">
                          ${textContainer.text['provisionerConfigsMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="provisioner-configs-more-options">
                        <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.addProvisionerConfiguration'); return false;"
                              >${textContainer.text['provisionerConfigMoreActionsAddButton'] }</a></li>
                      </ul>

                    </div>
                  
                  
                  </div>
                </div>
              </div>
            </div>
              
			<div class="row-fluid">
			  
			    <c:choose>
			      <c:when test="${fn:length(grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfigurations) > 0}">
			        
			        <table class="table table-hover table-bordered table-striped table-condensed data-table">
			          <thead>        
			            <tr>
			              <th>${textContainer.text['provisionerConfigsTableHeaderConfigId']}</th>
			              <th>${textContainer.text['provisionerConfigsTableHeaderType']}</th>
			              <th>${textContainer.text['provisionerConfigsTableHeaderFullSyncLastRunTimestamp']}</th>
			              <th>${textContainer.text['provisionerConfigsTableHeaderIncrementalSyncLastRunTimestamp']}</th>
			              <th>${textContainer.text['provisionerConfigsTableHeaderGroupCount']}</th>
			              <th>${textContainer.text['provisionerConfigsTableHeaderUserCount']}</th>
			              <th>${textContainer.text['provisionerConfigsTableHeaderMembershipCount']}</th>
			              <th>${textContainer.text['provisionerConfigsTableHeaderActions']}</th>
			            </tr>
			            </thead>
			            <tbody>
			              <c:set var="i" value="0" />
			              <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfigurations}" var="guiProvisionerConfiguration">
			              
			                <tr>
			                   <td style="white-space: nowrap;">
			                   
			                   <a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigDetails&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}');">
                    			${grouper:escapeHtml(guiProvisionerConfiguration.provisionerConfiguration.configId)}</a>
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${guiProvisionerConfiguration.provisionerConfiguration.title}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${guiProvisionerConfiguration.provisionerConfiguration.syncDetails.lastFullSyncTimestamp}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${guiProvisionerConfiguration.provisionerConfiguration.syncDetails.lastIncrementalSyncTimestamp}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${guiProvisionerConfiguration.provisionerConfiguration.syncDetails.groupCount}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${guiProvisionerConfiguration.provisionerConfiguration.syncDetails.userCount}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${guiProvisionerConfiguration.provisionerConfiguration.syncDetails.membershipCount}
			                   </td>
			                   
			                   <td>

                             <c:set var="buttonSize" value="btn-mini" />
                             <c:set var="buttonBlock" value="" />
                             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>

			                   </td>
			              </c:forEach>
			             
			             </tbody>
			         </table>
			        
			      </c:when>
			      <c:otherwise>
			        <div class="row-fluid">
			          <div class="span9"> <p><b>${textContainer.text['provisionerConfigNoConfiguredProvisionerConfigsFound'] }</b></p></div>
			        </div>
			      </c:otherwise>
			    </c:choose>
			    
			  </div>
