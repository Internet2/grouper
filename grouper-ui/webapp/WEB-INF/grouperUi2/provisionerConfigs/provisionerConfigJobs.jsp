<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousProvisionerConfigurationsJobsBreadcrumb'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsJobsMainDescription'] }</h1></div>
                  <div class="span2 pull-right">
                   <c:set var="buttonSize" value="btn-medium" />
                   <c:set var="buttonBlock" value="btn-block" />
                   <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
              
            </div>
              
              
			<div class="row-fluid">
			
			    <c:choose>
			      <c:when test="${fn:length(grouperRequestContainer.provisionerConfigurationContainer.provisionerJobs) > 0}">
			        <div>
			        <form class="form-inline form-small" name="provisionerConfigJobsFormName" id="provisionerConfigJobsFormId">
			        <table class="table table-hover table-bordered table-striped table-condensed data-table">
			          <thead>        
			            <tr>
			              <th>${textContainer.text['provisionerJobsTableHeaderSyncType']}</th>
			              <th>${textContainer.text['provisionerJobsTableHeaderJobState']}</th>
			              <th>${textContainer.text['provisionerJobsTableHeaderPercentComplete']}</th>
			              <th>${textContainer.text['provisionerJobsTableHeaderActions']}</th>
			            </tr>
			            </thead>
			            <tbody>
			              <c:set var="i" value="0" />
			              <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.provisionerJobs}" var="provisionerJob">
			              
			                <tr>
			                   <td style="white-space: nowrap;">
                    			${provisionerJob.syncType}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${provisionerJob.jobStateDb}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${provisionerJob.percentComplete}
			                   </td>
			                   
			                   <td>
			                     <div class="btn-group">
			                           <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
			                             aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
			                             ${textContainer.text['provisionerConfigRowActionsButton'] }
			                             <span class="caret"></span>
			                           </a>
			                           <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
			                             <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerJobDetails&provisionerJobId=${provisionerJob.id}&provisionerConfigId=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}');">${textContainer.text['provisionerJobsTableViewJobDetailsActionOption'] }</a></li>
			                           </ul>
			                         </div>
			                   </td>
			                   
			                   </tr>
			              </c:forEach>
			             
			             </tbody>
			         </table>
			         </form>
			        </div>
			       <%--  <div class="data-table-bottom gradient-background">
		                <grouper:paging2 guiPaging="${grouperRequestContainer.provisionerConfigurationContainer.guiPaging}" 
		                	formName="provisionerConfigLogsPagingForm" ajaxFormIds="provisionerConfigLogsFormId"
		                    refreshOperation="../app/UiV2ProvisionerConfiguration.viewProvisionerLogs?provisionerConfigId=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}" />
	            	</div> --%>
			      </c:when>
			      <c:otherwise>
			        <div class="row-fluid">
			          <div class="span9">
				          <p><b>
				          ${textContainer.text['provisionerConfigNoJobsFound'] } 
				          ${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}
				          </b></p>
			          </div>
			        </div>
			      </c:otherwise>
			    </c:choose>
			    
			  </div>
