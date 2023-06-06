<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

            <div class="bread-header-container">
              <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousGrouperProvisioningDiagnosticsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['miscellaneousProvisionerConfigurationsDiagnosticsMainDescription'] }</h1>
                <p>
                  ${textContainer.text['miscellaneousProvisionerConfigurationsDiagnosticsMainDescriptionParagraph']}
                </p>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-horizontal" id="provisioningDiagnosticsForm"
                    onsubmit="return false;">

                  <c:if test="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroupsAll()}">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['grouperProvisioningDiagnosticsLargeOperationsLabel'] }</label>
                      <div class="controls">
                        <label class="checkbox">
                          <input type="checkbox" name="diagnosticsGroupsAllSelectName" id="diagnosticsGroupsAllSelectId" 
                               ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().diagnosticsGroupsAllSelect ? 'checked="checked"' : '' } value="true" />
                               ${textContainer.text['grouperProvisioningDiagnosticsSelectAllGroupsLabel']}
                        </label>
                        <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsSelectAllGroupsDescription'] }</span>                    
                      </div>
                    </div>
                  </c:if>
                  <c:if test="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntitiesAll()}">
                    <div class="control-group">
                   
                      <div class="controls">
                        <label class="checkbox">
                          <input type="checkbox" name="diagnosticsEntitiesAllSelectName" id="diagnosticsEntitiesAllSelectId" 
                               ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().diagnosticsEntitiesAllSelect ? 'checked="checked"' : '' } value="true" />
                               ${textContainer.text['grouperProvisioningDiagnosticsSelectAllEntitiesLabel']}
                        </label>
                        <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsSelectAllEntitiesDescription'] }</span>                    
                      </div>
                    </div>
                  </c:if>
                  <c:if test="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsAll() && 
                    grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsWithEntity() == false &&
                    grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsWithGroup() == false}">
                    <div class="control-group">
                   
                      <div class="controls">
                        <label class="checkbox">
                          <input type="checkbox" name="diagnosticsMembershipsAllSelectName" id="diagnosticsMembershipsAllSelectId" 
                               ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().diagnosticsMembershipsAllSelect ? 'checked="checked"' : '' } value="true" />
                               ${textContainer.text['grouperProvisioningDiagnosticsSelectAllMembershipsLabel']}
                        </label>
                        <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsSelectAllMembershipsDescription'] }</span>                    
                      </div>
                    </div>
                  </c:if>
                  <div class="control-group">
                    <label for="diagnosticsGroupNameId" class="control-label">${textContainer.text['grouperProvisioningDiagnosticsGroupNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="diagnosticsGroupNameId" name="diagnosticsGroupNameName" 
                        value="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().diagnosticsGroupName}" /> 
                      <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsGroupNameDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="diagnosticsSubjectIdOrIdentifierId" class="control-label">${textContainer.text['grouperProvisioningDiagnosticsSubjectIdOrIdentifierLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="diagnosticsSubjectIdOrIdentifierId" name="diagnosticsSubjectIdOrIdentifierName" 
                        value="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().diagnosticsSubjectIdOrIdentifier}" /> 
                      <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsSubjectIdOrIdentifierDescription'] }</span>
                    </div>
                  </div>
                  <c:if test="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertGroups()}">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['grouperProvisioningDiagnosticsGroupInsertLabel'] }</label>
                      <div class="controls">
                        <label class="checkbox">
                          <input type="checkbox" name="diagnosticsGroupsInsertName" id="diagnosticsGroupsInsertId" 
                               ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().createGroupDuringDiagnostics ? 'checked="checked"' : '' } value="true" />
                               ${textContainer.text['grouperProvisioningDiagnosticsGroupInsertLabelTrue']}
                        </label>
                        <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsGroupInsertDescription'] }</span>                    
                      </div>
                    </div>
                  </c:if>
                  <c:if test="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteGroups()}">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['grouperProvisioningDiagnosticsGroupDeleteLabel'] }</label>
                      <div class="controls">
                        <label class="checkbox">
                          <input type="checkbox" name="diagnosticsGroupsDeleteName" id="diagnosticsGroupsDeleteId" 
                               ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().deleteGroupDuringDiagnostics ? 'checked="checked"' : '' } value="true" />
                               ${textContainer.text['grouperProvisioningDiagnosticsGroupDeleteLabelTrue']}
                        </label>
                        <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsGroupDeleteDescription'] }</span>                    
                      </div>
                    </div>
                  </c:if>
                  <c:if test="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertEntities()}">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['grouperProvisioningDiagnosticsEntityInsertLabel'] }</label>
                      <div class="controls">
                        <label class="checkbox">
                          <input type="checkbox" name="diagnosticsEntitiesInsertName" id="diagnosticsEntitiesInsertId" 
                               ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().createEntityDuringDiagnostics ? 'checked="checked"' : '' } value="true" />
                               ${textContainer.text['grouperProvisioningDiagnosticsEntityInsertLabelTrue']}
                        </label>
                        <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsEntityInsertDescription'] }</span>                    
                      </div>
                    </div>
                  </c:if>
                  <c:if test="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntities()}">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['grouperProvisioningDiagnosticsEntityDeleteLabel'] }</label>
                      <div class="controls">
                        <label class="checkbox">
                          <input type="checkbox" name="diagnosticsEntitiesDeleteName" id="diagnosticsEntitiesDeleteId" 
                               ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().deleteEntityDuringDiagnostics ? 'checked="checked"' : '' } value="true" />
                               ${textContainer.text['grouperProvisioningDiagnosticsEntityDeleteLabelTrue']}
                        </label>
                        <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsEntityDeleteDescription'] }</span>                    
                      </div>
                    </div>
                  </c:if>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['grouperProvisioningDiagnosticsMembershipInsertLabel'] }</label>
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="diagnosticsMembershipInsertName" id="diagnosticsMembershipInsertId" 
                             ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().createMembershipDuringDiagnostics ? 'checked="checked"' : '' } value="true" />
                             ${textContainer.text['grouperProvisioningDiagnosticsMembershipInsertLabelTrue']}
                      </label>
                      <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsMembershipInsertDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['grouperProvisioningDiagnosticsMembershipDeleteLabel'] }</label>
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="diagnosticsMembershipDeleteName" id="diagnosticsMembershipDeleteId" 
                        ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.retrieveGrouperProvisioningConfiguration().deleteMembershipDuringDiagnostics ? 'checked="checked"' : '' } value="true" />
                             ${textContainer.text['grouperProvisioningDiagnosticsMembershipDeleteLabelTrue']}
                      </label>
                      <span class="help-block">${textContainer.text['grouperProvisioningDiagnosticsMembershipDeleteDescription'] }</span>
                    </div>
                  </div>
        
                <input type="hidden" name="provisionerConfigId" value="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.grouperProvisioner.configId}" />
                <input type="hidden" name="provisionerInitted" value="true" />
                <div class="form-actions"><a href="#" class="btn btn-primary" role="button" 
                  onclick="ajax('../app/UiV2ProvisionerConfiguration.diagnostics', {formIds: 'provisioningDiagnosticsForm'}); return false;"
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

