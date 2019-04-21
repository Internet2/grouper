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
                        
                        <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigFormat == 'CSV'}">
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
                        </c:if>
                        
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
	                              <option value="false" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailToViewers ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperReportConfigNoDoNotSendEmailLabel']}</option>
	                              <option value="true" ${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailToViewers ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['grouperReportConfigYesSendEmailLabel']}</option>
	                            </select>
	                            <br />
	                            <span class="description">${textContainer.text['grouperReportConfigSendEmailToViewersHint']}</span>
	                          </td>
                          </tr>
                          
                          <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailToViewers == false}">
                          
	                          <tr>
		                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigSendEmailToGroupIdId">${textContainer.text['grouperReportConfigSendEmailToGroupIdLabel']}</label></strong></td>
		                          <td>
		                                
			                           <grouper:combobox2 idBase="grouperReportConfigSendEmailToGroupCombo" style="width: 30em" 
	                               value="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigSendEmailToGroupId}"
	                               filterOperation="../app/UiV2Group.groupUpdateFilter" />     
		                                
		                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                  data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
		                            <br />
		                            <span class="description">${textContainer.text['grouperReportConfigSendEmailToGroupIdHint']}</span>
		                          </td>
	                          </tr>
                          
                          </c:if>
                          
                        </c:if>
                        
                        <c:if test="${grouperRequestContainer.grouperReportContainer.configBean.reportConfigFormat == 'CSV'}">
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
                        
                      </c:if>
