<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousGshTemplatesOverallBreadcrumb'] }</li>
              </ul>
               
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousGshTemplatesMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="gshTemplateConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
			<div class="row-fluid">
			  
			    <c:choose>
			      <c:when test="${fn:length(grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfigurations) > 0}">
			        
			        <table class="table table-hover table-bordered table-striped table-condensed data-table">
			          <thead>        
			            <tr>
			              <th>${textContainer.text['gshTemplatesTableHeaderConfigId']}</th>
			              <th>${textContainer.text['gshTemplatesTableHeaderEnabled']}</th>
			              <th>${textContainer.text['gshTemplatesTableHeaderActions']}</th>
			            </tr>
			            </thead>
			            <tbody>
			              <c:set var="i" value="0" />
			              <c:forEach items="${grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfigurations}" var="guiGshTemplateConfiguration">
			              
			                <tr>
			                   <td style="white-space: nowrap;">
			                    ${grouper:escapeHtml(guiGshTemplateConfiguration.gshTemplateConfiguration.configId)}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                     <c:if test="${guiGshTemplateConfiguration.gshTemplateConfiguration.enabled == true}">
			                      ${textContainer.text['gshTemplatesTableEnabledTrueValue']}
			                     </c:if>
			                     <c:if test="${guiGshTemplateConfiguration.gshTemplateConfiguration.enabled == false }">
			                      ${textContainer.text['gshTemplatesTableEnabledFalseValue']}
			                     </c:if>
			                   </td>
			                  
			                   <td>
			                     <div class="btn-group">
			                           <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
			                             aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
			                             ${textContainer.text['gshTemplatesRowActionsButton'] }
			                             <span class="caret"></span>
			                           </a>
			                           <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
			                             
			                             <c:if test="${guiGshTemplateConfiguration.gshTemplateConfiguration.enabled == true}">
					                      <li><a href="#" onclick="return guiV2link('operation=UiV2GshTemplateConfig.disableGshTemplate&gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');">${textContainer.text['gshTemplatesTableDisableActionOption'] }</a></li>
					                     </c:if>
					                     
					                     <c:if test="${guiGshTemplateConfiguration.gshTemplateConfiguration.enabled == false}">
					                      <li><a href="#" onclick="return guiV2link('operation=UiV2GshTemplateConfig.enableGshTemplate&gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');">${textContainer.text['gshTemplatesTableEnableActionOption'] }</a></li>
					                     </c:if>
                               
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GshTemplateConfig.editGshTemplate&gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');">${textContainer.text['gshTemplatesTableEditDetailsActionOption'] }</a></li>
                               
                               <c:if test="${guiGshTemplateConfiguration.gshTemplateConfiguration.defaultRunButtonType == 'group'}">
                                 <li><a href="#" onclick="return guiV2link('operation=UiV2Template.newTemplate&groupId=${guiGshTemplateConfiguration.gshTemplateConfiguration.groupId}&templateType=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');">${textContainer.text['gshTemplatesTableRunTemplateActionOption'] }</a></li>
                               </c:if>
                               <c:if test="${guiGshTemplateConfiguration.gshTemplateConfiguration.defaultRunButtonType == 'folder'}">
                                <li><a href="#" onclick="return guiV2link('operation=UiV2Template.newTemplate&stemId=${guiGshTemplateConfiguration.gshTemplateConfiguration.folderId}&templateType=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');">${textContainer.text['gshTemplatesTableRunTemplateActionOption'] }</a></li>
                               </c:if>
                               
                               
<li>&nbsp;</li>			                             
			                             <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['gshTemplatesConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2GshTemplateConfig.deleteGshTemplate&gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');}">${textContainer.text['gshTemplatesTableDeleteDetailsActionOption'] }</a></li>
			                           </ul>
			                         </div>
			                   </td>
			              </c:forEach>
			             
			             </tbody>
			         </table>
			        
			      </c:when>
			      <c:otherwise>
			        <div class="row-fluid">
			          <div class="span9"> <p><b>${textContainer.text['gshTemplatesNoConfiguredGshTemplates'] }</b></p></div>
			        </div>
			      </c:otherwise>
			    </c:choose>
			    
			  </div>
