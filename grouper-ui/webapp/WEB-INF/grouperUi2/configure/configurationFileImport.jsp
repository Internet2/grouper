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
                   <label class="control-label">${textContainer.text['configurationSelectConfigFile'] }</label>
                   <div class="controls">
                     
                     <input type="file" name="importConfigFile" id="importConfigFileId" />
                     <span class="help-block">${textContainer.text['configurationFilesImportSelectConfigFileDescription'] }</span>
                   
                   </div>
                 </div>

                 <div class="form-actions"><a href="#" class="btn btn-primary" onclick="return guiSubmitFileForm(event, '#configurationImportForm', '../app/UiV2Configure.configurationFileImportSubmit');">${textContainer.text['configurationFilesImportSubmit'] }</a> 
                 <a href="#" onclick="return guiV2link('operation=UiV2Configure.configure&configFile=${grouperRequestContainer.configurationContainer.configFileName}');" class="btn btn-cancel">${textContainer.text['configurationFilesImportCancel'] }</a></div>

                </form>
             
              </div>
            </div>
