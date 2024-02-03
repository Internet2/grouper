<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
                      <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperReportConfigHasTypeId">${textContainer.text['grouperRuleCheckTypeLabel']}</label></strong></td>
                        <td>
                          <%-- <input type="hidden" name="grouperReportConfigTypeName" value="${grouperRequestContainer.grouperReportContainer.reportConfigType}" /> --%>
                          <select name="grouperRuleCheckType" id="grouperRuleCheckTypeId" style="width: 30em"
                              onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                           
                            <option value=""></option>
                            <c:forEach items="${grouperRequestContainer.rulesContainer.allCheckTypes}" var="checkType">
                              <option value="${checkType}"
                                  ${grouperRequestContainer.rulesContainer.ruleConfig.checkType == checkType ? 'selected="selected"' : '' }
                                  >${checkType}</option>
                            </c:forEach>
                          </select>
                          <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                          data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                          <br />
                          <span class="description">${textContainer.text['grouperRuleCheckTypeHint']}</span>
                        </td>
                      </tr>
                     
                      <c:if test="${!grouper:isBlank(grouperRequestContainer.rulesContainer.ruleConfig.checkType)}">
                        
                         <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckIsOwnerId">${textContainer.text['grouperRuleCheckIsOwnerLabel']}</label></strong></td>
                          <td>
                            
                            <select name="grouperRuleCheckIsOwner" id="grouperRuleCheckIsOwnerId" style="width: 30em" onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                              <option value="true" ${grouperRequestContainer.rulesContainer.ruleConfig.checkOwner == true ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperDaemonConfigEnableYes']}</option>
                              <option value="false" ${grouperRequestContainer.rulesContainer.ruleConfig.checkOwner == false ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperDaemonConfigEnableNo']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleCheckIsOwnerHint']}</span>
                          </td>
                        </tr>
                        
                     </c:if>
                     
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.checkOwner}">
                        
                         <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckOwnerTypeId">${textContainer.text['grouperRuleCheckOwnerTypeLabel']}</label></strong></td>
                          <td>
                            
                            <select name="grouperRuleCheckOwnerType" id="grouperRuleCheckOwnerTypeId" style="width: 30em" onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                              <option value=""></option>
                              <option value="group" ${grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerType == 'group' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleCheckOwnerTypeGroup']}</option>
                              <option value="stem" ${grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerType == 'stem' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleCheckOwnerTypeStem']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleCheckOwnerTypeHint']}</span>
                          </td>
                        </tr>
                        
                     </c:if>
                     
                     <c:if test="${!grouper:isBlank(grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerType)}">
                        
                         <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckOwnerUuidOrNameId">${textContainer.text['grouperRuleCheckOwnerUuidOrNameLabel']}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerUuidOrName)}"
                                name="grouperRuleCheckOwnerUuidOrName" id="grouperRuleCheckOwnerUuidOrNameId" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleCheckOwnerUuidOrNameHint']}</span>
                          </td>
                        </tr>
                        
                     </c:if>
                     
                     <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionExistsId">${textContainer.text['grouperRuleIfConditionExistsLabel']}</label></strong></td>
                        <td>
                          
                          <select name="grouperRuleIfConditionExists" id="grouperRuleIfConditionExistsId" style="width: 30em" onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                            <option value=""></option>
                            <option value="true" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionExists == true ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperDaemonConfigEnableYes']}</option>
                            <option value="false" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionExists == false ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperDaemonConfigEnableNo']}</option>
                          </select>
                          <br />
                          <span class="description">${textContainer.text['grouperRuleIfConditionExistsHint']}</span>
                        </td>
                    </tr>
                    
                    <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionExists}">
                       <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionTypeId">${textContainer.text['grouperRuleIfConditionTypeLabel']}</label></strong></td>
                        <td>
                          <select name="grouperRuleIfConditionType" id="grouperRuleIfConditionTypeId" style="width: 30em" onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                            <option value=""></option>
                            <option value="enum" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionType == 'enum' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleIfConditionTypeEnum']}</option>
                            <option value="el" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionType == 'el' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleIfConditionTypeEL']}</option>
                          </select>
                          <br />
                          <span class="description">${textContainer.text['grouperRuleIfConditionTypeHint']}</span>
                        </td>
                      </tr>
                      
                      <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionType == 'enum'}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionEnumId">${textContainer.text['grouperRuleIfConditionEnumLabel']}</label></strong></td>
                          <td>
                            <select name="grouperRuleIfConditionEnum" id="grouperRuleIfConditionEnumId" style="width: 30em"
                              onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                           
                            <option value=""></option>
                            <c:forEach items="${grouperRequestContainer.rulesContainer.allIfConditionEnums}" var="ifConditionEnum">
                              <option value="${ifConditionEnum}"
                                  ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionEnum == ifConditionEnum ? 'selected="selected"' : '' }
                                  >${ifConditionEnum}</option>
                            </c:forEach>
                          </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleIfConditionEnumHint']}</span>
                          </td>
                        </tr>
                      </c:if> 
                      
                      <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionType == 'el'}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionELId">${textContainer.text['grouperRuleIfConditionElLabel']}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.ifConditionEl)}"
                                name="grouperRuleIfConditionEL" id="grouperRuleIfConditionELId" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleIfConditionElHint']}</span>
                          </td>
                        </tr>
                      </c:if> 
                      
                      <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionIsOwnerId">${textContainer.text['grouperRuleIfConditionIsOwnerLabel']}</label></strong></td>
                          <td>
                            
                            <select name="grouperRuleIfConditionIsOwner" id="grouperRuleIfConditionIsOwnerId" style="width: 30em" onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                              <option value="true" ${grouperRequestContainer.rulesContainer.ruleConfig.isIfConditionOwner == true ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperDaemonConfigEnableYes']}</option>
                              <option value="false" ${grouperRequestContainer.rulesContainer.ruleConfig.isIfConditionOwner == false ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperDaemonConfigEnableNo']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleIfConditionIsOwnerHint']}</span>
                          </td>
                       </tr>
                       
                       <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.isIfConditionOwner}">
                        
                         <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionOwnerTypeId">${textContainer.text['grouperRuleIfConditionOwnerTypeLabel']}</label></strong></td>
                          <td>
                            
                            <select name="grouperRuleIfConditionOwnerType" id="grouperRuleIfConditionOwnerTypeId" style="width: 30em" onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                              <option value=""></option>
                              <option value="group" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerType == 'group' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleCheckOwnerTypeGroup']}</option>
                              <option value="stem" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerType == 'stem' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleCheckOwnerTypeStem']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleIfConditionOwnerTypeHint']}</span>
                          </td>
                        </tr>
                        
                        <c:if test="${!grouper:isBlank(grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerType)}">
                        
                           <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionOwnerUuidOrNameId">${textContainer.text['grouperRuleIfConditionOwnerUuidOrNameLabel']}</label></strong></td>
                            <td>
                              <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerUuidOrName)}"
                                  name="grouperRuleIfConditionOwnerUuidOrName" id="grouperRuleIfConditionOwnerUuidOrNameId" />
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              <br />
                              <span class="description">${textContainer.text['grouperRuleIfConditionOwnerUuidOrNameHint']}</span>
                            </td>
                          </tr>
                        
                        </c:if>
                        
                     </c:if>
                      
                    </c:if>  
                    
                    
                    <tr>
                      <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleThenTypeId">${textContainer.text['grouperRuleThenTypeLabel']}</label></strong></td>
                      <td>
                        <select name="grouperRuleThenType" id="grouperRuleThenTypeId" style="width: 30em" onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                          <option value=""></option>
                          <option value="enum" ${grouperRequestContainer.rulesContainer.ruleConfig.thenType == 'enum' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleIfConditionTypeEnum']}</option>
                          <option value="el" ${grouperRequestContainer.rulesContainer.ruleConfig.thenType == 'el' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleIfConditionTypeEL']}</option>
                        </select>
                        <br />
                        <span class="description">${textContainer.text['grouperRuleThenTypeHint']}</span>
                      </td>
                    </tr> 
                    
                    <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.thenType == 'enum'}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleThenEnumId">${textContainer.text['grouperRuleThenEnumLabel']}</label></strong></td>
                          <td>
                            <select name="grouperRuleThenEnum" id="grouperRuleThenEnumId" style="width: 30em"
                              onchange="ajax('../app/UiV2Stem.addRuleOnStem', {formIds: 'addRuleConfigFormId'}); return false;">
                           
                            <option value=""></option>
                            <c:forEach items="${grouperRequestContainer.rulesContainer.allThenConditionEnums}" var="thenEnum">
                              <option value="${thenEnum}"
                                  ${grouperRequestContainer.rulesContainer.ruleConfig.thenEnum == thenEnum ? 'selected="selected"' : '' }
                                  >${thenEnum}</option>
                            </c:forEach>
                          </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleThenEnumHint']}</span>
                          </td>
                        </tr>
                      </c:if> 
                      
                      <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.thenType == 'el'}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleThenELId">${textContainer.text['grouperRuleThenElLabel']}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.thenEl)}"
                                name="grouperRuleThenEL" id="grouperRuleThenELId" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleThenElHint']}</span>
                          </td>
                        </tr>
                      </c:if> 
                      
                      
                     