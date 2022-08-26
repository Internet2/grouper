<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <!-- start group/viewGroup.jsp -->
            <div class="bread-header-container">

              ${grouperRequestContainer.subjectContainer.guiSubject.breadcrumbs}
              
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="span9">
                    <h1>
                      ${grouperRequestContainer.subjectContainer.guiSubject.screenSubjectIcon2Html}
                      ${grouperRequestContainer.subjectContainer.guiSubject.screenLabelShort2noLink}</h1>

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
                    <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId == 'grouperEntities' && grouperRequestContainer.groupContainer.canAdmin}">
                    <div id="add-block-container" class="well hide">
                      <div id="add-members">
                        <form id="add-members-form" target="#" class="form-horizontal form-highlight">
                          <div class="control-group" id="add-member-control-group" aria-live="polite" aria-expanded="false">
                            <label for="entityAddMemberComboId" class="control-label">${textContainer.text['groupSearchMemberOrId'] }</label>
                            <div class="controls">
                              <div id="add-members-container">

                                <%-- placeholder: Enter the name of a person, group, or other entity --%>
                                <grouper:combobox2 idBase="entityAddMemberCombo" style="width: 30em"
                                  filterOperation="../app/UiV2Group.addMemberFilter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}"/>
                                ${textContainer.text['groupSearchLabelPreComboLink']} <a href="#member-search" onclick="$('#addMemberResults').empty();" role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['groupSearchForEntityLink']}</a>
                                
                              </div>
                            </div>
                          </div>

                          <div id="add-members-privileges-select" class="control-group"  ${(defaultMemberUnchecked || !grouperRequestContainer.groupContainer.canAdmin )? 'style="display:none"' : '' } >
                            <label class="control-label">${textContainer.text['groupViewAssignThesePrivileges']}</label>
                            <div class="controls" >
                              <label class="radio inline" >
                                <input type="radio" id="priv1" value="default" name="privilege-options" checked="checked" onclick="this.blur();" value="true" onchange="hideCustomPrivilege('add-members-privileges')"/>${textContainer.text['groupViewDefaultPrivileges'] }
                              </label>
                              <label class="radio inline">
                                <input type="radio" id="priv2" value="custom" name="privilege-options" onclick="this.blur();" value="true" onchange="showCustomPrivilege('add-members-privileges')"/>${textContainer.text['groupViewCustomPrivileges'] }
                              </label>
                            </div>
                          </div>
                          <div id="add-members-privileges" class="control-group hide" aria-live="polite" aria-expanded="false">
                            <label class="control-label" ${defaultMemberUnchecked ? '' : 'style="display:none"' } >${textContainer.text['groupViewAssignThesePrivileges']}</label>
                            <div class="controls">
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_admins" value="true" />${textContainer.text['priv.adminUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_viewers" value="true" />${textContainer.text['priv.viewUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_groupAttrReaders" value="true" />${textContainer.text['priv.groupAttrReadUpper'] }
                              </label>
                              <label class="checkbox inline" id="groupPrivsErrorId">
                                <input type="checkbox" name="privileges_groupAttrUpdaters" value="true" />${textContainer.text['priv.groupAttrUpdateUpper'] }
                              </label>
                            </div>
                          </div>
                          
                          <div class="control-group">
                            <div class="controls">
                              <button onclick="ajax('../app/UiV2LocalEntity.addMemberSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'add-members-form', formIdsOptional: 'groupRefreshPartFormId, groupFilterFormId,groupPagingFormId,groupPagingPrivilegesFormId,groupFilterPrivilegesFormId,groupPagingAuditForm, groupFilterAuditFormId, groupQuerySortAscendingFormId'}); return false;" 
                                id="add-members-submit" type="submit" class="btn btn-primary">${textContainer.text['groupViewAddMemberLink']}</button>
                            </div>
                          </div>
                        </form>
                      </div>
                      
                    </div>
                    </c:if>

                    <div id="group-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                      <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                        <h3 id="group-search-label">${textContainer.text['subjectSearchForGroupButton']}</h3>
                      </div>

                      <div class="modal-body">
                        <form class="form form-inline" id="addGroupSearchFormId">
                          <input id="addGroupSubjectSearchId" name="addGroupSubjectSearch" type="text" placeholder="${textContainer.text['subjectSearchGroupPlaceholder']}" />
                          <button class="btn" onclick="ajax('../app/UiV2Subject.addGroupSearch?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'addGroupSearchFormId'}); return false;" >${textContainer.text['subjectSearchButton'] }</button>
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

                    <div id="add-block-container" class="well gradient-background hide">
                      <div id="add-groups">
                        <form id="add-groups-form" target="#" class="form-horizontal form-highlight">
                          <div class="control-group">
                            <label for="add-block-input" class="control-label">${textContainer.text['subjectSearchGroupName'] }</label>
                            <div class="controls">
                              <div id="add-members-container">

                                <%-- placeholder: Enter the name of a group --%>
                                <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 30em"
                                  filterOperation="../app/UiV2Subject.addToGroupFilter?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}"/>
                                <%--a href="#member-search" onclick="$('#addMemberResults').empty();" role="button" data-toggle="modal" class="btn"><i class="fa fa-search"></i></a --%>
                                ${textContainer.text['subjectSearchLabelPreComboLink']} <a href="#group-search" onclick="$('#addGroupResults').empty(); $('#addGroupSubjectSearchId').val('');" role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['subjectSearchForGroupLink']}</a>
                                
                              </div>
                            </div>
                          </div>
                          <div id="add-members-privileges-select" class="control-group">
                            <label class="control-label">${textContainer.text['subjectViewAssignThesePrivileges']}</label>
                            <div class="controls">
                              <label class="radio inline">
                                <input type="radio" id="priv1" value="default" name="privilege-options" checked="checked" onclick="this.blur();" value="true" onchange="$('#add-members-privileges').hide('slow');"/>${textContainer.text['subjectViewDefaultPrivileges'] }
                              </label>
                              <label class="radio inline">
                                <input type="radio" id="priv2" value="custom" name="privilege-options" onclick="this.blur();" value="true" onchange="$('#add-members-privileges').show('slow');"/>${textContainer.text['subjectViewCustomPrivileges'] }
                              </label>
                            </div>
                          </div>
                          <div id="add-members-privileges" class="control-group hide">
                            <div class="controls">
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_members" value="true" checked="checked"/>${textContainer.text['priv.memberUpper']}
                              </label>
                              <label class="checkbox inline">
                                <%--
                                <input type="checkbox" name="privileges_admins" value="true" 
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllAdmin ? 'checked="checked"' : '' } />ADMIN
                                --%>
                                <input type="checkbox" name="privileges_admins" value="true" />${textContainer.text['priv.adminUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_updaters" value="true" />${textContainer.text['priv.updateUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_readers" value="true" />${textContainer.text['priv.readUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_viewers" value="true" />${textContainer.text['priv.viewUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_optins" value="true" />${textContainer.text['priv.optinUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_optouts" value="true" />${textContainer.text['priv.optoutUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_groupAttrReaders" value="true" />${textContainer.text['priv.groupAttrReadUpper'] }
                              </label>
                              <label class="checkbox inline" id="groupPrivsErrorId">
                                <input type="checkbox" name="privileges_groupAttrUpdaters" value="true" />${textContainer.text['priv.groupAttrUpdateUpper'] }
                              </label>
                            </div>
                          </div>

                          <c:if test="${!defaultMemberUnchecked}">
                            <div class="control-group">
                              <label for="member-start-date"
                                class="control-label">${textContainer.text['subjectViewStartDate'] }</label>
                              <div class="controls">
                                <input type="text" name="startDate"  placeholder="${textContainer.text['membershipEditDatePlaceholder'] }" id="member-start-date"><span class="help-block">${textContainer.text['subjectViewStartDateSubtext'] }</span>
                              </div>
                            </div>
                            <div class="control-group">
                              <label for="member-end-date" class="control-label">${textContainer.text['subjectViewEndDate'] }</label>
                              <div class="controls">
                                <input type="text" name="endDate" placeholder="${textContainer.text['membershipEditDatePlaceholder'] }" id="member-end-date"><span class="help-block">${textContainer.text['subjectViewEndDateSubtext'] }</span>
                              </div>
                            </div>
                          </c:if>

                          <div class="control-group">
                            <div class="controls">
                              <button onclick="ajax('../app/UiV2Subject.addGroupSubmit?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'add-groups-form', formIdsOptional: 'groupFilterFormId,groupPagingFormId,groupPrivilegeFormId,groupPagingPrivilegesFormId,subjectRefreshPartFormId'}); return false;" 
                                id="add-members-submit" type="submit" class="btn btn-primary">${textContainer.text['subjectViewAddGroupLink']}</button> ${textContainer.text['subjectViewTextBetweenAddAndBulk']} <a href="#" onclick="return guiV2link('operation=UiV2GroupImport.groupImport&subjectId=${grouper:escapeUrl(grouperRequestContainer.subjectContainer.guiSubject.subject.id)}&sourceId=${grouper:escapeUrl(grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId)}&backTo=subject'); return false;" class="blue-link">${textContainer.text['subjectViewBulkLink'] }</a> ${textContainer.text['subjectViewTextPostBulkLink'] }
                            </div>
                          </div>
                        </form>
                      </div>
                    </div>


                    <div id="stem-search" tabindex="-1" role="dialog" aria-labelledby="stem-search-label" aria-hidden="true" class="modal hide fade">
                      <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                        <h3 id="stem-search-label">${textContainer.text['subjectSearchForStemButton']}</h3>
                      </div>

                      <div class="modal-body">
                        <form class="form form-inline" id="stemSearchFormId">
                          <input id="addStemSubjectSearchId" name="stemSearch" type="text" placeholder="${textContainer.text['subjectSearchStemPlaceholder']}" />
                          <button class="btn" onclick="ajax('../app/UiV2Subject.addStemSearch?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'stemSearchFormId'}); return false;" >${textContainer.text['subjectSearchButton'] }</button>
                        </form>
                        <div id="folderSearchResultsId">
                        </div>
                      </div>
                      <div class="modal-footer">
                        <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['subjectSearchCloseButton']}</button>
                      </div>
                    </div>

                    <div id="add-block-stem-container" class="well gradient-background hide">
                      <div id="add-stems">
                        <form id="add-stems-form" target="#" class="form-horizontal form-highlight">
                          <div class="control-group">
                            <label for="add-block-stem-input" class="control-label">${textContainer.text['subjectSearchStemName'] }</label>
                            <div class="controls">
                              <div id="add-stems-container">

                                <%-- placeholder: Enter the name of a group --%>
                                <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em"
                                  filterOperation="../app/UiV2Subject.addToStemFilter?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}"/>
                                ${textContainer.text['subjectSearchStemLabelPreComboLink']} <a href="#stem-search" onclick="$('#addStemResults').empty(); $('#addStemSubjectSearchId').val('');" role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['subjectSearchForStemLink']}</a>
                                
                              </div>
                            </div>
                          </div>
                          <div id="add-members-stem-privileges-select" class="control-group">
                            <label class="control-label">${textContainer.text['subjectViewAssignThesePrivileges']}</label>
                            <div class="controls">
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_stemAdmins" value="true" />${textContainer.text['priv.stemAdminsUpper']}
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_creators" value="true" />${textContainer.text['priv.creatorsUpper']}
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_stemAttrReaders" value="true" />${textContainer.text['priv.stemAttrReadersUpper']}
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_stemAttrUpdaters" value="true" />${textContainer.text['priv.stemAttrUpdatersUpper']}
                              </label>
                              <label class="checkbox inline" id="stemPrivsErrorId">
                                <input type="checkbox" name="privileges_stemViewers" value="true" />${textContainer.text['priv.stemViewersUpper']}
                              </label>
                            </div>
                          </div>
                          <div class="control-group">
                            <div class="controls">
                              <button onclick="ajax('../app/UiV2Subject.addStemSubmit?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'add-stems-form,groupPrivilegeFormId,groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;" 
                                id="add-stem-members-submit" type="submit" class="btn btn-primary">${textContainer.text['subjectViewAddStemLink']}</button>
                            </div>
                          </div>
                        </form>
                      </div>
                    </div>

                    <div id="attributeDef-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                      <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                        <h3 id="attributeDef-search-label">${textContainer.text['subjectSearchForAttributeDefButton']}</h3>
                      </div>

                      <div class="modal-body">
                        <form class="form form-inline" id="addAttributeDefSearchFormId">
                          <input id="addAttributeDefSubjectSearchId" name="addAttributeDefSubjectSearch" type="text" placeholder="${textContainer.text['subjectSearchAttributeDefPlaceholder']}" />
                          <button class="btn" onclick="ajax('../app/UiV2Subject.addAttributeDefSearch?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'addAttributeDefSearchFormId'}); return false;" >${textContainer.text['subjectSearchButton'] }</button>
                        </form>
                        <div id="addAttributeDefResults">
                        </div>
                      </div>
                      <div class="modal-footer">
                        <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['subjectSearchCloseButton']}</button>
                      </div>
                    </div>

                    <div id="add-block-attributeDef-container" class="well gradient-background hide">
                      <div id="add-attributeDefs">
                        <form id="add-attributeDefs-form" target="#" class="form-horizontal form-highlight">
                          <div class="control-group">
                            <label for="add-block-input" class="control-label">${textContainer.text['subjectSearchAttributeDefName'] }</label>
                            <div class="controls">
                              <div id="add-attributeDefs-container">

                                <%-- placeholder: Enter the name of a attributeDef --%>
                                <grouper:combobox2 idBase="attributeDefAddMemberCombo" style="width: 30em"
                                  filterOperation="../app/UiV2Subject.addToAttributeDefFilter?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}"/>
                                ${textContainer.text['subjectSearchAttributeDefLabelPreComboLink']} <a href="#attributeDef-search" onclick="$('#addAttributeDefResults').empty(); $('#addAttributeDefSubjectSearchId').val('');" role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['subjectSearchForAttributeDefLink']}</a>
                                
                              </div>
                            </div>
                          </div>
                          
                          <div id="add-members-attributeDef-privileges-select" class="control-group">
                            <label class="control-label">${textContainer.text['subjectViewAssignThesePrivileges']}</label>
                            <div class="controls">
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_attrAdmins" value="true" />${textContainer.text['priv.attrAdminUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_attrUpdaters" value="true" />${textContainer.text['priv.attrUpdateUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_attrReaders" value="true" />${textContainer.text['priv.attrReadUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_attrViewers" value="true" />${textContainer.text['priv.attrViewUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_attrOptins" value="true" />${textContainer.text['priv.attrOptinUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_attrOptouts" value="true" />${textContainer.text['priv.attrOptoutUpper'] }
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_attrDefAttrReaders" value="true" />${textContainer.text['priv.attrDefAttrReadUpper'] }
                              </label>
                              <label class="checkbox inline"  id="attributeDefPrivsErrorId">
                                <input type="checkbox" name="privileges_attrDefAttrUpdaters" value="true" />${textContainer.text['priv.attrDefAttrUpdateUpper'] }
                              </label>
                            </div>
                          </div>
                          <div class="control-group">
                            <div class="controls">
                              <button onclick="ajax('../app/UiV2Subject.addAttributeDefSubmit?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'add-attributeDefs-form,groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;" 
                                id="add-attributeDef-members-submit" type="submit" class="btn btn-primary">${textContainer.text['subjectViewAddAttributeDefLink']}</button>
                            </div>
                          </div>
                        </form>
                      </div>
                    </div>

                    <c:forEach items="${grouperRequestContainer.subjectContainer.guiSubject.attributeNamesNonExpandedView}" 
                      var="attributeName" >
                      <c:set value="${grouperRequestContainer.subjectContainer.guiSubject.attributes[attributeName]}" var="attributeValue" />
                      <div class="row-fluid">
                        <div class="span2"><strong>${grouperRequestContainer.subjectContainer.guiSubject.attributeLabel[attributeName] }</strong></div>
                        <div class="span10">
                          <p style="margin-bottom: 0px;">${grouper:escapeHtml(attributeValue)}</p>
                          <p>${grouperRequestContainer.subjectContainer.guiSubject.attributeNameFriendlyDescripton[attributeName]}</p>
                        </div>
                      </div>
                    </c:forEach>

                    <div style="display: none;" id="subjectDetailsId">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <c:forEach items="${grouperRequestContainer.subjectContainer.guiSubject.attributeNamesExpandedView}" 
                              var="attributeName" >
                            <c:set value="${grouperRequestContainer.subjectContainer.guiSubject.attributes[attributeName]}" var="attributeValue" />
                            <tr>
                              <td><strong>${grouperRequestContainer.subjectContainer.guiSubject.attributeLabel[attributeName] }</strong></td>
                              <td>
                              <p style="margin-bottom: 0px;">${grouper:escapeHtml(attributeValue) }</p>
                              <p>${grouperRequestContainer.subjectContainer.guiSubject.attributeNameFriendlyDescripton[attributeName]}</p>
                              </td>
                            </tr>
                          </c:forEach>
                        </tbody>
                      </table>
                    </div>
                    <p id="subjectDetailsMoreId"><a href="#" aria-label="${textContainer.text['ariaLabelGuiMoreSubjectDetails']}" onclick="$('#subjectDetailsId').show('slow'); $('#subjectDetailsMoreId').hide(); $('#subjectDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="fa fa-angle-down"></i></a></p>
                    <p id="subjectDetailsLessId" style="display: none"><a href="#" onclick="$('#subjectDetailsId').hide('slow'); $('#subjectDetailsLessId').hide(); $('#subjectDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="fa fa-angle-up"></i></a></p>
                  </div>
                  <div class="span3" id="subjectMoreActionsButtonContentsDivId">
                    <%@ include file="subjectMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
