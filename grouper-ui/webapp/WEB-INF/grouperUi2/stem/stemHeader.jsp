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
                          <c:if test="${grouperRequestContainer.stemContainer.showAddInheritedPrivileges}">
                            <div id="choose-inherited-type" class="control-group">
                              <label class="control-label">${textContainer.text['stemPrivilegesInheritedAssignTo']}</label>
                              <div class="controls">
                                <label class="checkbox inline" for="inherited_privilege_stem_id">
                                  <input type="checkbox" name="inherited_privilege_stem" value="true" id="inherited_privilege_stem_id"
                                    onchange="if ($('#inherited_privilege_stem_id').prop('checked')) { showCustomPrivilege('add-members-privileges-select');} else {hideCustomPrivilege('add-members-privileges-select');}"
                                   />${textContainer.text['rulesThenTypeFolder']}
                                </label>
                                <label class="checkbox inline" for="inherited_privilege_group_id">
                                  <input type="checkbox" name="inherited_privilege_group" value="true" id="inherited_privilege_group_id" 
                                    onchange="if ($('#inherited_privilege_group_id').prop('checked')) {showCustomPrivilege('add-members-privileges');} else { hideCustomPrivilege('add-members-privileges');}"
                                  />${textContainer.text['rulesThenTypeGroup']}
                                </label>
                                <label class="checkbox inline" for="inherited_privilege_attributeDef_id" id="inheritedPrivilegeTypeErrorId">
                                  <input type="checkbox" name="inherited_privilege_attributeDef" value="true" id="inherited_privilege_attributeDef_id" 
                                    onchange="if ($('#inherited_privilege_attributeDef_id').prop('checked')) {showCustomPrivilege('add-members-privileges-attrDef');} else { hideCustomPrivilege('add-members-privileges-attrDef');}"
                                  />${textContainer.text['rulesThenTypeAttribute']}
                                </label>
                              </div>

                            </div>
                          </c:if>
                          <div id="add-members-privileges-select" class="control-group ${grouperRequestContainer.stemContainer.showAddInheritedPrivileges ? 'hide' : ''}">
                            <c:choose>
                              <c:when test="${grouperRequestContainer.stemContainer.showAddInheritedPrivileges}">
                                <label class="control-label">${textContainer.text['stemPrivilegesInheritedAddStemPrivileges']}</label>
                              </c:when>
                              <c:otherwise>
                                <label class="control-label">${textContainer.text['stemViewAssignThesePrivileges']}</label>
                              </c:otherwise>
                            </c:choose>
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
                              <label class="checkbox inline" id="stemPrivsErrorId">
                                <input type="checkbox" name="privileges_stemAttrUpdaters" value="true" />${textContainer.text['priv.stemAttrUpdatersUpper']}
                              </label>
                            </div>
                          </div>
                          <c:if test="${grouperRequestContainer.stemContainer.showAddInheritedPrivileges}">
                            <div id="add-members-privileges" class="control-group hide">
                              <label class="control-label" >${textContainer.text['stemPrivilegesInheritedAddGroupPrivileges']}</label>
                              <div class="controls">

                                <label class="checkbox inline">
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
                                <label class="checkbox inline"  id="groupPrivsErrorId">
                                  <input type="checkbox" name="privileges_groupAttrUpdaters" value="true" />${textContainer.text['priv.groupAttrUpdateUpper'] }
                                </label>
                              </div>
                            </div>
                          
                            <div id="add-members-privileges-attrDef" class="control-group hide">
                              <label class="control-label">${textContainer.text['stemPrivilegesInheritedAddAttributeDefPrivileges']}</label>
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
                                  <input type="checkbox" name="privileges_attributeDefAttrReaders" value="true" />${textContainer.text['priv.attrDefAttrReadUpper'] }
                                </label>
                                <label class="checkbox inline" id="attributeDefPrivsErrorId">
                                  <input type="checkbox" name="privileges_attributeDefAttrUpdaters" value="true" />${textContainer.text['priv.attrDefAttrUpdateUpper'] }
                                </label>
                              </div>
                            </div>
                            <div class="control-group">
                              <label for="levels" class="control-label">${textContainer.text['stemPrivilegesInheritedLevels'] }</label>
                              <div class="controls">
                                <label class="radio" for="level-all">
                                  <input type="radio" name="levelsName" id="level-all" value="sub" checked="checked"  
                                  >${textContainer.text['stemPrivilegesInheritAllLabel'] }
                                </label>
                                <label class="radio" for="level-one")>
                                  <input type="radio" name="levelsName" id="level-one" value="one"  
                                  >${textContainer.text['stemPrivilegesInheritOneLabel'] }
                                </label><%--span class="help-block">${textContainer.text['stemPrivilegesInheritedLevelsDecription'] }</span --%>
                              </div>
                            </div>

                          </c:if>                          
                          
                          <div class="control-group">
                            <div class="controls">
                              <c:choose>
                                <c:when test="${grouperRequestContainer.stemContainer.showAddInheritedPrivileges}">
                                  <button onclick="ajax('../app/UiV2Stem.privilegeInheritanceAddMemberSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'add-members-form'}); return false;" 
                                    id="add-members-submit" type="submit" class="btn btn-primary">${textContainer.text['stemPrivilegesInheritedAddMemberLink']}</button>
                                </c:when>
                                <c:otherwise>
                                  <button onclick="ajax('../app/UiV2Stem.addMemberSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'add-members-form', formIdsOptional: 'stemPagingPrivilegesFormId,stemFilterPrivilegesFormId'}); return false;" 
                                    id="add-members-submit" type="submit" class="btn btn-primary">${textContainer.text['groupViewAddMemberLink']}</button>
                                </c:otherwise>
                              </c:choose>
                            </div>
                          </div>
                        </form>
                      </div>
                    </div>
                    
                    <p>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.description)}</p>
                    <p>${grouperRequestContainer.objectTypeContainer.userFriendlyStringForConfiguredAttributes}</p>
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
                    <p id="stemDetailsMoreId"><a href="#" aria-label="${textContainer.text['ariaLabelGuiMoreStemDetails']}" onclick="$('#stemDetailsId').show('slow'); $('#stemDetailsMoreId').hide(); $('#stemDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="fa fa-angle-down"></i></a></p>
                    <p id="stemDetailsLessId" style="display: none"><a href="#" onclick="$('#stemDetailsId').hide('slow'); $('#stemDetailsLessId').hide(); $('#stemDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="fa fa-angle-up"></i></a></p>
                     
                  </div>
                  <div class="span2" id="stemMoreActionsButtonContentsDivId">
                    <%@ include file="stemMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
