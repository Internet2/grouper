<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousProvisionerDiagnosticsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['miscellaneousProvisionerConfigurationsDiagnosticsMainDescription'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-horizontal" id="provisioningDiagnosticsForm"
                    onsubmit="return false;">
        
        <%--
                  <div class="control-group">
                    <label for="provisioningSourceIdId" class="control-label">${textContainer.text['adminprovisioningDiagnosticsSourceId'] }</label>
                    <div class="controls">
            
            <input type="hidden" name="provisioningSourceIdName" id="provisioningSourceIdId" value="${grouper:escapeHtml(grouperRequestContainer.subjectSourceContainer.subjectSourceId)}" />                     
                  ${grouper:escapeHtml(grouperRequestContainer.subjectSourceContainer.subjectSourceId)}
                    
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsSourceIdLabel'] }</span>
                    </div>
                  </div>

                  <div class="control-group">
                    <label for="subjectIdId" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsSubjectId'] }</label>
                    <div class="controls">
                      <input type="text" id="subjectIdId" name="subjectIdName" value="" /> 
                      <span class="help-block">${textContainer.text['adminSubjprovisioningnosticsSubjectIdLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="subjectIdentifierId" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsSubjectIdentifier'] }</label>
                    <div class="controls">
                      <input type="text" id="subjectIdentifierId" name="subjectIdentifierName" value="" /> 
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsSubjectIdentifierLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="searchStringId" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsSearchString'] }</label>
                    <div class="controls">
                      <input type="text" id="searchStringId" name="searchStringName" value="" /> 
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsSearchStringLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="actAsComboID" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsActAs'] }</label>
                    <div class="controls">
                      <grouper:combobox2 idBase="actAsCombo" style="width: 30em"
                                      filterOperation="../app/UiV2Admin.provisioningDiagnosticsActAsCombo"/>
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsActAsLabel'] }</span>
                    </div>
                  </div>
            --%>      
                  
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.diagnostics&provisionerConfigId=${grouperRequestContainer.provisionerDiagnosticsContainer.grouperProvisioner.configId}');"
                  >${textContainer.text['provisioningDiagnosticsSubmitButton'] }</a> 
                  &nbsp;
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                          >${textContainer.text['provisioningDiagnosticsCancelButton'] }</a>
                  </div>
                  
                </form>
                <div id="provisioningDiagnosticsResultsId">
                </div>
              </div>
           </div>

