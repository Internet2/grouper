<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('customUiConfigsPageTitle')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousCustomUiOverallBreadcrumb'] }</li>
              </ul>
               
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousCustomUiMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="customUiConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
      <div class="row-fluid">
        
          <c:choose>
            <c:when test="${fn:length(grouperRequestContainer.customUiContainer.guiCustomUiConfigurations) > 0}">
              
              <table class="table table-hover table-bordered table-striped table-condensed data-table">
                <thead>        
                  <tr>
                    <th>${textContainer.text['customUiConfigsTableHeaderConfigId']}</th>
                    <th>${textContainer.text['customUiConfigsTableHeaderEnabled']}</th>
                    <th>${textContainer.text['customUiConfigsTableHeaderActions']}</th>
                  </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach items="${grouperRequestContainer.customUiContainer.guiCustomUiConfigurations}" var="guiCustomUiConfiguration">
                    
                      <tr>
                         <td style="white-space: nowrap;">
                          ${grouper:escapeHtml(guiCustomUiConfiguration.customUiConfiguration.configId)}
                         </td>
                         
                         <td style="white-space: nowrap;">
                           <c:if test="${guiCustomUiConfiguration.customUiConfiguration.enabled == true}">
                            ${textContainer.text['customUiConfigsTableEnabledTrueValue']}
                           </c:if>
                           <c:if test="${guiCustomUiConfiguration.customUiConfiguration.enabled == false }">
                            ${textContainer.text['customUiConfigsTableEnabledFalseValue']}
                           </c:if>
                         </td>
                        
                         <td>
                           <div class="btn-group">
                                 <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                                   aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                                   ${textContainer.text['customUiConfigsRowActionsButton'] }
                                   <span class="caret"></span>
                                 </a>
                                 <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                                   
                               <c:if test="${guiCustomUiConfiguration.customUiConfiguration.enabled == true}">
                                <li><a href="#" onclick="return guiV2link('operation=UiV2CustomUiConfig.disableCustomUiConfig&customUiConfigId=${guiCustomUiConfiguration.customUiConfiguration.configId}');">${textContainer.text['customUiConfigsTableDisableActionOption'] }</a></li>
                               </c:if>
                               
                               <c:if test="${guiCustomUiConfiguration.customUiConfiguration.enabled == false}">
                                <li><a href="#" onclick="return guiV2link('operation=UiV2CustomUiConfig.enableCustomUiConfig&customUiConfigId=${guiCustomUiConfiguration.customUiConfiguration.configId}');">${textContainer.text['customUiConfigsTableEnableActionOption'] }</a></li>
                               </c:if>
                               
                               <li><a href="#" onclick="return guiV2link('operation=UiV2CustomUiConfig.editCustomUiConfig&customUiConfigId=${guiCustomUiConfiguration.customUiConfiguration.configId}');">${textContainer.text['customUiConfigsTableEditDetailsActionOption'] }</a></li>
                               
                               <c:if test="${guiCustomUiConfiguration.customUiConfiguration.enabled == true}">
                                 <li><a href="#" onclick="return guiV2link('operation=UiV2CustomUi.customUiGroup&groupId=${guiCustomUiConfiguration.customUiConfiguration.groupId}'); return false;">${textContainer.text['customUiConfigsTableRunActionOption'] }</a></li>
                               </c:if>
                           
                               <li>&nbsp;</li>                                  
                               <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['customUiConfigsConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2CustomUiConfig.deleteCustomUiConfig&customUiConfigId=${guiCustomUiConfiguration.customUiConfiguration.configId}');}">${textContainer.text['customUiConfigsTableDeleteDetailsActionOption'] }</a></li>
                                 </ul>
                               </div>
                         </td>
                    </c:forEach>
                   
                   </tbody>
               </table>
              
            </c:when>
            <c:otherwise>
              <div class="row-fluid">
                <div class="span9"> <p><b>${textContainer.text['customUiNoConfiguredCustomUis'] }</b></p></div>
              </div>
            </c:otherwise>
          </c:choose>
          
        </div>
