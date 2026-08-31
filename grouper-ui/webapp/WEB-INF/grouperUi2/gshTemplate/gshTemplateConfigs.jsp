<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('gshTemplateConfigsPageTitle')}

            <div class="bread-header-container">
            <grouper:browserPage jspName="gshTemplateConfigs" />
              <ul class="breadcrumb">
                  <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
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
			  
          <p class="lead" id="templateHeader"></p>

          <script>
          function gshTemplatesFilter() {
            ajax('../app/UiV2GshTemplateConfig.viewGshTemplates?filterTemplateType=' + encodeURIComponent($('#gshFilterTypeId').val())
              + '&filterTemplateMode=' + encodeURIComponent($('#gshFilterModeId').val())
              + '&filterCompileStatus=' + encodeURIComponent($('#gshFilterStatusId').val()));
            return false;
          }
          </script>

          <div class="row-fluid">
            <div class="span12" style="background: #f4f7fb; border: 1px solid #ddd; border-radius: 4px; padding: 12px 15px; margin-bottom: 12px;">
              <table class="table table-condensed" style="background: transparent; margin-bottom: 0;">
                <tbody>
                  <tr>
                    <td style="vertical-align: top; white-space: nowrap; border-top: none; width: 1%;"><strong><label for="gshFilterTypeId" style="font-weight: bold;">${textContainer.text['gshTemplatesFilterTypeLabel']}</label></strong></td>
                    <td style="border-top: none;">
                      <select id="gshFilterTypeId" onchange="return gshTemplatesFilter();">
                        <option value="">${textContainer.text['gshTemplatesFilterAny']}</option>
                      <option value="abac" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'abac' ? 'selected' : ''}>abac</option>
                      <option value="customUi" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'customUi' ? 'selected' : ''}>customUi</option>
                      <option value="daemon" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'daemon' ? 'selected' : ''}>daemon</option>
                      <option value="daemonChangeLog" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'daemonChangeLog' ? 'selected' : ''}>daemonChangeLog</option>
                      <option value="gsh" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'gsh' ? 'selected' : ''}>gsh</option>
                      <option value="hook" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'hook' ? 'selected' : ''}>hook</option>
                      <option value="library" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'library' ? 'selected' : ''}>library</option>
                      <option value="provisioner" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'provisioner' ? 'selected' : ''}>provisioner</option>
                      <option value="report" ${grouperRequestContainer.gshTemplateContainer.filterTemplateType == 'report' ? 'selected' : ''}>report</option>
                      </select>
                      <br /><span class="description">${textContainer.text['gshTemplatesFilterTypeDescription']}</span>
                    </td>
                  </tr>
                  <tr>
                    <td style="vertical-align: top; white-space: nowrap; width: 1%;"><strong><label for="gshFilterModeId" style="font-weight: bold;">${textContainer.text['gshTemplatesFilterModeLabel']}</label></strong></td>
                    <td>
                      <select id="gshFilterModeId" onchange="return gshTemplatesFilter();">
                        <option value="">${textContainer.text['gshTemplatesFilterAny']}</option>
                        <option value="compiled" ${grouperRequestContainer.gshTemplateContainer.filterTemplateMode == 'compiled' ? 'selected' : ''}>compiled</option>
                        <option value="interpreted" ${grouperRequestContainer.gshTemplateContainer.filterTemplateMode == 'interpreted' ? 'selected' : ''}>interpreted</option>
                      </select>
                      <br /><span class="description">${textContainer.text['gshTemplatesFilterModeDescription']}</span>
                    </td>
                  </tr>
                  <tr>
                    <td style="vertical-align: top; white-space: nowrap; width: 1%;"><strong><label for="gshFilterStatusId" style="font-weight: bold;">${textContainer.text['gshTemplatesFilterCompileStatusLabel']}</label></strong></td>
                    <td>
                      <select id="gshFilterStatusId" onchange="return gshTemplatesFilter();">
                        <option value="">${textContainer.text['gshTemplatesFilterAny']}</option>
                        <option value="ok" ${grouperRequestContainer.gshTemplateContainer.filterCompileStatus == 'ok' ? 'selected' : ''}>${textContainer.text['gshTemplatesCompileStatusOk']}</option>
                        <option value="failed" ${grouperRequestContainer.gshTemplateContainer.filterCompileStatus == 'failed' ? 'selected' : ''}>${textContainer.text['gshTemplatesCompileStatusFailed']}</option>
                        <option value="fileMissing" ${grouperRequestContainer.gshTemplateContainer.filterCompileStatus == 'fileMissing' ? 'selected' : ''}>${textContainer.text['gshTemplatesCompileStatusFileMissing']}</option>
                      </select>
                      <br /><span class="description">${textContainer.text['gshTemplatesFilterCompileStatusDescription']}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
			    <c:choose>
			      <c:when test="${fn:length(grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfigurations) > 0}">
			        
			        <table class="table table-hover table-bordered table-striped table-condensed data-table">
			          <thead>        
			            <tr>
			              <th>${textContainer.text['gshTemplatesTableHeaderConfigId']}</th>
              <th>${textContainer.text['gshTemplatesTableHeaderType']}</th>
              <th>${textContainer.text['gshTemplatesTableHeaderMode']}</th>
              <th>${textContainer.text['gshTemplatesTableHeaderSource']}</th>
              <th>${textContainer.text['gshTemplatesTableHeaderCompileStatus']}</th>
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
			                    <c:if test="${guiGshTemplateConfiguration.defaultRunButtonResolutionError}">
			                      <span class="text-warning" rel="tooltip" tabindex="0" data-placement="right" data-original-title="${textContainer.textEscapeDouble['gshTemplatesDefaultRunButtonTargetNotFound']}" style="margin-left: 4px;">
			                        <i class="fa fa-exclamation-triangle" aria-hidden="true"></i>
			                        <span class="sr-only">${textContainer.text['gshTemplatesConfigurationWarning']}</span>
			                      </span>
			                    </c:if>
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${grouper:escapeHtml(guiGshTemplateConfiguration.templateType)}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${grouper:escapeHtml(guiGshTemplateConfiguration.templateMode)}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${grouper:escapeHtml(guiGshTemplateConfiguration.sourceLocation)}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    <c:choose>
			                     <c:when test="${guiGshTemplateConfiguration.compileStatus == 'ok'}">
			                      <span class="label label-success" rel="tooltip" data-placement="right" data-original-title="${grouper:escapeHtml(guiGshTemplateConfiguration.lastCompiled)}">${textContainer.text['gshTemplatesCompileStatusOk']}</span>
			                     </c:when>
			                     <c:when test="${guiGshTemplateConfiguration.compileStatus == 'failed'}">
			                      <span class="label label-important" rel="tooltip" data-html="true" data-placement="right" data-original-title="${grouper:escapeHtml(guiGshTemplateConfiguration.compileStatusDetail)}">${textContainer.text['gshTemplatesCompileStatusFailed']}</span>
			                     </c:when>
			                     <c:when test="${guiGshTemplateConfiguration.compileStatus == 'fileMissing'}">
			                      <span class="label label-warning">${textContainer.text['gshTemplatesCompileStatusFileMissing']}</span>
			                     </c:when>
			                     <c:otherwise>&mdash;</c:otherwise>
			                    </c:choose>
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
			                           <a data-toggle="dropdown" type="dropdown" href="#" id="actions_${grouper:escapeHtml(guiGshTemplateConfiguration.gshTemplateConfiguration.configId)}_id" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
			                             aria-haspopup="true" aria-expanded="false" role="button" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
			                             ${textContainer.text['gshTemplatesRowActionsButton'] }
			                             <span class="caret"></span>
			                           </a>
			                           <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
			                             
			                             <c:if test="${guiGshTemplateConfiguration.gshTemplateConfiguration.enabled == true}">
					                      <li><a href="#" onclick="ajax('../app/UiV2GshTemplateConfig.disableGshTemplate?gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}'); return false;">${textContainer.text['gshTemplatesTableDisableActionOption'] }</a></li>
					                     </c:if>
					                     
					                     <c:if test="${guiGshTemplateConfiguration.gshTemplateConfiguration.enabled == false}">
					                      <li><a href="#" onclick="ajax('../app/UiV2GshTemplateConfig.enableGshTemplate?gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}'); return false;">${textContainer.text['gshTemplatesTableEnableActionOption'] }</a></li>
					                     </c:if>
                               
                               <li><a href="?operation=UiV2GshTemplateConfig.editGshTemplate&gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GshTemplateConfig.editGshTemplate&gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');">${textContainer.text['gshTemplatesTableEditDetailsActionOption'] }</a></li>
                               
                               <c:if test="${not guiGshTemplateConfiguration.defaultRunButtonResolutionError and guiGshTemplateConfiguration.gshTemplateConfiguration.defaultRunButtonType == 'group'}">
                                 <li><a id="groupTemplateActionsRunTemplateButton" href="?operation=UiV2Template.newTemplate&groupId=${guiGshTemplateConfiguration.defaultRunButtonTargetId}&templateType=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Template.newTemplate&groupId=${guiGshTemplateConfiguration.defaultRunButtonTargetId}&templateType=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');">${textContainer.text['gshTemplatesTableRunTemplateActionOption'] }</a></li>
                               </c:if>
                               <c:if test="${not guiGshTemplateConfiguration.defaultRunButtonResolutionError and guiGshTemplateConfiguration.gshTemplateConfiguration.defaultRunButtonType == 'folder'}">
                                <li><a id="stemTemplateActionsRunTemplateButton" href="?operation=UiV2Template.newTemplate&stemId=${guiGshTemplateConfiguration.defaultRunButtonTargetId}&templateType=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Template.newTemplate&stemId=${guiGshTemplateConfiguration.defaultRunButtonTargetId}&templateType=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}');">${textContainer.text['gshTemplatesTableRunTemplateActionOption'] }</a></li>
                               </c:if>
                               <li><a href="#" onclick="ajax('../app/UiV2Template.test?gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}'); return false;"
                                    >${textContainer.text['gshTemplatesTableTestActionOption'] }</a></li>
                               
                               
<li>&nbsp;</li>			                             
			                             <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['gshTemplatesConfirmDeleteConfig']}')) { ajax('../app/UiV2GshTemplateConfig.deleteGshTemplate?gshTemplateConfigId=${guiGshTemplateConfiguration.gshTemplateConfiguration.configId}'); return false;}">${textContainer.text['gshTemplatesTableDeleteDetailsActionOption'] }</a></li>
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
