<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <!-- start group/groupHeader.jsp -->
            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="span2 pull-right" id="groupMoreActionsButtonContentsDivId">
                    <%@ include file="groupMoreActionsButtonContents.jsp"%>
                  </div>
                  <div class="span10 pull-left">
                    <h1><i class="fa fa-group"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</h1>
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
                    <div id="add-block-container" class="well hide">
                      <c:choose>
                        <c:when test="${!defaultMemberUnchecked && grouperRequestContainer.groupContainer.guiGroup.group.hasComposite}">
                          ${textContainer.text['groupCompositeCantAddMembersToComposite']}
                        </c:when>
                        <c:otherwise>
                          <div id="add-members">
                            <form id="add-members-form" target="#" class="form-horizontal form-highlight">
                              <div class="control-group" id="add-member-control-group" aria-live="polite" aria-expanded="false">
                                <label for="groupAddMemberComboID" class="control-label">${textContainer.text['groupSearchMemberOrId'] }</label>
                                <div class="controls">
                                  <div id="add-members-container">
    
                                    <%-- placeholder: Enter the name of a person, group, or other entity --%>
                                    <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 30em"
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
                                  <%-- dont show member if its a composite --%>
                                  <c:if test="${!grouperRequestContainer.groupContainer.guiGroup.group.hasComposite}">
                                    <label class="checkbox inline">
                                      <input type="checkbox" name="privileges_members" value="true" ${defaultMemberUnchecked ? '' : 'checked="checked" '} />${textContainer.text['priv.memberUpper']}
                                    </label>
                                  </c:if>
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
                              
                              <div class="control-group">
                                <div class="controls">
                                  <button onclick="ajax('../app/UiV2Group.addMemberSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'add-members-form', formIdsOptional: 'groupRefreshPartFormId, groupFilterFormId,groupPagingFormId,groupPagingPrivilegesFormId,groupFilterPrivilegesFormId,groupPagingAuditForm, groupFilterAuditFormId, groupQuerySortAscendingFormId'}); return false;" 
                                    id="add-members-submit" type="submit" class="btn btn-primary">${textContainer.text['groupViewAddMemberLink']}</button> ${textContainer.text['groupViewTextBetweenAddAndBulk']} <a href="#" onclick="return guiV2link('operation=UiV2GroupImport.groupImport&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&backTo=group'); return false;" class="blue-link">${textContainer.text['groupViewBulkLink'] }</a> ${textContainer.text['groupViewTextPostBulkLink'] }
                                </div>
                              </div>
                            </form>
                          </div>
                        </c:otherwise>
                      </c:choose>
                      
                    </div>
                    <p>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.description)}</p>
                    <p>${grouperRequestContainer.objectTypeContainer.userFriendlyStringForConfiguredAttributes}</p>
                    <div id="groupDetailsId" style="display: none;">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelName']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelPath']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayName)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelIdPath']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.name)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelAlternateIdPath']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.alternateName)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelId']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.extension)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelCreated'] }</strong></td>
                            <td>${grouperRequestContainer.groupContainer.guiGroup.createdString }</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelCreator'] }</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.groupContainer.guiGroup.group.creatorUuid)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelLastEdited']}</strong></td>
                            <td>${grouperRequestContainer.groupContainer.guiGroup.lastEditedString}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelLastEditedBy']}</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.groupContainer.guiGroup.group.modifierUuid)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelTypeLabel']}</strong></td>
                            <td>${textContainer.text[grouper:concat2('groupLabelType_',grouperRequestContainer.groupContainer.guiGroup.group.typeOfGroup)]}</td>
                          </tr>
                          <c:if test="${grouperRequestContainer.groupContainer.canAdmin }">
                            <tr>
                              <td><strong>${textContainer.text['groupLabelPrivilegesAssignedToEveryone']}</strong></td>
                              <td>
                                ${grouperRequestContainer.groupContainer.guiGroup.privilegeLabelsAllowedByGrouperAll }
                              
                              </td>
                            </tr>
                          </c:if>
                          <c:if test="${grouperRequestContainer.groupContainer.canRead }">

                            <tr>
                              <td><strong>${textContainer.text['groupLabelCompositeOwner'] }</strong></td>
                              <td>${grouperRequestContainer.groupContainer.guiGroup.compositeOwnerText}</td>
                            </tr>
                            <tr>
                              <td><strong>${textContainer.text['groupLabelCompositeFactors'] }</strong></td>
                              <td>${grouperRequestContainer.groupContainer.guiGroup.compositeFactorOfOtherGroupsText}</td>
                            </tr>
                          </c:if>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelIdIndex']}</strong></td>
                            <td>${grouperRequestContainer.groupContainer.guiGroup.group.idIndex}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelUuid']}</strong></td>
                            <td>${grouperRequestContainer.groupContainer.guiGroup.group.uuid}</td>
                          </tr>

                        </tbody>
                      </table>
                    </div>
                    <p id="groupDetailsMoreId"><a href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGroupDetails']}" id="moreButtonId" onclick="$('#groupDetailsId').show('slow'); $('#groupDetailsMoreId').hide(); $('#groupDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="fa fa-angle-down"></i></a></p>
                    <p id="groupDetailsLessId" style="display: none"><a href="#" onclick="$('#groupDetailsId').hide('slow'); $('#groupDetailsLessId').hide(); $('#groupDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="fa fa-angle-up"></i></a></p>
                  </div>
                </div>
              </div>
            </div>
