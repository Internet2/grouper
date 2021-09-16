<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousSqlSyncOverallBreadcrumb'] }</li>
              </ul>
               
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousSqlSyncMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="sqlSyncConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
              
      <div class="row-fluid">
        
          <c:choose>
            <c:when test="${fn:length(grouperRequestContainer.sqlSyncConfigurationContainer.guiSqlSyncConfigurations) > 0}">
              
              <table class="table table-hover table-bordered table-striped table-condensed data-table">
                <thead>        
                  <tr>
                    <th>${textContainer.text['sqlSyncConfigsTableHeaderConfigId']}</th>
                    <th>${textContainer.text['sqlSyncConfigsTableHeaderEnabled']}</th>
                    <th>${textContainer.text['sqlSyncConfigsTableHeaderActions']}</th>
                  </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach items="${grouperRequestContainer.sqlSyncConfigurationContainer.guiSqlSyncConfigurations}" var="guiSqlSyncConfiguration">
                    
                      <tr>
                         <td style="white-space: nowrap;">
                          ${grouper:escapeHtml(guiSqlSyncConfiguration.sqlSyncConfiguration.configId)}
                         </td>
                         
                         <td style="white-space: nowrap;">
                           <c:if test="${guiSqlSyncConfiguration.sqlSyncConfiguration.enabled == true}">
                            ${textContainer.text['sqlSyncConfigsTableEnabledTrueValue']}
                           </c:if>
                           <c:if test="${guiSqlSyncConfiguration.sqlSyncConfiguration.enabled == false }">
                            ${textContainer.text['sqlSyncConfigsTableEnabledFalseValue']}
                           </c:if>
                         </td>
                        
                         <td>
                           <div class="btn-group">
                            <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                              aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                              ${textContainer.text['sqlSyncConfigsRowActionsButton'] }
                              <span class="caret"></span>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                              
                          <c:if test="${guiSqlSyncConfiguration.sqlSyncConfiguration.enabled == true}">
                           <li><a href="#" onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.disableSqlSyncConfig&sqlSyncConfigId=${guiSqlSyncConfiguration.sqlSyncConfiguration.configId}');">${textContainer.text['sqlSyncConfigsTableDisableActionOption'] }</a></li>
                          </c:if>
                          
                          <c:if test="${guiSqlSyncConfiguration.sqlSyncConfiguration.enabled == false}">
                           <li><a href="#" onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.enableSqlSyncConfig&sqlSyncConfigId=${guiSqlSyncConfiguration.sqlSyncConfiguration.configId}');">${textContainer.text['sqlSyncConfigsTableEnableActionOption'] }</a></li>
                          </c:if>
                          
                          <li><a href="#" onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.editSqlSyncConfig&sqlSyncConfigId=${guiSqlSyncConfiguration.sqlSyncConfiguration.configId}');">${textContainer.text['sqlSyncConfigsTableEditDetailsActionOption'] }</a></li>
                          
                          <%-- <c:if test="${guiSqlSyncConfiguration.sqlSyncConfiguration.enabled == true}">
                            <li><a href="#" onclick="return guiV2link('operation=UiV2CustomUi.customUiGroup&groupId=${guiSqlSyncConfiguration.sqlSyncConfiguration.groupId}'); return false;">${textContainer.text['sqlSyncConfigsTableRunActionOption'] }</a></li>
                          </c:if> --%>
                      
                          <li>&nbsp;</li>                                  
                          <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['sqlSyncConfigsConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2SqlSyncConfiguration.deleteSqlSyncConfig&sqlSyncConfigId=${guiSqlSyncConfiguration.sqlSyncConfiguration.configId}');}">${textContainer.text['sqlSyncConfigsTableDeleteDetailsActionOption'] }</a></li>
                            </ul>
                        </div>
                       </td>
                    </c:forEach>
                   
                   </tbody>
               </table>
              
            </c:when>
            <c:otherwise>
              <div class="row-fluid">
                <div class="span9"> <p><b>${textContainer.text['sqlSyncNoConfiguredSqlSyncs'] }</b></p></div>
              </div>
            </c:otherwise>
          </c:choose>
          
        </div>
