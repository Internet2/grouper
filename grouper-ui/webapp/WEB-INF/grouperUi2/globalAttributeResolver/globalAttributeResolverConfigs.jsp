<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('globalAttributeResolverConfigsPageTitle')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousGlobalAttributeResolverConfigsOverallBreadcrumb'] }</li>
              </ul>
               
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousGlobalAttributeResolverConfigsMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="globalAttributeResolverConfigMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
      <div class="row-fluid">
        
          <c:choose>
            <c:when test="${fn:length(grouperRequestContainer.globalAttributeResolverConfigContainer.guiGlobalAttributeResolverConfigs) > 0}">
              
              <table class="table table-hover table-bordered table-striped table-condensed data-table">
                <thead>        
                  <tr>
                    <th>${textContainer.text['globalAttributeResolverConfigsTableHeaderConfigId']}</th> 
                    <th>${textContainer.text['globalAttributeResolverConfigsTableHeaderEnabled']}</th>
                    <th>${textContainer.text['globalAttributeResolverConfigsTableHeaderActions']}</th>
                  </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach items="${grouperRequestContainer.globalAttributeResolverConfigContainer.guiGlobalAttributeResolverConfigs}" var="guiGlobalAttributeResolverConfiguration">
                    
                      <tr>
                         <td style="white-space: nowrap;">
                          ${grouper:escapeHtml(guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.configId)}
                         </td>
                         
                         <td style="white-space: nowrap;">
                           <c:if test="${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.enabled == true}">
                            ${textContainer.text['globalAttributeResolverConfigsTableEnabledTrueValue']}
                           </c:if>
                           <c:if test="${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.enabled == false }">
                            ${textContainer.text['globalAttributeResolverConfigsTableEnabledFalseValue']}
                           </c:if>
                         </td>
                        
                         <td>
                           <div class="btn-group">
                                 <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                                   aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                                   ${textContainer.text['globalAttributeResolverConfigsRowActionsButton'] }
                                   <span class="caret"></span>
                                 </a>
                                 <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                                   
                                   <c:if test="${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.enabled == true}">
                                      <li><a href="#" onclick="return guiV2link('operation=UiV2GlobalAttributeResolverConfig.disableGlobalAttributeResolverConfig&globalAttributeResolverConfigId=${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.configId}');">${textContainer.text['globalAttributeResolverConfigsTableDisableActionOption'] }</a></li>
                                   </c:if>
                               
                               <c:if test="${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.enabled == false}">
                                <li><a href="#" onclick="return guiV2link('operation=UiV2GlobalAttributeResolverConfig.enableGlobalAttributeResolverConfig&globalAttributeResolverConfigId=${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.configId}');">${textContainer.text['globalAttributeResolverConfigsTableEnableActionOption'] }</a></li>
                               </c:if>
                               
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GlobalAttributeResolverConfig.editGlobalAttributeResolverConfig&globalAttributeResolverConfigId=${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.configId}');">${textContainer.text['gshTemplatesTableEditDetailsActionOption'] }</a></li>
                               
                                  <li>&nbsp;</li>                                  
                                   <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['globalAttributeResolverConfigsConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2GlobalAttributeResolverConfig.deleteGlobalAttributeResolverConfig&globalAttributeResolverConfigId=${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.configId}');}">${textContainer.text['globalAttributeResolverConfigsTableDeleteDetailsActionOption'] }</a></li>
                                 </ul>
                               </div>
                         </td>
                    </c:forEach>
                   
                   </tbody>
               </table>
              
            </c:when>
            <c:otherwise>
              <div class="row-fluid">
                <div class="span9"> <p><b>${textContainer.text['globalAttributeResolverConfigsNoConfiguredResolvers'] }</b></p></div>
              </div>
            </c:otherwise>
          </c:choose>
          
        </div>
