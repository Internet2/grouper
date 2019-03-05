<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}
                <br /><small>${textContainer.text['stemDeleteTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <p>${textContainer.text['stemDeleteText'] }</p>
                  <form class="form-inline form-small form-filter" id="deleteFolderFormId">
                    <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                    <input type="hidden" name="formSubmitted" value="true" />
                    <table class="table table-condensed table-striped">
                      <tbody>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="stemObliterateId"
                            >${textContainer.text['stemDeleteObliterateLabel']}</label></strong></td>
                          <td>
                            <select name="stemObliterateName" id="stemObliterateNameId" style="width: 30em"
                                onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                              
                              <option value=""></option>
                              <c:choose>
                                <c:when test="${grouperRequestContainer.stemDeleteContainer.emptyStem}">
                                  <option value="deleteStem"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateType == 'deleteStem' ? 'selected="selected"' : '' }
                                      >${textContainer.text['stemObliterateOptionDeleteStem'] }</option>
                                </c:when>
                                <c:otherwise>
                                  <option value="obliterateSome"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateType == 'obliterateSome' ? 'selected="selected"' : '' }
                                      >${textContainer.text['stemObliterateOptionMost'] }</option>
                                  <c:if test="${grouperRequestContainer.stemDeleteContainer.canObliterate}">
                                    <option value="obliterateAll"
                                        ${grouperRequestContainer.stemDeleteContainer.obliterateType == 'obliterateAll' ? 'selected="selected"' : '' }
                                        >${textContainer.text['stemObliterateOptionAll'] }</option>
                                  </c:if>
                                </c:otherwise>
                              </c:choose>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['stemDeleteObliterateHint']}</span>
                          </td>
                        </tr>
                        <c:choose>
                          <c:when  test="${grouperRequestContainer.stemDeleteContainer.obliterateType == 'obliterateSome'}">

                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="obliterateStemScopeOneId"
                                >${textContainer.text['obliterateStemScopeLabel']}</label></strong></td>
                              <td>
                                <select name="obliterateStemScopeOneName" id="obliterateStemScopeOneId" style="width: 30em"
                                    onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                                  
                                  <option value="false"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateStemScopeOne == false ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliterateAllStemScopeLabel'] }</option>
                                  <option value="true"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateStemScopeOne == true ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliterateOneStemScopeLabel'] }</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['obliterateStemScopeDescription']}</span>
                              </td>
                            </tr>
                            
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="stemDeleteEmptyStemsId"
                                >${textContainer.text['obliterateEmptyStemsLabel']}</label></strong></td>
                              <td>
                                <select name="stemDeleteEmptyStemsName" id="stemDeleteEmptyStemsId" style="width: 30em"
                                    onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                                  
                                  <option value="true"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateEmptyStems == true ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliterateEmptyStemsTrue'] }</option>
                                  <option value="false"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateEmptyStems == false ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliterateEmptyStemsFalse'] }</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['obliterateEmptyStemsHint']}</span>
                              </td>
                            </tr>
                            
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="stemDeleteGroupsId"
                                >${textContainer.text['obliterateGroupsLabel']}</label></strong></td>
                              <td>
                                <select name="stemDeleteGroupsName" id="stemDeleteGroupsId" style="width: 30em"
                                    onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                                  
                                  <option value="true"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateGroups == true ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliterateGroupsTrue'] }</option>
                                  <option value="false"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateGroups == false ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliterateGroupsFalse'] }</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['obliterateGroupsHint']}</span>
                              </td>
                            </tr>
                            
                            <c:if test="${grouperRequestContainer.stemDeleteContainer.obliterateGroups == false}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label for="stemDeleteGroupMembershipsId"
                                  >${textContainer.text['obliterateGroupMembershipsLabel']}</label></strong></td>
                                <td>
                                  <select name="stemDeleteGroupMembershipsName" id="stemDeleteGroupMembershipsId" style="width: 30em"
                                      onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                                    
                                    <option value="true"
                                        ${grouperRequestContainer.stemDeleteContainer.obliterateGroupMemberships == true ? 'selected="selected"' : '' }
                                        >${textContainer.text['obliterateGroupMembershipsTrue'] }</option>
                                    <option value="false"
                                        ${grouperRequestContainer.stemDeleteContainer.obliterateGroupMemberships == false ? 'selected="selected"' : '' }
                                        >${textContainer.text['obliterateGroupMembershipsFalse'] }</option>
                                  </select>
                                  <br />
                                  <span class="description">${textContainer.text['obliterateGroupMembershipsHint']}</span>
                                </td>
                              </tr>
                            </c:if>
                            
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="stemDeleteAttributeDefsId"
                                >${textContainer.text['obliterateAttributeDefsLabel']}</label></strong></td>
                              <td>
                                <select name="stemDeleteAttributeDefsName" id="stemDeleteAttributeDefsId" style="width: 30em"
                                    onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                                  
                                  <option value="true"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateAttributeDefs == true ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliterateAttributeDefsTrue'] }</option>
                                  <option value="false"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateAttributeDefs == false ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliterateAttributeDefsFalse'] }</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['obliterateAttributeDefsHint']}</span>
                              </td>
                            </tr>
                            
                            <c:if test="${grouperRequestContainer.stemDeleteContainer.obliterateAttributeDefs == false}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label for="stemDeleteAttributeDefNamesId"
                                  >${textContainer.text['obliterateAttributeDefNamesLabel']}</label></strong></td>
                                <td>
                                  <select name="stemDeleteAttributeDefNamesName" id="stemDeleteAttributeDefNamesId" style="width: 30em"
                                      onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                                    
                                    <option value="true"
                                        ${grouperRequestContainer.stemDeleteContainer.obliterateAttributeDefNames == true ? 'selected="selected"' : '' }
                                        >${textContainer.text['obliterateAttributeDefNamesTrue'] }</option>
                                    <option value="false"
                                        ${grouperRequestContainer.stemDeleteContainer.obliterateAttributeDefNames == false ? 'selected="selected"' : '' }
                                        >${textContainer.text['obliterateAttributeDefNamesFalse'] }</option>
                                  </select>
                                  <br />
                                  <span class="description">${textContainer.text['obliterateAttributeDefNamesHint']}</span>
                                </td>
                              </tr>
                            </c:if>
                            
                          </c:when>
                          <c:when  test="${grouperRequestContainer.stemDeleteContainer.obliterateType == 'obliterateAll' && grouperRequestContainer.stemDeleteContainer.grouperAdmin}">
                            
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="stemDeletePointInTimeId"
                                >${textContainer.text['obliteratePointInTimeLabel']}</label></strong></td>
                              <td>
                                <select name="stemDeletePointInTimeName" id="stemDeletePointInTimeId" style="width: 30em"
                                    onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                                  
                                  <option value=""></option>
                                  <option value="true"
                                      ${grouperRequestContainer.stemDeleteContainer.obliteratePointInTime == true ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliteratePointInTimeTrue'] }</option>
                                  <option value="false"
                                      ${grouperRequestContainer.stemDeleteContainer.obliteratePointInTime == false ? 'selected="selected"' : '' }
                                      >${textContainer.text['obliteratePointInTimeFalse'] }</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['obliteratePointInTimeHint']}</span>
                              </td>
                            </tr>

                          </c:when>
                        </c:choose>
                        
                        <c:if test="${grouperRequestContainer.stemDeleteContainer.obliterateGroups 
                            || grouperRequestContainer.stemDeleteContainer.obliterateEmptyStems
                            || grouperRequestContainer.stemDeleteContainer.obliterateAttributeDefs
                            || grouperRequestContainer.stemDeleteContainer.obliterateAttributeDefNames
                            || grouperRequestContainer.stemDeleteContainer.obliterateGroupMemberships
                            || grouperRequestContainer.stemDeleteContainer.obliteratePointInTime != null
                            || grouperRequestContainer.stemDeleteContainer.obliterateType == 'deleteStem'
                            || (grouperRequestContainer.stemDeleteContainer.obliterateType == 'obliterateAll' && !grouperRequestContainer.stemDeleteContainer.grouperAdmin)}">
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="stemDeleteAreYouSureId"
                              >${textContainer.text['stemDeleteAreYouSureLabel']}</label></strong></td>
                            <td>
                              <select name="stemDeleteAreYouSureName" id="stemDeleteAreYouSureId" style="width: 30em"
                                  onchange="ajax('../app/UiV2Stem.stemDelete', {formIds: 'deleteFolderFormId'}); return false;">
                                
                                <option value=""></option>
                                <option value="true"
                                    ${grouperRequestContainer.stemDeleteContainer.areYouSure == true ? 'selected="selected"' : '' }
                                    >${textContainer.text['stemDeleteAreYouSureTrue'] }</option>
                                <option value="false"
                                    ${grouperRequestContainer.stemDeleteContainer.areYouSure == false ? 'selected="selected"' : '' }
                                    >${textContainer.text['stemDeleteAreYouSureFalse'] }</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['stemDeleteAreYouSureHint']}</span>
                            </td>
                          </tr>
                        </c:if>                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"></td>
                          <td>
                            <c:if test="${grouperRequestContainer.stemDeleteContainer.areYouSure == true}">
                                <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Stem.stemDeleteSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'deleteFolderFormId'}); return false;">${textContainer.text['stemDeleteButton'] }</a>
                                &nbsp;
                            </c:if>
                            
                            <a href="#" class="btn btn-cancel" role="button" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" >${textContainer.text['stemCancelButton'] }</a></div>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </form>
                  <%--
                 --%>
              </div>
            </div>
