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
                                  <option value="obliterateAll"
                                      ${grouperRequestContainer.stemDeleteContainer.obliterateType == 'obliterateAll' ? 'selected="selected"' : '' }
                                      >${textContainer.text['stemObliterateOptionAll'] }</option>
                                </c:otherwise>
                              </c:choose>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['stemDeleteObliterateHint']}</span>
                          </td>
                        </tr>
                        <c:choose>
                          <c:when  test="${grouperRequestContainer.stemDeleteContainer.obliterateAll == false}">
                            
                            
                            
                          </c:when>
                          <c:when  test="${grouperRequestContainer.stemDeleteContainer.obliterateAll == true}">
                            
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
                        
                        <c:if test="${grouperRequestContainer.stemDeleteContainer.obliteratePointInTime != null
                                ||  grouperRequestContainer.stemDeleteContainer.obliterateType == 'deleteStem'}">
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
                      </tbody>
                    </table>
                    <c:if test="${grouperRequestContainer.stemDeleteContainer.areYouSure != null}">     
                      <c:choose>
                        <c:when test="${grouperRequestContainer.stemDeleteContainer.areYouSure == true}">
                          <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Stem.stemDeleteSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">${textContainer.text['stemDeleteButton'] }</a> 
                        
                        </c:when>
                        <c:when test="${grouperRequestContainer.stemDeleteContainer.areYouSure == false}">
                          <a href="#" class="btn btn-cancel" role="button" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" >${textContainer.text['stemCancelButton'] }</a></div>
                        </c:when>
                      </c:choose>
                    </c:if>
                  </form>
                  <%--
                 --%>
              </div>
            </div>
