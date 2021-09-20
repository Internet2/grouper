<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousWsTrustedJwtsOverallBreadcrumb'] }</li>
              </ul>
               
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousWsTrustedJwtsMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="wsTrustedJwtsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
      <div class="row-fluid">
        
          <c:choose>
            <c:when test="${fn:length(grouperRequestContainer.authenticationContainer.guiWsTrustedJwtConfigs) > 0}">
              
              <table class="table table-hover table-bordered table-striped table-condensed data-table">
                <thead>        
                  <tr>
                    <th>${textContainer.text['wsTrustedJwtsTableHeaderConfigId']}</th> 
                    <th>${textContainer.text['wsTrustedJwtsTableHeaderEnabled']}</th>
                    <th>${textContainer.text['wsTrustedJwtsTableHeaderActions']}</th>
                  </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach items="${grouperRequestContainer.authenticationContainer.guiWsTrustedJwtConfigs}" var="guiWsTrustedJwtConfiguration">
                    
                      <tr>
                         <td style="white-space: nowrap;">
                          ${grouper:escapeHtml(guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.configId)}
                         </td>
                         
                         <td style="white-space: nowrap;">
                           <c:if test="${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.enabled == true}">
                            ${textContainer.text['wsTrustedJwtsTableEnabledTrueValue']}
                           </c:if>
                           <c:if test="${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.enabled == false }">
                            ${textContainer.text['wsTrustedJwtsTableEnabledFalseValue']}
                           </c:if>
                         </td>
                        
                         <td>
                           <div class="btn-group">
                                 <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                                   aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                                   ${textContainer.text['wsTrustedJwtsRowActionsButton'] }
                                   <span class="caret"></span>
                                 </a>
                                 <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                                   
                                   <c:if test="${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.enabled == true}">
                                      <li><a href="#" onclick="return guiV2link('operation=UiV2AuthenticationConfig.disableWsTrustedJwtConfig&wsTrustedJwtConfigId=${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.configId}');">${textContainer.text['wsTrustedJwtTableDisableActionOption'] }</a></li>
                                   </c:if>
                               
                               <c:if test="${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.enabled == false}">
                                <li><a href="#" onclick="return guiV2link('operation=UiV2AuthenticationConfig.enableWsTrustedJwtConfig&wsTrustedJwtConfigId=${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.configId}');">${textContainer.text['wsTrustedJwtTableEnableActionOption'] }</a></li>
                               </c:if>
                               
                               <li><a href="#" onclick="return guiV2link('operation=UiV2AuthenticationConfig.editWsTrustedJwtConfig&wsTrustedJwtConfigId=${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.configId}');">${textContainer.text['gshTemplatesTableEditDetailsActionOption'] }</a></li>
                               
                                  <li>&nbsp;</li>                                  
                                   <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['wsTrustedJwtConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2AuthenticationConfig.deleteWsTrustedJwtConfig&wsTrustedJwtConfigId=${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.configId}');}">${textContainer.text['wsTrustedJwtTableDeleteDetailsActionOption'] }</a></li>
                                 </ul>
                               </div>
                         </td>
                    </c:forEach>
                   
                   </tbody>
               </table>
              
            </c:when>
            <c:otherwise>
              <div class="row-fluid">
                <div class="span9"> <p><b>${textContainer.text['wsTrustedJwtsNoConfiguredJwts'] }</b></p></div>
              </div>
            </c:otherwise>
          </c:choose>
          
        </div>
