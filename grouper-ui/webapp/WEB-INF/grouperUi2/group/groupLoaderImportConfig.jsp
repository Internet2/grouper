<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../group/groupTabs.jsp" %>
                <div class="row-fluid">
                  <div class="lead span10">${textContainer.text['grouperLoaderDiagnosticsHeader'] }</div>
                  <div class="span2" id="grouperLoaderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperLoaderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <form id="importLoaderConfigFormId" class="form-horizontal" method="post" 
                  action="UiV2GrouperLoader.importLoaderConfigSubmit" enctype="multipart/form-data">
                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="importLoaderConfigFormatId">${textContainer.text['grouperLoaderImportConfigChooseFormat']}</label></strong></td>
                        <td>
                          <select name="importLoaderConfigFormat" id="importLoaderConfigFormatId" style="width: 25em"
                            onchange="ajax('../app/UiV2GrouperLoader.importLoaderConfig', {formIds: 'importLoaderConfigFormId'}); return false;">
                            
                            <option value=""></option>
                            <option value="file" ${grouperRequestContainer.grouperLoaderContainer.importLoaderConfigFormat == 'file' ? 'selected="selected"' : '' } 
                              >${textContainer.textEscapeXml['grouperLoaderImportConfigFormatFile']}</option>
                            <option value="copyPaste" ${grouperRequestContainer.grouperLoaderContainer.importLoaderConfigFormat == 'copyPaste' ? 'selected="selected"' : '' }
                              >${textContainer.textEscapeXml['grouperLoaderImportConfigFormatCopyPaste']}</option>
                            
                          </select>
                          <br />
                          <span class="description">${textContainer.text['grouperLoaderHasLoaderDescription']}</span>
                        </td>
                      </tr>
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.importLoaderConfigFormat == 'file'}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="importConfigFileId">${textContainer.text['grouperLoaderImportConfigFile']}</label></strong></td>
                          <td>
                            <span style="white-space: nowrap">
                              <input type="file" name="importConfigFile" id="importConfigFileId" />
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            </span>
                            <br />
                            <span class="description">${textContainer.text['grouperLoaderImportConfigFile']}</span>
                          </td>
                        </tr>
                      </c:if>
                      
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.importLoaderConfigFormat == 'copyPaste'}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderTypeId">${textContainer.text['grouperLoaderImportConfigJsonContents']}</label></strong></td>
                          <td>
                            <span style="white-space: nowrap">
                              
                              <textarea id="loaderConfigContentsId" name="loaderConfigContents" rows="3" cols="40" class="input-block-level"></textarea>
                              
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            </span>
                            <br />
                            <span class="description">${textContainer.text['grouperLoaderImportConfigJsonContents']}</span>
                          </td>
                        </tr>
                      </c:if>
                      
                      <c:if test="${not empty grouperRequestContainer.grouperLoaderContainer.importLoaderConfigFormat}">
                      <tr>
                      <td style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                      
                      <button type="button" class="btn btn-primary" onclick="return guiSubmitFileForm(event, '#importLoaderConfigFormId', '../app/UiV2GrouperLoader.importLoaderConfigSubmit');">${textContainer.text['grouperLoaderEditButtonSave'] }</button>
                      
                      
                      <%-- <input type="submit" class="btn btn-primary" aria-controls="groupFilterResultsId" id="filterSubmitId" 
                              value="${textContainer.text['grouperLoaderEditButtonSave'] }" 
                              onclick="ajax('../app/UiV2GrouperLoader.importLoaderConfigSubmit', {formIds: 'importLoaderConfigFormId'}); return false;"> --%> 
                      </td>
                      <td></td>
                      </tr>
                      </c:if>
                        
                      </tbody>
                     </table>
                  </form>

              </div>
            </div>
