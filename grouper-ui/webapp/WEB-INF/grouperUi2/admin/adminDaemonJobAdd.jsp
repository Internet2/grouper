<%@ include file="../assetsJsp/commonTaglib.jsp"%>

		<c:set value="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration}" var="guiGrouperDaemonConfiguration"/>
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.daemonJobs');">${textContainer.text['adminDaemonJobsLink'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['daemonJobsAddDaemon'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <div class="row-fluid">
              
                <div class="span2 pull-right" id="daemonMoreActionsButtonContentsDivId">
                  <%@ include file="adminDaemonJobsMoreActionsButtonContents.jsp"%>
                </div>
                <div class="span10 pull-left">
                  <h1>${textContainer.text['daemonJobsAddDaemon'] }</h1>
                  ${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration.jobDescription}
                </div>
              </div>
              
            </div>
            <div class="row-fluid">
              <div class="lead span9">${textContainer.text['daemonJobsAddDaemon']}</div>

              <form class="form-inline form-small form-filter" id="addDaemonFormId">

                <table class="table table-condensed table-striped">
                  <tbody>

				    <tr>
					  <td style="vertical-align: top; white-space: nowrap;"><strong><label for="configId">${textContainer.text['daemonConfigIdLabel']}</label></strong></td>
					  <td>&nbsp;</td>
					  <td>
					    <input type="hidden" value="${grouper:escapeHtml(guiGrouperDaemonConfiguration.grouperDaemonConfiguration.configId)}"
					       name="previousDaemonConfigId" id="previousDaemonConfigId" />
					    <input type="hidden" value="${grouper:escapeHtml(guiGrouperDaemonConfiguration.grouperDaemonConfiguration['class'].name)}"
					       name="previousDaemonConfigType" id="previousDaemonConfigType" />
					    <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiGrouperDaemonConfiguration.grouperDaemonConfiguration.configId)}"
					       name="daemonConfigId" id="configId" />
					    <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
					       data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
					    <br />
					    <span class="description">${textContainer.text['daemonConfigIdHint']}</span>
					  </td>
					</tr>
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
                    <tr>
                      <td style="vertical-align: top; white-space: nowrap;"><strong><label for="daemonTypeId">${textContainer.text['daemonJobTypeLabel']}</label></strong></td>
                      <td>&nbsp;</td>
                      <td>
						<select name="daemonConfigType" id="daemonTypeId" style="width: 30em"
						      onchange="ajax('../app/UiV2Admin.addDaemon', {formIds: 'addDaemonFormId'}); return false;">
						       
					        <option value=""></option>
					        <option value="loader" ${grouperRequestContainer.adminContainer.grouperDaemonLoader == true ? 'selected="selected"' : ''}>${textContainer.text['grouperDaemonConfigLoaderType']}</option>
					        <c:forEach items="${grouperRequestContainer.adminContainer.allGrouperDaemonTypesConfiguration}" var="grouperDaemonConfiguration">
					          
					          <c:if test="${grouperDaemonConfiguration.multiple}">
					          	<option value="${grouperDaemonConfiguration['class'].name}"
					              ${guiGrouperDaemonConfiguration.grouperDaemonConfiguration['class'].name == grouperDaemonConfiguration['class'].name ? 'selected="selected"' : '' }
					              >${grouperDaemonConfiguration.title}</option>
					          </c:if>
					        </c:forEach>
					      </select>
					      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
					      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
					      <br />
					      <span class="description">${textContainer.text['grouperDaemonTypeHint']}</span>
                      </td>
                    </tr>
                    
                    <c:if test="${grouperRequestContainer.adminContainer.grouperDaemonLoader}">
                    	
                    	<tr>
                    		<td colspan="3">
                    			${textContainer.text['grouperDaemonConfigLoaderInstructions']}
                    		</td>
                    	</tr>
                    	
                    </c:if>
                    
                    <c:if test="${guiGrouperDaemonConfiguration != null}">
						
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
                                ajaxCallback="ajax('../app/UiV2Admin.addDaemon?daemonConfigId=${guiGrouperDaemonConfiguration.grouperDaemonConfiguration.configId}&daemonConfigType=${guiGrouperDaemonConfiguration.grouperDaemonConfiguration['class'].name}', {formIds: 'addDaemonFormId'}); return false;"
                                valuesAndLabels="${attribute.dropdownValuesAndLabels }"
                                checkboxAttributes="${attribute.checkboxAttributes}"
                                indent="${attribute.configItemMetadata.indent}"
                              />
                              
                            </c:forEach>
                            
                          </tbody>
                    
                      </c:forEach>
            
                    </c:if>
                    </tbody>
                    
                    <tr>
                      <td>&nbsp;</td>
                      <td>&nbsp;</td>
                      <td style="white-space: nowrap; padding-top: 1em; padding-bottom: 1em;">
	                       <input type="submit" class="btn btn-primary"
	                       aria-controls="addDaemonFormId" id="addDaemonSubmitButtonId"
	                       value="${textContainer.text['grouperDaemonAddEditSubmitButton'] }"
	                       onclick="ajax('../app/UiV2Admin.addDaemonSubmit', {formIds: 'addDaemonFormId'}); return false;">
                      		&nbsp; 
                      		<a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2Admin.daemonJobs'); return false;"
                          >${textContainer.text['grouperDaemonConfigCancelButton'] }</a>
                      
                      </td>
                    </tr>
                </table>
                
              </form>

            </div>
          </div>