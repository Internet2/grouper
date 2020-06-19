<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousProvisionerConfigurationsMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
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
			              <th>${textContainer.text['provisionerConfigsTableHeaderEnabled']}</th>
			              <th>${textContainer.text['provisionerConfigsTableHeaderActions']}</th>
			            </tr>
			            </thead>
			            <tbody>
			              <c:set var="i" value="0" />
			              <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfigurations}" var="guiProvisionerConfiguration">
			              
			                <tr>
			                   <td style="white-space: nowrap;">
			                    ${grouper:escapeHtml(guiProvisionerConfiguration.provisionerConfiguration.configId)}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${guiProvisionerConfiguration.provisionerConfiguration.title}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                     <c:if test="${guiProvisionerConfiguration.provisionerConfiguration.enabled == true}">
			                      ${textContainer.text['provisionerConfigsTableEnabledTrueValue']}
			                     </c:if>
			                     <c:if test="${guiProvisionerConfiguration.provisionerConfiguration.enabled == false }">
			                      ${textContainer.text['provisionerConfigsTableEnabledFalseValue']}
			                     </c:if>
			                   </td>
			                  
			                   <td>
			                     <div class="btn-group">
			                           <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
			                             aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
			                             ${textContainer.text['provisionerConfigRowActionsButton'] }
			                             <span class="caret"></span>
			                           </a>
			                           <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
			                             <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurationDetails&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}');">${textContainer.text['provisionerConfigsTableViewDetailsActionOption'] }</a></li>
			                             <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.editProvisionerConfiguration&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}');">${textContainer.text['provisionerConfigsTableEditDetailsActionOption'] }</a></li>
			                             
			                             <c:if test="${guiProvisionerConfiguration.provisionerConfiguration.enabled == true}">
					                      <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.disableProvisionerConfiguration&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}');">${textContainer.text['provisionerConfigsTableDisableActionOption'] }</a></li>
					                     </c:if>
					                     
					                     <c:if test="${guiProvisionerConfiguration.provisionerConfiguration.enabled == false}">
					                      <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.enableProvisionerConfiguration&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}');">${textContainer.text['provisionerConfigsTableEnableActionOption'] }</a></li>
					                     </c:if>
			                             
			                             <li><a href="#" onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['provisionerConfigConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2ProvisionerConfiguration.deleteProvisionerConfiguration&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}');}">${textContainer.text['provisionerConfigsTableDeleteDetailsActionOption'] }</a></li>
			                           </ul>
			                         </div>
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
