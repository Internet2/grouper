<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousConfigureBreadcrumb'] }</li>
              </ul>
                            
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="lead span9"><h1>${textContainer.text['miscellaneousConfigurationMainDescription'] }</h1></div>
                  <%-- c:if test="${grouperRequestContainer.deprovisioningContainer.allowedToDeprovision}">
                      <div class="span3" id="deprovisioningMainMoreActionsButtonContentsDivId">
                        <%@ include file="deprovisioningMainMoreActionsButtonContents.jsp" %>
                      </div>
                  </c:if --%>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousConfigurationMainSubtitle']}</p>
                  </div>
                </div>

                <div id="configuration-select-container">
                 <form id="configurationSelectForm" class="form-horizontal" method="get" action="UiV2Configure.configure" >
                   <div class="control-group">
                     <label class="control-label">${textContainer.text['configurationSelectConfigFile'] }</label>
                     <div class="controls">
                       <%-- --%>
                       <select id="configFileSelect" class="span2" name="configFile" 
                              onchange="return guiV2link('operation=UiV2Configuration.configure', {optionalFormElementNamesToSend: 'configFile'});"
                              >
                          <option value=""></option>
                          <c:forEach items="${grouperRequestContainer.deprovisioningContainer.guiDeprovisioningAffiliationsAll}" var="guiDeprovisioningAffiliation" >
                          
                          allConfigFileNames
                          <option value="GROUPER_CACHE_PROPERTIES">grouper.cache.properties</option>
                          <option value="GROUPER_CLIENT_PROPERTIES">grouper.client.properties</option>
                          <option value="GROUPER_LOADER_PROPERTIES">grouper-loader.properties</option>
                          <option value="GROUPER_PROPERTIES">grouper.properties</option>
                          <option value="GROUPER_UI_PROPERTIES">grouper-ui.properties</option>
                          <option value="GROUPER_WS_PROPERTIES">grouper-ws.properties</option>
                          <option value="SUBJECT_PROPERTIES">subject.properties</option>
                       </select>
                       
                       <span class="help-block">${textContainer.text['configurationSelectConfigFileDescription'] }</span>
                     
                     </div>
                   </div>
                   
                  </form>
               
                </div>

                <c:if test="${grouperRequestContainer.configurationContainer.configFileName != null}">
Hello               
                </c:if>
              </div>
              
              <!-- a <div class="row-fluid">
                     
                    </div> -->
              
            </div>

            <div class="row-fluid" id="configurationMainDivId">
            </div>
