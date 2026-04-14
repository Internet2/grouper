<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('customUiConfigsPageTitle')}
            <grouper:browserPage jspName="customUiConfigs" />
            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousCustomUiOverallBreadcrumb'] }</li>
              </ul>
               
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span11 pull-left"><h4>${textContainer.text['miscellaneousCustomUiMainDescription'] }</h4></div>
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
                  </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach items="${grouperRequestContainer.customUiContainer.guiCustomUiConfigurations}" var="guiCustomUiConfiguration">
                    
                      <tr>
                         <td style="white-space: nowrap;">
                         
                          <c:if test="${guiCustomUiConfiguration.canRun}">
                            <a id="run_${grouper:escapeHtml(guiCustomUiConfiguration.customUiConfiguration.configId)}_id" href="?operation=UiV2CustomUi.customUiGroup&groupId=${guiCustomUiConfiguration.customUiConfiguration.groupId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2CustomUi.customUiGroup&groupId=${guiCustomUiConfiguration.customUiConfiguration.groupId}'); return false;">${grouper:escapeHtml(guiCustomUiConfiguration.customUiConfiguration.configId)}</a>
                          </c:if>
                          
                          <c:if test="${guiCustomUiConfiguration.canRun == false}">
                              ${grouper:escapeHtml(guiCustomUiConfiguration.customUiConfiguration.configId)}
                          </c:if>
                          
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
