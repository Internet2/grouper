<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
                      <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigHasTypeId">${textContainer.text['grouperReportConfigTypeLabel']}</label></strong></td>
                        <td>
                          <%-- <input type="hidden" name="grouperReportConfigTypeName" value="${grouperRequestContainer.grouperReportContainer.reportConfigType}" /> --%>
                          <select name="grouperReportConfigType" id="grouperReportConfigHasTypeId" style="width: 30em"
                              onchange="ajax('../app/UiV2GrouperReport.reportOn${ObjectType}Add', {formIds: 'addReportConfigFormId'}); return false;">
                           
                            <option value=""></option>
                            <c:forEach items="${grouperRequestContainer.grouperReportContainer.allReportConfigTypes}" var="reportConfigType">
                              <option value="${reportConfigType}"
                                  ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigType == reportConfigType ? 'selected="selected"' : '' }
                                  >${reportConfigType}</option>
                            </c:forEach>
                          </select>
                          <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                          data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                          <br />
                          <span class="description">${textContainer.text['grouperReportConfigTypeHint']}</span>
                        </td>
                      </tr>
                     
                      <c:if test="${!grouper:isBlank(grouperRequestContainer.grouperReportContainer.configBean.reportConfigType)}">
                      
                       
                      <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigType == 'SQL'}">
                      
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportSqlConfigId">${textContainer.text['grouperReportSqlConfigLabel']}</label></strong></td>
                          <td>
                            <select name="grouperReportSqlConfig" id="grouperReportSqlConfigId" style="width: 30em">
                             
                              <option value=""></option>
                              <c:forEach items="${grouperRequestContainer.grouperReportContainer.allSqlConfigs}" var="sqlConfig">
                                <option value="${sqlConfig}"
                                    ${grouperRequestContainer.grouperReportContainer.configBean.sqlConfig == sqlConfig ? 'selected="selected"' : '' }
                                    >${sqlConfig}</option>
                              </c:forEach>
                            </select>
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['grouperReportSqlConfigHint']}</span>
                          </td>
                        </tr>
                      
                      </c:if>
                      
                      
                        <tr>
	                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigHasFormatId">${textContainer.text['grouperReportConfigFormatLabel']}</label></strong></td>
	                        <td>
	                          <%-- <input type="hidden" name="grouperReportConfigFormatName" value="${grouperRequestContainer.grouperReportContainer.reportConfigFormat}" /> --%>
	                          <select name="grouperReportConfigFormat" id="grouperReportConfigHasFormatId" style="width: 30em"
	                              onchange="ajax('../app/UiV2GrouperReport.reportOn${ObjectType}Add', {formIds: 'addReportConfigFormId'}); return false;">
	                            
	                            <option value=""></option>
	                            <c:forEach items="${grouperRequestContainer.grouperReportContainer.allReportConfigFormats}" var="reportConfigFormat">
	                              <option value="${reportConfigFormat}"
	                                  ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigFormat == reportConfigFormat ? 'selected="selected"' : '' }
	                                  >${reportConfigFormat}</option>
	                            </c:forEach>
	                          </select>
	                          <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
	                          <br />
	                          <span class="description">${textContainer.text['grouperReportConfigFormatHint']}</span>
	                        </td>
                        </tr>
                      
                      </c:if>
                      
                      <c:if test="${!grouper:isBlank(grouperRequestContainer.grouperReportContainer.configBean.reportConfigFormat)}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigNameId">${textContainer.text['grouperReportConfigNameLabel']}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.grouperReportContainer.configBean.reportConfigName)}"
                                name="grouperReportConfigName" id="grouperReportConfigNameId" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['grouperReportConfigNameHint']}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigFileNameId">${textContainer.text['grouperReportConfigFileNameLabel']}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.grouperReportContainer.configBean.reportConfigFilename)}"
                                name="grouperReportConfigFileName" id="grouperReportConfigFileNameId" />
                            
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['grouperReportConfigFileNameHint']}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigDescriptionId">${textContainer.text['grouperReportConfigDescriptionLabel']}</label></strong></td>
                          <td>
                            <textarea id="grouperReportConfigDescriptionId" name="grouperReportConfigDescription" rows="6" cols="60" class="input-block-level"
                                >${grouper:escapeHtml(grouperRequestContainer.grouperReportContainer.configBean.reportConfigDescription)}</textarea>
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['grouperReportConfigDescriptionHint']}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigViewersGroupIdId">${textContainer.text['grouperReportConfigViewersGroupIdLabel']}</label></strong></td>
                          <td>
				                    <grouper:combobox2 idBase="grouperReportConfigViewersGroupCombo" style="width: 30em" 
				                       value="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigViewersGroupId}"
				                       filterOperation="../app/UiV2Group.groupUpdateFilter" />
                            
                            <br />
                            <span class="description">${textContainer.text['grouperReportConfigViewersGroupIdHint']}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigQuartzCronId">${textContainer.text['grouperReportConfigQuartzCronLabel']}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.grouperReportContainer.configBean.reportConfigQuartzCron)}"
                                name="grouperReportConfigQuartzCron" id="grouperReportConfigQuartzCronId" />
                             <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['grouperReportConfigQuartzCronHint']}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigSendEmailId">${textContainer.text['grouperReportConfigSendEmailLabel']}</label></strong></td>
                          <td>
                            <select name="grouperReportConfigSendEmail" id="grouperReportConfigSendEmailId" style="width: 30em"
                                onchange="ajax('../app/UiV2GrouperReport.reportOn${ObjectType}Add', {formIds: 'addReportConfigFormId'}); return false;">
                              <option value="false" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmail ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperReportConfigNoDoNotSendEmailLabel']}</option>
                              <option value="true" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmail ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperReportConfigYesSendEmailLabel']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperReportConfigSendEmailHint']}</span>
                          </td>
                        </tr>
                        
                        <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmail}">
                        
                          <tr>
	                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigEmailSubjectId">${textContainer.text['grouperReportConfigEmailSubjectLabel']}</label></strong></td>
	                          <td>
	                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.grouperReportContainer.configBean.reportConfigEmailSubject)}"
	                                name="grouperReportConfigEmailSubject" id="grouperReportConfigEmailSubjectId" />
	                            <br />
	                            <span class="description">${textContainer.text['grouperReportConfigEmailSubjectHint']}</span>
	                          </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigEmailBodyId">${textContainer.text['grouperReportConfigEmailBodyLabel']}</label></strong></td>
                            <td>
                              <textarea id="grouperReportConfigEmailBodyId" name="grouperReportConfigEmailBody" rows="6" cols="60" class="input-block-level"
                                >${grouper:escapeHtml(grouperRequestContainer.grouperReportContainer.configBean.reportConfigEmailBody)}</textarea>
                              <br />
                              <span class="description">${textContainer.text['grouperReportConfigEmailBodyHint']}</span>
                            </td>
                          </tr>
                          
                          <tr>
	                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigSendEmailToViewersId">${textContainer.text['grouperReportConfigSendEmailToViewersLabel']}</label></strong></td>
	                          <td>
	                            <select name="grouperReportConfigSendEmailToViewers" id="grouperReportConfigSendEmailToViewersId" style="width: 30em"
	                                onchange="ajax('../app/UiV2GrouperReport.reportOn${ObjectType}Add', {formIds: 'addReportConfigFormId'}); return false;">
	                              <option value="false" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailToViewers ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperReportConfigNoDoNotSendEmailToViewersLabel']}</option>
	                              <option value="true" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailToViewers ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperReportConfigYesSendEmailToViewersLabel']}</option>
	                            </select>
	                            <br />
	                            <span class="description">${textContainer.text['grouperReportConfigSendEmailToViewersHint']}</span>
	                          </td>
                          </tr>
                          
                          <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailToViewers == false}">
                          
	                          <tr>
		                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigSendEmailToGroupIdId">${textContainer.text['grouperReportConfigSendEmailToGroupIdLabel']}</label></strong></td>
		                          <td>
		                             <table cellspacing="0" cellpadding="0" border="0">
                                  <tr>
                                    <td style="padding: 0px; border: 0px; margin: 0px; background-color: white;">
      			                           <grouper:combobox2 idBase="grouperReportConfigSendEmailToGroupCombo" style="width: 30em" 
      	                               value="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailToGroupId}"
      	                               filterOperation="../app/UiV2Group.groupUpdateFilter" />     
		                                </td>
                                    <td>
      		                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                                    </td>
                                  </tr>
                                </table>
		                            <br />
		                            <span class="description">${textContainer.text['grouperReportConfigSendEmailToGroupIdHint']}</span>
		                          </td>
	                          </tr>
                          
                          </c:if>
                        </c:if>
                          
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigStoreWithNoDataId">${textContainer.text['grouperReportConfigStoreWithNoDataLabel']}</label></strong></td>
                          <td>
                            <select name="grouperReportConfigStoreWithNoData" id="grouperReportConfigStoreWithNoDataId" style="width: 30em"
                                onchange="ajax('../app/UiV2GrouperReport.reportOn${ObjectType}Add', {formIds: 'addReportConfigFormId'}); return false;">
                              <option value="false" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigStoreWithNoData ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperReportConfigNoDoNotStoreWithNoDataLabel']}</option>
                              <option value="true" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigStoreWithNoData ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperReportConfigYesStoreWithNoDataLabel']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperReportConfigStoreWithNoDataHint']}</span>
                          </td>
                        </tr>
                        
                        <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmail && grouperRequestContainer.grouperReportContainer.configBean.reportConfigStoreWithNoData}">
                            
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigSendEmailWithNoDataId">${textContainer.text['grouperReportConfigSendEmailWithNoDataLabel']}</label></strong></td>
                            <td>
                              <select name="grouperReportConfigSendEmailWithNoData" id="grouperReportConfigSendEmailWithNoDataId" style="width: 30em"
                                  onchange="ajax('../app/UiV2GrouperReport.reportOn${ObjectType}Add', {formIds: 'addReportConfigFormId'}); return false;">
                                <option value="false" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailWithNoData ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperReportConfigNoDoNotEmailWithNoDataLabel']}</option>
                                <option value="true" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailWithNoData ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperReportConfigYesEmailWithNoDataLabel']}</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['grouperReportConfigSendEmailWithNoDataHint']}</span>
                            </td>
                          </tr>
                        
                        
                          
                        </c:if>
                        <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigType == 'SQL'}">
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigQueryId">${textContainer.text['grouperReportConfigQueryLabel']}</label></strong></td>
                            <td>
                              <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.grouperReportContainer.configBean.reportConfigQuery)}"
                                  name="grouperReportConfigQuery" id="grouperReportConfigQueryId" />
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              <br />
                              <span class="description">${textContainer.text['grouperReportConfigQueryHint']}</span>
                            </td>
                          </tr>
                        </c:if>
                        <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigType == 'GSH'}">
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigScriptId">${textContainer.text['grouperReportConfigScriptLabel']}</label></strong></td>
                            <td>
                              <span style="white-space: nowrap;">
                              <textarea id="grouperReportConfigScriptId" name="grouperReportConfigScript" rows="10" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.grouperReportContainer.configBean.reportConfigScript)}</textarea>
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              </span>
                              <br />
                              <span class="description">${textContainer.text['grouperReportConfigScriptHint']}</span>
                            </td>
                          </tr>
                        </c:if>
                          
                      </c:if>
