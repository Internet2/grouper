<%@ include file="../assetsJsp/commonTaglib.jsp"%>
			<c:set value="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration}" var="guiGrouperDaemonConfiguration"/>
            <div class="bread-header-container">
               <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.daemonJobs');">${textContainer.text['adminDaemonJobsLink'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['daemonJobsEditDaemon'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
                <div class="row-fluid">
              
                <div class="span2 pull-right" id="daemonMoreActionsButtonContentsDivId">
                  <%@ include file="adminDaemonJobsMoreActionsButtonContents.jsp"%>
                </div>
                <div class="span10 pull-left">
                  <h1>${textContainer.text['daemonJobsEditDaemon'] }</h1>
                  ${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration.jobDescription}
                </div>
              </div>
              
            </div>
            </div>
              
			<div class="row-fluid">
			  <div class="span12">
			  	<form class="form-inline form-small form-filter" id="editDaemonFormId">
			  		<input type="hidden" name="previousJobName" value="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration.jobName}" />
			  		<input type="hidden" name="jobName" value="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration.jobName}" />
					
					<table class="table table-condensed table-striped">
              		  <tbody>
              		  	<tr>
						  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['daemonNameLabel']}</label></strong></td>
						    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
						    <td>
						     ${grouper:escapeHtml(grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration.jobName)}
						    </td>
						</tr>
						
						<c:if test="${guiGrouperDaemonConfiguration.grouperDaemonConfiguration.multiple == true}">
							<tr>
							  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['daemonConfigIdLabel']}</label></strong></td>
							  <td>&nbsp;</td>
							  <td>
							    ${grouper:escapeHtml(guiGrouperDaemonConfiguration.grouperDaemonConfiguration.configId)}
							  </td>
							</tr>
						</c:if>
						
						<tr>
	                      <td style="vertical-align: top; white-space: nowrap;"><strong><label for="daemonTypeId">${textContainer.text['grouperDaemonConfigEnableLabel']}</label></strong></td>
	                      <td>&nbsp;</td>
	                      <td>
							<select name="daemonConfigEnable" id="daemonConfigEnableId" style="width: 30em">
						      <option value="true" ${guiGrouperDaemonConfiguration.enabled == true ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperDaemonConfigEnableYes']}</option>
						      <option value="false" ${guiGrouperDaemonConfiguration.enabled == false ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperDaemonConfigEnableNo']}</option>
						    </select>
						    <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
						      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
						    <br />
						    <span class="description">${textContainer.text['grouperDaemonConfigEnableDescription']}</span>
	                      </td>
	                    </tr>
              		  	
                      <c:forEach items="${guiGrouperDaemonConfiguration.grouperDaemonConfiguration.subSections}" var="subSection">
                          <tbody>
                            <c:if test="${!grouper:isBlank(subSection.label) and subSection.show}">
                              <tr>
                                <th colspan="3">
                                  <%-- the header needs to be on a field to subsitute the name in the label if there --%>
                                  <c:set target="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration}"
                                          property="currentConfigSuffix"
                                          value="${subSection.label}.header" />  
                                  <h4>${subSection.title}</h4>
                                  <p style="font-weight: normal;">${subSection.description} </p>
                                </th>
                              </tr>
                            
                            </c:if>
                                    
                            <c:forEach items="${subSection.attributesValues}" var="attribute">  
                            
                              <c:set target="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration}"
                                      property="index"
                                      value="${attribute.repeatGroupIndex}" />  
                              <c:set target="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration}"
                                      property="currentConfigSuffix"
                                      value="${attribute.configSuffix}" />  

                              <%--  ajaxCallback="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration?focusOnElementName=config_${attribute.configSuffix}&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}', {formIds: 'provisionerConfigDetails'}); return false;" --%>
                              <grouper:configFormElement 
                                formElementType="${attribute.formElement}" 
                                configId="${attribute.configSuffix}" 
                                label="${attribute.label}"
                                readOnly="${attribute.readOnly}"
                                helperText="${attribute.description}"
                                helperTextDefaultValue="${attribute.defaultValue}"
                                required="${attribute.required}"
                                shouldShow="${attribute.show}"
                                value="${attribute.valueOrExpressionEvaluation}"
                                hasExpressionLanguage="${attribute.expressionLanguage}"
                                ajaxCallback="ajax('../app/UiV2Admin.editDaemon?daemonConfigId=${guiGrouperDaemonConfiguration.grouperDaemonConfiguration.configId}&daemonConfigType=${guiGrouperDaemonConfiguration.grouperDaemonConfiguration['class'].name}', {formIds: 'editDaemonFormId'}); return false;"
                                valuesAndLabels="${attribute.dropdownValuesAndLabels }"
                                checkboxAttributes="${attribute.checkboxAttributes}"
                                indent="${attribute.configItemMetadata.indent}"
                              />
                              
                            </c:forEach>
                            
                          </tbody>
                    
                      </c:forEach>
					  			
              		  </tbody>
              		</table>
					
					<div class="span6">
	                  <input type="submit" class="btn btn-primary"
	                        aria-controls="editDaemonFormId" id="addDaemonSubmitButtonId"
	                        value="${textContainer.text['grouperDaemonAddEditSubmitButton'] }"
	                        onclick="ajax('../app/UiV2Admin.editDaemonSubmit', {formIds: 'editDaemonFormId'}); return false;">
                          &nbsp; 
                     <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2Admin.viewLogs&jobName=${grouper:escapeHtml(grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration.jobName)}'); return false;"
                          >${textContainer.text['grouperDaemonConfigCancelButton'] }</a>
                   
                   </div>
				</form>
			  </div>
			</div>