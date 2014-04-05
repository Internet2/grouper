<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">Applications</li>
              </ul>
              --%>
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myGroupsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['groupImportMembersBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['groupImportTitle'] }</h1>
              </div>
            </div>

            <div id="member-search" tabindex="-1" role="dialog" aria-labelledby="member-search-label" aria-hidden="true" class="modal hide fade">
              <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                <h3 id="member-search-label">${textContainer.text['groupSearchForEntityButton'] }</h3>
              </div>
              <div class="modal-body">
                <form class="form form-inline" id="addMemberSearchFormId">
                  <input name="addMemberSubjectSearch" type="text" placeholder=""/>
                  <button class="btn" onclick="ajax('../app/UiV2Group.addMemberSearch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'addMemberSearchFormId'}); return false;" >${textContainer.text['groupSearchButton'] }</button>
                  <br />
                  <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['groupLabelExactIdMatch'] }</span>
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


            <div id="group-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
              <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                <h3 id="group-search-label">${textContainer.text['groupImportSearchForGroupButton']}</h3>
              </div>

              <div class="modal-body">
                <form class="form form-inline" id="addGroupSearchFormId">
                  <input id="addGroupSearchId" name="addGroupSearch" type="text" placeholder="${textContainer.text['groupImportSearchGroupPlaceholder']}" />
                  <button class="btn" onclick="ajax('../app/UiV2GroupImport.groupImportGroupSearch', {formIds: 'addGroupSearchFormId'}); return false;" >${textContainer.text['groupImportSearchButton'] }</button>
                  <br />
                  <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['subjectSearchExactIdMatch'] }</span>
                </form>
                <div id="addGroupResults">
                </div>
              </div>
              <div class="modal-footer">
                <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['subjectSearchCloseButton']}</button>
              </div>
            </div>

            <form class="form-horizontal" id="importGroupFormId" enctype="multipart/form-data" method="post" >
              <div class="bulk-add-group-input-container">
                <div class="control-group bulk-add-group-block">
                  <label for="add-entities" style="position:absolute" class="control-label">${textContainer.text['groupImportAddMembersToGroupLabel'] }</label>
                  <div class="controls">
                    <grouper:combobox2 idBase="groupImportGroupCombo" style="width: 30em" 
                       value="${grouperRequestContainer.groupContainer.guiGroup.group.id}"
                       filterOperation="../app/UiV2Group.groupUpdateFilter" />                    
                    ${textContainer.text['groupImportGroupLabelPreComboLink']} <a href="#group-search" 
                      onclick="$('#addGroupSearchId').val(''); $('#addGroupResults').empty(); return true;"
                      role="button" data-toggle="modal" style="text-decoration: underline !important;"
                      >${textContainer.text['groupImportGroupSearchLink']}</a>
                  </div>
                  <br />
                  <div class="control-group" style="margin-bottom: 5px">
                    <div class="controls"><a href="#" 
                      onclick="ajax('../app/UiV2GroupImport.groupImportAddGroup', {formIds: 'importGroupFormId'}); return false;"
                      class="btn bulk-add-another-group">${textContainer.text['groupImportAddAnotherGroupButton']}</a></div>
                  </div>

                  <div class="controls">
                    <div id="groupImportExtraGroupsDivId">
                      <%@ include file="groupImportExtraGroups.jsp"%>
                    </div>
                  </div>
                </div>
              </div>
              
              <hr>
              <div class="control-group">
                <label class="control-label">${textContainer.text['groupImportHowAdd'] }</label>
                <div class="controls">
                  <label class="radio">
                    <input type="radio" name="bulkAddOptions" value="input" checked="checked" 
                    onchange="$('.bulk-add-import-container').slideUp('fast'); $('.bulk-add-list-container').slideUp('fast'); $('.bulk-add-input-container').slideDown('fast'); return true;"
                    >${textContainer.text['groupImportSearchForMembersToAdd'] }
                  </label>
                  <label class="radio">
                    <input type="radio" name="bulkAddOptions" value="list"
                      onchange="$('.bulk-add-import-container').slideUp('fast'); $('.bulk-add-list-container').slideDown('fast'); $('.bulk-add-input-container').slideUp('fast'); return true;"
                    >${textContainer.text['groupImportCopyListOfIds'] }
                  </label>
                  <label class="radio">
                    <input type="radio" name="bulkAddOptions" value="import"
                      onchange="$('.bulk-add-import-container').slideDown('fast'); $('.bulk-add-list-container').slideUp('fast'); $('.bulk-add-input-container').slideUp('fast'); return true;"
                    >${textContainer.text['groupImportImportFile'] }
                  </label>
                </div>
              </div>

              <div class="bulk-add-input-container">
                <div class="control-group bulk-add-block">
                  <label for="add-entities" class="control-label">${textContainer.text['groupImportEnterMemberNameOrId'] }</label>
                  <div class="controls">
                    <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 30em" 
                       value="${grouperRequestContainer.groupImportContainer.importDefaultSubject}"
                       filterOperation="../app/UiV2Group.addMemberFilter" />
                                           
                    ${textContainer.text['groupImportSubjectLabelPreComboLink']} <a href="#member-search" 
                      onclick="$('#addMemberSearchId').val(''); $('#addMemberResults').empty(); return true;"
                      role="button" data-toggle="modal" style="text-decoration: underline !important;"
                      >${textContainer.text['groupImportSubjectSearchLink']}</a>
                  </div>
                  <br />
                  <div class="control-group" style="margin-bottom: 5px">
                    <div class="controls">
                      <a href="#" 
                        onclick="ajax('../app/UiV2GroupImport.groupImportAddMember', {formIds: 'importGroupFormId'}); return false;"
                        class="btn bulk-add-another">${textContainer.text['groupImportAddAnotherMemberButton']}</a></div>
                  </div>
                  <div class="controls">
                  
                    <div id="groupImportExtraMembersDivId">
                      <%@ include file="groupImportExtraSubjects.jsp"%>
                    </div>
                    
                  </div>
                </div>
              </div>
              
              <div class="bulk-add-import-container hide">
                <div class="control-group">
                  <label class="control-label">${textContainer.text['groupImportSelectFileToImport'] }</label>
                  <div class="controls">
                    <input type="file" name="importCsvFile" id="importCsvFileId"><span class="help-block">
                    
                    ${textContainer.text['groupImportSelectFileDescription']}
                    </span>                    
                    <br><br>
                      <table class="table table-condensed table-striped table-bordered">
                        <thead>
                          <tr>
                            <th>${textContainer.text['groupImportSourceId'] }</th>
                            <th>${textContainer.text['groupImportSourceName'] }</th>
                          </tr>
                        </thead>
                        <tbody>
                          <%-- <tr>
                            <td>g:gsa</td>
                            <td>Grouper: Group Source Adapter</td>
                          </tr> --%>
                          <c:forEach items="${grouperRequestContainer.commonRequestContainer.sources}" 
                            var="source" >
                            <tr>
                              <td>${grouper:escapeHtml(source.id) }</td>
                              <td>${grouper:escapeHtml(source.name) }</td>
                            </tr>
                          
                          </c:forEach>
                          
                        </tbody>
                      </table>
                  </div>
                </div>
              </div>

              <div class="bulk-add-list-container hide">
                <div class="control-group">
                  <label for="add-entities" class="control-label">${textContainer.text['groupImportEnterListOfMemberIds'] }</label>
                  <div class="controls">
                    <textarea rows="10" name="entityList" id="entityListId"></textarea>
                    <br /><br />
                    <a href="#" 
                      onclick="ajax('../app/UiV2GroupImport.groupImportValidateList', {formIds: 'importGroupFormId'}); return false;"
                      class="btn bulk-add-another">${textContainer.text['groupImportValidateButton']}</a>
                  </div>
                </div>
                <div class="control-group">
                  <label for="searchEntitiesSource" class="control-label">${textContainer.text['find.search-source'] }</label>
                  <div class="controls">
                    <select name="searchEntitySourceName" id="searchEntitySourceId">
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
                    </select>
                  </div>
                </div>
              </div>
              
              <div class="control-group">
                <div class="controls">
                  <label class="checkbox">
                    <input type="checkbox" name="replaceExistingMembers" value="true">${textContainer.text['groupImportReplaceExistingMembers'] }
                  </label>
                </div>
              </div>
              <div class="form-actions">
                <a href="#" 
                  onclick="return guiSubmitFileForm(event, '#importGroupFormId', '../app/UiV2GroupImport.groupImportSubmit')"
                  class="btn btn-primary">${textContainer.text['groupImportAddMembersButton'] }</a> 
                <%-- needs to go back to the calling page which is set in a param --%>
                <c:choose>
                  <c:when test="${grouperRequestContainer.groupImportContainer.importFromSubject}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouper:escapeUrl(grouperRequestContainer.subjectContainer.guiSubject.subject.id)}&sourceId=${grouper:escapeUrl(grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId)}');"
                       class="btn btn-cancel">${textContainer.text['groupImportCancelButton']}</a>
                  </c:when>
                  <c:when test="${grouperRequestContainer.groupImportContainer.importFromGroup}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');"
                       class="btn btn-cancel">${textContainer.text['groupImportCancelButton']}</a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');"
                       class="btn btn-cancel">${textContainer.text['groupImportCancelButton']}</a>
                  </c:otherwise>
                </c:choose>
              </div>
              
              <%-- helps with navigation --%>
              <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
              <input type="hidden" name="subjectId" value="${grouper:escapeHtml(grouperRequestContainer.subjectContainer.guiSubject.subject.id)}" />
              <input type="hidden" name="sourceId" value="${grouper:escapeHtml(grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId)}" />

            </form>


