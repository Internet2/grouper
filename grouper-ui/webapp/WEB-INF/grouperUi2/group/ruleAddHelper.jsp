<%@ include file="../assetsJsp/commonTaglib.jsp"%>


                      <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRulePatternId">${textContainer.text['grouperRulePatternLabel']}</label></strong></td>
                        <td>
                          <select name="grouperRulePattern" id="grouperRulePatternId" style="width: 30em"
                              onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                           
                            <option value=""></option>
                            <c:forEach items="${grouperRequestContainer.rulesContainer.allPatterns}" var="pattern">
                              <option value="${pattern.key}"
                                  ${grouperRequestContainer.rulesContainer.ruleConfig.pattern == pattern.key ? 'selected="selected"' : '' }
                                  >${pattern.value}</option>
                            </c:forEach>
                          </select>
                          <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                          data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                          <br />
                          <span class="description">${textContainer.text['grouperRulePatternHint']}</span>
                        </td>
                      </tr>
                      
                      <c:if test="${!grouper:isBlank(grouperRequestContainer.rulesContainer.ruleConfig.pattern) and grouperRequestContainer.rulesContainer.ruleConfig.pattern != 'custom'}">
                      
                          <c:forEach items="${grouperRequestContainer.rulesContainer.ruleConfig.elementsToShow}" var="attribute">  
                            
                           <%--    <c:set target="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration}"
                                      property="index"
                                      value="${attribute.repeatGroupIndex}" />  
                              <c:set target="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration}"
                                      property="currentConfigSuffix"
                                      value="${attribute.configSuffix}" />   --%>

                              <%--  ajaxCallback="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration?focusOnElementName=config_${attribute.configSuffix}&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}', {formIds: 'provisionerConfigDetails'}); return false;" --%>
                              <grouper:configFormElement 
                                formElementType="${attribute.formElement}" 
                                shouldShowElCheckbox = "false"
                                configId="${attribute.configSuffix}" 
                                label="${attribute.label}"
                                readOnly="${attribute.readOnly}"
                                helperText="${attribute.description}"
                                helperTextDefaultValue="${attribute.defaultValue}"
                                required="${attribute.required}"
                                shouldShow="${attribute.show}"
                                value="${attribute.valueOrExpressionEvaluation}"
                                hasExpressionLanguage="false"
                                valuesAndLabels="${attribute.dropdownValuesAndLabels }"
                                checkboxAttributes="${attribute.checkboxAttributes}"
                                indent="${attribute.configItemMetadata.indent}"
                              />
                              
                        </c:forEach>
                        
                         <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.canSetDaemon}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleRunDaemonId">${textContainer.text['grouperRuleRunDaemonLabel']}</label></strong></td>
                              <td>
                                
                                <select name="grouperRuleRunDaemon" id="grouperRuleRunDaemonId" style="width: 30em" onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                                  <option value=""></option> 
                                  <option value="true" ${grouperRequestContainer.rulesContainer.ruleConfig.runDaemon == true ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleRunDaemonYesLabel']}</option>
                                  <option value="false" ${grouperRequestContainer.rulesContainer.ruleConfig.runDaemon == false ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleRunDaemonNoLabel']}</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['grouperRuleRunDaemonHint']}</span>
                              </td>
                            </tr>
                        </c:if>
                      
                      </c:if>
                     
                     <c:if test="${!grouper:isBlank(grouperRequestContainer.rulesContainer.ruleConfig.pattern) and grouperRequestContainer.rulesContainer.ruleConfig.pattern == 'custom'}">
                      <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckTypeId">${textContainer.text['grouperRuleCheckTypeLabel']}</label></strong></td>
                        <td>
                          <%-- <input type="hidden" name="grouperReportConfigTypeName" value="${grouperRequestContainer.grouperReportContainer.reportConfigType}" /> --%>
                          <select name="grouperRuleCheckType" id="grouperRuleCheckTypeId" style="width: 30em"
                              onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                           
                            <option value=""></option>
                            <c:forEach items="${grouperRequestContainer.rulesContainer.allCheckTypes}" var="checkType">
                              <option value="${checkType.key}"
                                  ${grouperRequestContainer.rulesContainer.ruleConfig.checkType == checkType.key ? 'selected="selected"' : '' }
                                  >${checkType.value}</option>
                            </c:forEach>
                          </select>
                          <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                          data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                          <br />
                          <span class="description">${textContainer.text['grouperRuleCheckTypeHint']}</span>
                        </td>
                      </tr>
                     
                    
                    <c:if test="${!grouper:isBlank(grouperRequestContainer.rulesContainer.ruleConfig.checkType) and grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerType == 'FOLDER'}">
                      
                           <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckOwnerUuidOrNameId">${textContainer.text['grouperRuleCheckOwnerUuidOrNameLabel']}</label></strong></td>
                            <td>
                              <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerUuidOrName)}"
                                  name="grouperRuleCheckOwnerUuidOrName" id="grouperRuleCheckOwnerAnotherUuidOrNameId" />
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              <br />
                              <span class="description">${textContainer.text['grouperRuleCheckOwnerUuidOrNameHint']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckOwnerStemScopeId">${textContainer.text['grouperRuleOwnerStemScopeLabel']}</label></strong></td>
                              <td>
                                
                                <select name="grouperRuleCheckOwnerStemScope" id="grouperRuleCheckOwnerStemScopeId" style="width: 30em" onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                                  <option value="SUB" ${grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerStemScope == 'SUB' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleOwnerSubStemScopeLabel']}</option>
                                  <option value="ONE" ${grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerStemScope == 'ONE' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleOwnerOneStemScopeLabel']}</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['grouperRuleOwnerStemScopeDescription']}</span>
                              </td>
                          </tr>
                        
                     </c:if>
                     
                       <c:if test="${!grouper:isBlank(grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerType) and grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerType == 'GROUP'}">
                      
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckOwnerId">${textContainer.text['grouperRuleCheckOwnerLabel']}</label></strong></td>
                          <td>
                            
                            <select name="grouperRuleCheckOwner" id="grouperRuleCheckOwnerId" style="width: 30em" onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                              <option value=""></option>
                              <option value="thisGroup" ${grouperRequestContainer.rulesContainer.ruleConfig.checkOwner == 'thisGroup' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleCheckGroupOwnerThisGroup']}</option>
                              <option value="anotherGroup" ${grouperRequestContainer.rulesContainer.ruleConfig.checkOwner == 'anotherGroup' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleCheckGroupOwnerAnotherGroup']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleCheckOwnerHint']}</span>
                          </td>
                        </tr>
                        
                        <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerType == 'GROUP' and grouperRequestContainer.rulesContainer.ruleConfig.checkOwner == 'anotherGroup'}">
                          
                           <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckOwnerUuidOrNameId">${textContainer.text['grouperRuleCheckOwnerUuidOrNameLabel']}</label></strong></td>
                            <td>
                              <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.checkOwnerUuidOrName)}"
                                  name="grouperRuleCheckOwnerUuidOrName" id="grouperRuleCheckOwnerAnotherUuidOrNameId" />
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              <br />
                              <span class="description">${textContainer.text['grouperRuleCheckOwnerUuidOrNameHint']}</span>
                            </td>
                          </tr>
                        
                        </c:if>
                        
                     </c:if> 
                    
                     
                    <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.checkUsesArg0}">
                     
                       <tr>
                         <c:set var="arg0TextKey" value="rulesCheckArgHumanFriendlyLabel_${grouperRequestContainer.rulesContainer.ruleConfig.checkType}_Arg0" />
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckArg0Id">${textContainer.text[arg0TextKey]}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.checkArg0)}"
                                name="grouperRuleCheckArg0" id="grouperRuleCheckArg0Id" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            
                            <c:set var="arg0TextKeyHint" value="rulesCheckArgHumanFriendlyDescription_${grouperRequestContainer.rulesContainer.ruleConfig.checkType}_Arg0" />
                            <span class="description">${textContainer.text[arg0TextKeyHint]}</span>
                          </td>
                       </tr>
                     
                     </c:if>
                     
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.checkUsesArg1}">
                     
                       <tr>
                        
                        <c:set var="arg1TextKey" value="rulesCheckArgHumanFriendlyLabel_${grouperRequestContainer.rulesContainer.ruleConfig.checkType}_Arg1" />
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckArg1Id">${textContainer.text[arg1TextKey]}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.checkArg1)}"
                                name="grouperRuleCheckArg1" id="grouperRuleCheckArg1Id" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <c:set var="arg1TextKeyHint" value="rulesCheckArgHumanFriendlyDescription_${grouperRequestContainer.rulesContainer.ruleConfig.checkType}_Arg1" />
                            <span class="description">${textContainer.text[arg1TextKeyHint]}</span>
                          </td>
                       </tr>
                     
                     </c:if>
                     
                     <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionOptionsId">${textContainer.text['grouperRuleIfConditionOptionsLabel']}</label></strong></td>
                        <td>
                          <select name="grouperRuleIfConditionOption" id="grouperRuleIfConditionOptionsId" style="width: 30em"
                            onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                         
                          <option value=""></option>
                          <c:forEach items="${grouperRequestContainer.rulesContainer.allIfConditionOptions}" var="ifConditionOption">
                            <option value="${ifConditionOption.key}"
                                ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOption == ifConditionOption.key ? 'selected="selected"' : '' }
                                >${ifConditionOption.value}</option>
                          </c:forEach>
                        </select>
                          <br />
                          <span class="description">${textContainer.text['grouperRuleIfConditionOptionsHint']}</span>
                        </td>
                      </tr>
                      
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOption == 'EL'}">
                      
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
                      
                      
                   
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerType == 'FOLDER'}">
                      
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
                          
                         <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckOwnerStemScopeId">${textContainer.text['grouperRuleOwnerStemScopeLabel']}</label></strong></td>
                          <td>
                            
                            <select name="grouperRuleIfConditionOwnerStemScope" id="grouperRuleIfConditionOwnerStemScopeId" style="width: 30em" onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                              <option value="SUB" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerStemScope == 'SUB' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleOwnerSubStemScopeLabel']}</option>
                              <option value="ONE" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerStemScope == 'ONE' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleOwnerOneStemScopeLabel']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleOwnerStemScopeDescription']}</span>
                          </td>
                        </tr>
                        
                        </c:if>
                        
                    <%--  </c:if> --%>
                      
                     
                     
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerType == 'GROUP'}">
                      
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionOwnerId">${textContainer.text['grouperRuleIfConditionOwnerLabel']}</label></strong></td>
                          <td>
                            
                            <select name="grouperRuleIfConditionOwner" id="grouperRuleIfConditionOwnerId" style="width: 30em" onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                              <option value=""></option>
                              <option value="thisGroup" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwner == 'thisGroup' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleCheckGroupOwnerThisGroup']}</option>
                              <option value="anotherGroup" ${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwner == 'anotherGroup' ? 'selected="selected"' : ''}>${textContainer.textEscapeXml['grouperRuleCheckGroupOwnerAnotherGroup']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['grouperRuleIfConditionOwnerHint']}</span>
                          </td>
                        </tr>
                        
                        <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerType == 'GROUP' and grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwner == 'anotherGroup'}">
                          
                           <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleCheckOwnerUuidOrNameId">${textContainer.text['grouperRuleIfConditionOwnerUuidOrNameLabel']}</label></strong></td>
                            <td>
                              <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOwnerUuidOrName)}"
                                  name="grouperRuleIfConditionOwnerUuidOrName" id="ifConditionOwnerUuidOrNameId" />
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              <br />
                              <span class="description">${textContainer.text['grouperRuleIfConditionOwnerUuidOrNameHint']}</span>
                            </td>
                          </tr>
                        
                        </c:if>
                        
                     </c:if>
                     
                      
                    <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifUsesArg0}">
                     
                       <tr>
                          <c:set var="arg0TextKeyCondition" value="rulesConditionArgHumanFriendlyLabel_${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOption}_Arg0" />
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionArg0Id">${textContainer.text[arg0TextKeyCondition]}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.ifConditionArg0)}"
                                name="grouperRuleIfConditionArg0" id="grouperRuleIfConditionArg0Id" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            
                            <c:set var="arg0TextKeyConditionHint" value="rulesConditionArgHumanFriendlyDescription_${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOption}_Arg0" />
                            <span class="description">${textContainer.text[arg0TextKeyConditionHint]}</span>
                          </td>
                       </tr>
                     
                     </c:if>
                     
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.ifUsesArg1}">
                     
                       <tr>
                       
                          <c:set var="arg1TextKeyCondition" value="rulesConditionArgHumanFriendlyLabel_${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOption}_Arg1" />
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleIfConditionArg1Id">${textContainer.text[arg1TextKeyCondition]}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.ifConditionArg1)}"
                                name="grouperRuleIfConditionArg1" id="grouperRuleIfConditionArg1Id" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <c:set var="arg1TextKeyConditionHint" value="rulesConditionArgHumanFriendlyDescription_${grouperRequestContainer.rulesContainer.ruleConfig.ifConditionOption}_Arg1" />
                            <span class="description">${textContainer.text[arg1TextKeyConditionHint]}</span>
                          </td>
                       </tr>
                     
                     </c:if>
                     
                     <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleThenOptionsId">${textContainer.text['grouperRuleThenOptionsLabel']}</label></strong></td>
                        <td>
                          <select name="grouperRuleThenOption" id="grouperRuleThenOptionsId" style="width: 30em"
                            onchange="ajax('../app/UiV2Group.addRuleOnGroup', {formIds: 'addRuleConfigFormId'}); return false;">
                         
                          <option value=""></option>
                          <c:forEach items="${grouperRequestContainer.rulesContainer.allThenOptions}" var="thenOption">
                            <option value="${thenOption.key}"
                                ${grouperRequestContainer.rulesContainer.ruleConfig.thenOption == thenOption.key ? 'selected="selected"' : '' }
                                >${thenOption.value}</option>
                          </c:forEach>
                        </select>
                          <br />
                          <span class="description">${textContainer.text['grouperRuleThenOptionsHint']}</span>
                        </td>
                     </tr>
                      
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.thenOption == 'EL'}">
                      
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
                    
                    <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.thenUsesArg0}">
                     
                       <tr>
                       
                          <c:set var="arg0TextKeyThen" value="rulesThenArgHumanFriendlyLabel_${grouperRequestContainer.rulesContainer.ruleConfig.thenOption}_Arg0" />
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleThenArg0Id">${textContainer.text[arg0TextKeyThen]}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.thenArg0)}"
                                name="grouperRuleThenArg0" id="grouperRuleThenArg0Id" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            
                            <c:set var="arg0TextKeyThenHint" value="rulesThenArgHumanFriendlyDescription_${grouperRequestContainer.rulesContainer.ruleConfig.thenOption}_Arg0" />
                            <span class="description">${textContainer.text[arg0TextKeyThenHint]}</span>
                          </td>
                       </tr>
                     
                     </c:if>
                     
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.thenUsesArg1}">
                     
                       <tr>
                          <c:set var="arg1TextKeyThen" value="rulesThenArgHumanFriendlyLabel_${grouperRequestContainer.rulesContainer.ruleConfig.thenOption}_Arg1" />
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleThenArg1Id">${textContainer.text[arg1TextKeyThen]}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.thenArg1)}"
                                name="grouperRuleThenArg1" id="grouperRuleThenArg1Id" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <c:set var="arg1TextKeyThenHint" value="rulesThenArgHumanFriendlyDescription_${grouperRequestContainer.rulesContainer.ruleConfig.thenOption}_Arg1" />
                            <span class="description">${textContainer.text[arg1TextKeyThenHint]}</span>
                          </td>
                       </tr>
                     
                     </c:if>
                     
                     <c:if test="${grouperRequestContainer.rulesContainer.ruleConfig.thenUsesArg2}">
                     
                       <tr>
                          <c:set var="arg2TextKeyThen" value="rulesThenArgHumanFriendlyLabel_${grouperRequestContainer.rulesContainer.ruleConfig.thenOption}_Arg2" />
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperRuleThenArg2Id">${textContainer.text[arg2TextKeyThen]}</label></strong></td>
                          <td>
                            <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.rulesContainer.ruleConfig.thenArg2)}"
                                name="grouperRuleThenArg2" id="grouperRuleThenArg2Id" />
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <c:set var="arg2TextKeyThenHint" value="rulesThenArgHumanFriendlyDescription_${grouperRequestContainer.rulesContainer.ruleConfig.thenOption}_Arg2" />
                            <span class="description">${textContainer.text[arg2TextKeyThenHint]}</span>
                          </td>
                       </tr>
                     
                     </c:if>
                     
                     </c:if>  
                     