<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">

              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="span10">
                    <h1><i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}</h1>
                    <div id="member-search" tabindex="-1" role="dialog" aria-labelledby="member-search-label" aria-hidden="true" class="modal hide fade">
                      <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                        <h3 id="member-search-label">${textContainer.text['stemSearchForEntityButton'] }</h3>
                      </div>
                      <div class="modal-body">
                        <form class="form form-inline" id="addMemberSearchFormId">
                          <input name="addMemberSubjectSearch" type="text" placeholder=""/>
                          <button class="btn" onclick="ajax('../app/UiV2Stem.addMemberSearch?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'addMemberSearchFormId'}); return false;" >${textContainer.text['groupSearchButton'] }</button>
                          <br />
                          <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['stemLabelExactIdMatch'] }</span>
                          <br />
                          <span style="white-space: nowrap;">${textContainer.text['find.search-source'] } 
                          <select name="sourceId">
                            <option value="all">${textContainer.textEscapeXml['find.search-all-sources'] }</option>
                            <c:forEach items="${grouperRequestContainer.subjectContainer.sources}" var="source" >
                              <option value="${grouper:escapeHtml(source.id)}">
                                ${grouper:escapeHtml(source.name) } (
                                  <c:forEach var="subjectType" items="${source.subjectTypes}" varStatus="typeStatus">
                                    <c:if test="${typeStatus.count>1}">, </c:if>
                                    ${grouper:escapeHtml(subjectType)}
                                  </c:forEach>
                                )                               
                              </option>
                            </c:forEach>
                          </select></span>
                        </form>
                        <div id="addMemberResults">
                        </div>
                      </div>
                      <div class="modal-footer">
                        <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['groupSearchCloseButton']}</button>
                      </div>
                    </div>
                    <div id="add-block-container" class="well hide">
                      <div id="add-members">
                        <form id="add-members-form" target="#" class="form-horizontal form-highlight">
                          <div class="control-group">
                            <label for="add-block-input" class="control-label">${textContainer.text['stemSearchMemberOrId'] }</label>
                            <div class="controls">
                              <div id="add-members-container">

                                <%-- placeholder: Enter the name of a person, group, or other entity --%>
                                <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 30em"
                                  filterOperation="../app/UiV2Stem.addMemberFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}"/>
                                ${textContainer.text['stemSearchLabelPreComboLink']} <a href="#member-search" onclick="$('#addMemberResults').empty();" role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['stemSearchForEntityLink']}</a>
                                
                              </div>
                            </div>
                          </div>
                          <div id="add-members-privileges-select" class="control-group">
                            <label class="control-label">${textContainer.text['stemViewAssignThesePrivileges']}</label>
                            <div class="controls">
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_stemmers" value="true" />${textContainer.text['priv.stemmersUpper']}
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_creators" value="true" />${textContainer.text['priv.creatorsUpper']}
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_stemAttrReaders" value="true" />${textContainer.text['priv.stemAttrReadersUpper']}
                              </label>
                              <label class="checkbox inline" id="stemPrivsErrorId">
                                <input type="checkbox" name="privileges_stemAttrUpdaters" value="true" />${textContainer.text['priv.stemAttrUpdatersUpper']}
                              </label>
                            </div>
                          </div>
                          <div class="control-group">
                            <div class="controls">
                              <button onclick="ajax('../app/UiV2Stem.addMemberSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'add-members-form', formIdsOptional: 'stemPagingPrivilegesFormId,stemFilterPrivilegesFormId'}); return false;" 
                                id="add-members-submit" type="submit" class="btn btn-primary">${textContainer.text['groupViewAddMemberLink']}</button>
                            </div>
                          </div>
                        </form>
                      </div>
                    </div>
                    
                    <p>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.description)}</p>
                    <div id="stemDetailsId" style="display: none;">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.displayName)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelIdPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.name)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelAlternateIdPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.alternateName)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelId'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.extension)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelCreated'] }</strong></td>
                            <td>${grouper:formatDateLong(grouperRequestContainer.stemContainer.guiStem.stem.createTimeLong)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelCreator'] }</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.stemContainer.guiStem.stem.creatorUuid)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelLastEdited'] }</strong></td>
                            <td>${grouper:formatDateLong(grouperRequestContainer.stemContainer.guiStem.stem.modifyTimeLong)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelLastEditor'] }</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.stemContainer.guiStem.stem.modifierUuid)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelIdIndex']}</strong></td>
                            <td>${grouperRequestContainer.stemContainer.guiStem.stem.idIndex}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelUuid'] }</strong></td>
                            <td>${grouperRequestContainer.stemContainer.guiStem.stem.uuid}</td>
                          </tr>
                          
                          
                        </tbody>
                      </table>
                    </div>
                    <p id="stemDetailsMoreId"><a href="#" onclick="$('#stemDetailsId').show('slow'); $('#stemDetailsMoreId').hide(); $('#stemDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="fa fa-angle-down"></i></a></p>
                    <p id="stemDetailsLessId" style="display: none"><a href="#" onclick="$('#stemDetailsId').hide('slow'); $('#stemDetailsLessId').hide(); $('#stemDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="fa fa-angle-up"></i></a></p>
                     
                  </div>
                  <div class="span2" id="stemMoreActionsButtonContentsDivId">
                    <%@ include file="stemMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
