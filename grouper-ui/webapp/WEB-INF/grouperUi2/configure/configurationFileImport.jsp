<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Configure.index');">${textContainer.text['miscellaneousConfigureBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Configure.configure');">${textContainer.text['miscellaneousConfigurationFilesBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousConfigurationFilesImportBreadcrumb'] }</li>
              </ul>
                            
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['configurationFilesImportTitle'] }</h1></div>
                  <div class="span2 pull-right">
                    <%@ include file="configureFilesMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['configurationFilesImportSubtitle']}</p>
                  </div>
                </div>
              </div>
              
            </div>

            <div class="row-fluid" id="configurationMainDivId">
            
              <div id="configuration-select-container">
               <form id="configurationImportForm" class="form-horizontal" method="post" action="UiV2Configure.configurationFileImportSubmit" 
                   enctype="multipart/form-data">
                   
                <div class="control-group">
                <label class="control-label">${textContainer.text['configurationImportHowAdd'] }</label>
                <div class="controls">
                  <label class="radio">
                    <input type="radio" name="configurationImportHowAdd" value="file" checked="checked"
                    onchange="$('.config-import-file').slideDown('fast'); $('.config-import-copyPaste').slideUp('fast'); return true;"
                    >${textContainer.text['configurationImportFile'] } &nbsp; <span  id="configurationImportHowAddImportFileErrorId"></span>
                  </label>
                  <label class="radio">
                    <input type="radio" name="configurationImportHowAdd" value="copyPaste"
                      onchange="$('.config-import-file').slideUp('fast'); $('.config-import-copyPaste').slideDown('fast'); return true;"
                    >${textContainer.text['configurationImportCopyPaste'] }
                  </label>
                </div>
              </div>
                   
                   
                   
              <div class="config-import-file">
                   
                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationSelectConfigFile'] }</label>
                   <div class="controls">
                     
                     <input type="file" name="importConfigFile" id="importConfigFileId" />
                     <span class="help-block">${textContainer.text['configurationFilesImportSelectConfigFileDescription'] }</span>
                   
                   </div>
                 </div>
                </div>
                
              <div class="config-import-copyPaste hide">

                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationImportSelectConfigFile'] }</label>
                   <div class="controls">
                     <select id="configFileSelect" class="span4" name="configFile" >
                        <option value=""></option>
                        <option value="GROUPER_CACHE_PROPERTIES">grouper.cache.properties</option>
                        <option value="GROUPER_CLIENT_PROPERTIES">grouper.client.properties</option>
                        <option value="GROUPER_LOADER_PROPERTIES">grouper-loader.properties</option>
                        <option value="GROUPER_PROPERTIES">grouper.properties</option>
                        <option value="GROUPER_TEXT_EN_US_PROPERTIES">grouper.text.en.us.properties</option>
                        <c:if test="${ grouperRequestContainer.configurationContainer.hasFrench }">
                          <option value="GROUPER_TEXT_FR_FR_PROPERTIES">grouper.text.fr.fr.properties</option>
                        </c:if>
                        <option value="GROUPER_UI_PROPERTIES">grouper-ui.properties</option>
                        <option value="GROUPER_WS_PROPERTIES">grouper-ws.properties</option>
                        <option value="SUBJECT_PROPERTIES">subject.properties</option>
                     </select>
                     
                     <span class="help-block">${textContainer.text['configurationImportSelectConfigFileDescription'] }</span>
                   
                   </div>
                 </div>
                   
                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationImportConfigFileCopyPaste'] }</label>
                   <div class="controls">
                     <textarea id="configInputCopyPasteId" name="configInputCopyPasteName" rows="10" cols="40" 
                     class="input-block-level"></textarea><span class="help-block">${textContainer.text['configurationImportConfigFileCopyPasteDescription'] }</span>
                     
                   </div>
                 </div>
                </div>
                
                 <div class="form-actions"><a href="#" class="btn btn-primary" onclick="return guiSubmitFileForm(event, '#configurationImportForm', '../app/UiV2Configure.configurationFileImportSubmit');">${textContainer.text['configurationFilesImportSubmit'] }</a> 
                 <a href="#" onclick="return guiV2link('operation=UiV2Configure.configure&configFile=${grouperRequestContainer.configurationContainer.configFileName}');" class="btn btn-cancel">${textContainer.text['configurationFilesImportCancel'] }</a></div>

                </form>
             
              </div>
            </div>
