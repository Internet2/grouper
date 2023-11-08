<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('scriptTesterScreenPageTitle')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousScriptTesterOverallBreadcrumb'] }</li>
              </ul>
               
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousScriptTesterMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%-- <%@ include file="sqlSyncConfigsMoreActionsButtonContents.jsp"%> --%>
                  </div>
                </div>
                
                <div class="row-fluid">
                  <div class="lead span12 pull-left" style="color: darkred; font-weight: bold;">
                    ${textContainer.text['miscellaneousScriptTesterWarningMessage'] }
                  </div>
                  </div>
                </div>
                
              </div>
              
        <div class="row-fluid">
          <div class="span12">
           <div id="messages"></div>
               
               <form class="form-inline form-small form-filter" id="scriptTesterForm">
                  <table class="table table-condensed table-striped">
                    <tbody>
                      
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="scriptTypeId">${textContainer.text['scriptTypeLabel']}</label></strong></td>
                          <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                          <td>
                          <input type="hidden" name="previousScriptType" value="${grouperRequestContainer.scriptTesterContainer.selectedScriptType}" />
                            <select name="scriptType" id="scriptTypeId" style="width: 30em"
                            onchange="ajax('../app/UiV2ScriptTester.testScript', {formIds: 'scriptTesterForm'}); return false;"
                            >
                              <option value=""></option>
                              <c:forEach items="${grouperRequestContainer.scriptTesterContainer.allScriptTypes}" var="scriptType">
                                <option value="${scriptType}"
                                    ${grouperRequestContainer.scriptTesterContainer.selectedScriptType == scriptType ? 'selected="selected"' : '' }
                                    >${scriptType}</option>
                              </c:forEach>
                            </select>
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <c:if test="${!grouper:isBlank(grouperRequestContainer.scriptTesterContainer.scriptDescription)}">
                              <span class="description">${grouperRequestContainer.scriptTesterContainer.scriptDescription}</span>
                            </c:if>
                            <c:if test="${grouper:isBlank(grouperRequestContainer.scriptTesterContainer.scriptDescription)}">
                              <span class="description">${textContainer.text['scriptTypeHint']}</span>
                            </c:if>
                          </td>
                        </tr>
                        
                       <c:if test="${!grouper:isBlank(grouperRequestContainer.scriptTesterContainer.selectedScriptType)}">
                       
                         <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="exampleTypeId">${textContainer.text['exampleLabel']}</label></strong></td>
                            <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                            <td>
                            
                            <select name="exampleType" id="exampleTypeId" style="width: 30em"
                            onchange="ajax('../app/UiV2ScriptTester.testScript', {formIds: 'scriptTesterForm'}); return false;"
                            >
                              <option value=""></option>
                              <c:forEach items="${grouperRequestContainer.scriptTesterContainer.examplesAvailableForSelectedScriptType}" var="example">
                                <option value="${example}"
                                    ${grouperRequestContainer.scriptTesterContainer.selectedExample == example ? 'selected="selected"' : '' }
                                    >${example}</option>
                              </c:forEach>
                            </select>
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            
                            <c:if test="${!grouper:isBlank(grouperRequestContainer.scriptTesterContainer.exampleDescription)}">
                              <span class="description">${grouperRequestContainer.scriptTesterContainer.exampleDescription}</span>
                            </c:if>
                            <c:if test="${grouper:isBlank(grouperRequestContainer.scriptTesterContainer.exampleDescription)}">
                              <span class="description">${textContainer.text['exampleHint']}</span>
                            </c:if>
                            </td>
                          </tr>
                       </c:if>
                       
                       <c:if test="${!grouper:isBlank(grouperRequestContainer.scriptTesterContainer.selectedExample)}">
                       
                         <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="availableBeansId">${textContainer.text['availableBeansLabel']}</label></strong></td>
                            <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                            <td>
                            
                              <textarea id="availableBeansId" name="availableBeansGshScript" rows="20" cols="25" style="width: 500px;">${grouper:escapeHtml(grouperRequestContainer.scriptTesterContainer.availableBeansForSelectedScriptType)}</textarea>
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                               <br/>
                              <span class="description">${textContainer.text['availableBeansHint']}</span>
                            </td>
                          </tr>
                          
                          
                          <c:if test="${grouperRequestContainer.scriptTesterContainer.showNullCheckingJexlScript}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="nullCheckingJexlScriptId">${textContainer.text['nullCheckingJexlScriptLabel']}</label></strong></td>
                              <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                              <td>
                                <textarea id="nullCheckingJexlScriptId" name="nullCheckingJexlScript" rows="20" cols="25" style="width: 500px;">${grouper:escapeHtml(grouperRequestContainer.scriptTesterContainer.nullCheckingJexlScript)}</textarea>
                                <br />
                                <span class="description">${textContainer.text['nullCheckingJexlScriptHint']}</span>
                              </td>
                            </tr>
                          </c:if>
                          
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="scriptSourceId">${textContainer.text['scriptSourceLabel']}</label></strong></td>
                            <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                            <td>
                              <textarea id="scriptSourceId" name="jexlScript" rows="20" cols="25" style="width: 500px;">${grouper:escapeHtml(grouperRequestContainer.scriptTesterContainer.jexlScript)}</textarea>
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              <br />
                              <span class="description">${textContainer.text['scriptSourceHint']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                              <td>
                              </td>
                              <td></td>
                              <td
                                style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                                <input type="submit" class="btn btn-primary"
                                aria-controls="scriptTesterForm" id="submitId"
                                value="${textContainer.text['scriptTestFormSubmitButton'] }"
                                onclick="ajax('../app/UiV2ScriptTester.testScriptSubmit', {formIds: 'scriptTesterForm'}); return false;">
                                &nbsp;
                              </td>
                            </tr>
                            
                            <c:if test="${grouperRequestContainer.scriptTesterContainer.jexlScriptTesterResult != null}">
                              <tr> 
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['scriptResult']}</label></strong></td>
                                <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                                <td>
                                  <c:if test="${grouperRequestContainer.scriptTesterContainer.jexlScriptTesterResult.success == false}">
                                    <div role="alert" class="alert alert-error">
                                      <pre>${grouper:escapeHtml(grouperRequestContainer.scriptTesterContainer.jexlScriptTesterResult.resultForScreen)}</pre>
                                    </div>
                                  </c:if>
                                  <c:if test="${grouperRequestContainer.scriptTesterContainer.jexlScriptTesterResult.success}">
                                    <div>
                                      <pre>${grouper:escapeHtml(grouperRequestContainer.scriptTesterContainer.jexlScriptTesterResult.resultForScreen)}</pre>
                                    </div>
                                  </c:if>
                                </td>
                              </tr>
                              <tr> 
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['scriptExecuted']}</label></strong></td>
                                <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                                <td>
                                 <p class="showHideGshScript"><a href="#" onclick="$('.gshScript').toggle('slow'); return false;">${textContainer.text['showHideGshScript']} <i class="fa fa-angle-down"></i></a></p>
                                 
                                 <code class="gshScript" style="display: none; white-space: pre-wrap;"
                                  >${grouper:escapeHtml(grouperRequestContainer.scriptTesterContainer.jexlScriptTesterResult.gshScriptThatWasExecuted)}</code>
                                </td>
                              </tr>
                            </c:if>
                            
                          
                          
                       
                       </c:if>
                       
                      
                    </tbody>
                  </table>
                  
                </form>
            
          </div>
        </div>
