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
              
              <!-- a <div class="row-fluid">
                     
                    </div> -->
              
            </div>

            <div class="row-fluid" id="configurationMainDivId">
            
              <div id="configuration-select-container">
               <form id="configurationSelectForm" class="form-horizontal" method="post" action="UiV2Configure.configure" >
                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationFilesImportFileLabel'] }</label>
                   <div class="controls">
                     
                     <input type="file" />
                     
                     <span class="help-block">${textContainer.text['configurationFilesImportFileDescription'] }</span>
                   
                   </div>
                 </div>
                 
                 <div class="control-group">
                   <label class="control-label">${textContainer.text['configurationFilesImportTypeLabel'] }</label>
                   <div class="controls">
                     <select id="importTypeId" class="span10" name="importTypeName" >
                        <option value=""></option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_CACHE_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_CACHE_PROPERTIES">grouper.cache.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_CLIENT_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_CLIENT_PROPERTIES">grouper.client.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_LOADER_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_LOADER_PROPERTIES">grouper-loader.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_PROPERTIES">grouper.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_UI_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_UI_PROPERTIES">grouper-ui.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'GROUPER_WS_PROPERTIES' ? 'selected="selected"' : '' } value="GROUPER_WS_PROPERTIES">grouper-ws.properties</option>
                        <option ${grouperRequestContainer.configurationContainer.configFileName == 'SUBJECT_PROPERTIES' ? 'selected="selected"' : '' } value="SUBJECT_PROPERTIES">subject.properties</option>
                     </select>
                     
                     <span class="help-block">${textContainer.text['configurationFilesAddEntryPropertyNameDescription'] }</span>
                   
                   </div>
                 </div>

                 <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Configure.configurationFileAddConfigSubmit', {formIds: 'configurationSelectForm'}); return false;">${textContainer.text['configurationFilesAddEntrySubmit'] }</a> 
                 <a href="#" onclick="return guiV2link('operation=UiV2Configure.configure&configFile=${grouperRequestContainer.configurationContainer.configFileName}');" class="btn btn-cancel">${textContainer.text['configurationFilesAddEntryCancel'] }</a></div>

                </form>
             
              </div>
            </div>
