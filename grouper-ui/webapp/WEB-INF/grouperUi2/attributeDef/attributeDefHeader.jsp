<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.breadcrumbs}
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="span10">
                    <h4>${textContainer.text['attributeDefHeaderAttributeDefinition'] }</h4>
                    <h1><i class="fa fa-cog"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.extension)}</h1>

                    <div id="member-search" tabindex="-1" role="dialog" aria-labelledby="member-search-label" aria-hidden="true" class="modal hide fade">
                      <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                        <h3 id="member-search-label">${textContainer.text['attributeDefSearchForEntityButton'] }</h3>
                      </div>
                      <div class="modal-body">
                        <form class="form form-inline" id="addMemberSearchFormId">
                          <input name="addMemberSubjectSearch" type="text" placeholder=""/>
                          <button class="btn" onclick="ajax('../app/UiV2AttributeDef.addMemberSearch?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {formIds: 'addMemberSearchFormId'}); return false;" >${textContainer.text['attributeDefSearchButton'] }</button>
                          <br />
                          <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['attributeDefLabelExactIdMatch'] }</span>
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
                        <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['attributeDefSearchCloseButton']}</button>
                      </div>
                    </div>
                    <div id="add-block-container" class="well hide">
                      <div id="add-members">
                        <form id="add-members-form" target="#" class="form-horizontal form-highlight">
                          <div class="control-group">
                            <label for="add-block-input" class="control-label">${textContainer.text['attributeDefSearchMemberOrId'] }</label>
                            <div class="controls">
                              <div id="add-members-container">

                                <%-- placeholder: Enter the name of a person, group, or other entity --%>
                                <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 30em"
                                  filterOperation="../app/UiV2AttributeDef.addMemberFilter?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}"/>
                                ${textContainer.text['attributeDefSearchLabelPreComboLink']} <a href="#member-search" onclick="$('#addMemberResults').empty();" role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['attributeDefSearchForEntityLink']}</a>
                              </div>
                            </div>
                          </div>
                          <div id="add-members-privileges" class="control-group">
                            <label class="control-label">${textContainer.text['attributeDefViewAssignThesePrivileges']}</label>
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
                            <div class="controls">
                              <button onclick="ajax('../app/UiV2AttributeDef.addMemberSubmit?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {formIds: 'add-members-form', formIdsOptional: 'attributeDefRefreshPartFormId, attributeDefFilterFormId,attributeDefPagingFormId,attributeDefPagingPrivilegesFormId,attributeDefFilterPrivilegesFormId,attributeDefPagingAuditForm, attributeDefFilterAuditFormId, attributeDefQuerySortAscendingFormId'}); return false;" 
                                id="add-members-submit" type="submit" class="btn btn-primary">${textContainer.text['attributeDefViewAddMemberLink']}</button>
                            </div>
                          </div>
                        </form>
                      </div>
                      
                    </div>
                    <p>${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.description)}</p>
                    <div id="attributeDefDetailsId" style="display: none;">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelType']}</strong></td>
                            <td><grouper:message key="simpleAttributeUpdate.type.${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb}" /></td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelValueType']}</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'marker'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.marker'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'floating'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.floating'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'integer'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.integer'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'memberId'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.memberId'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'string'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.string'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'timestamp'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.timestamp'] }
                                </c:when>
                              </c:choose>
                            
                            </td>
                          </tr>
                          <tr>
                            <td class="top-vertical-align"><strong>${textContainer.text['attributeDefLabelAssignTo']}</strong></td>
                            <td>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToAttributeDef }">
                                ${textContainer.text['attributeDefAssignTo.attributeDef'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToAttributeDefAssn }">
                                ${textContainer.text['attributeDefAssignTo.attributeDefAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToEffMembership }">
                                ${textContainer.text['attributeDefAssignTo.membership'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToEffMembershipAssn }">
                                ${textContainer.text['attributeDefAssignTo.membershipAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToGroup }">
                                ${textContainer.text['attributeDefAssignTo.group'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToGroupAssn }">
                                ${textContainer.text['attributeDefAssignTo.groupAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToImmMembership }">
                                ${textContainer.text['attributeDefAssignTo.immediateMembership'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToImmMembershipAssn }">
                                ${textContainer.text['attributeDefAssignTo.immediateMembershipAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToMember }">
                                ${textContainer.text['attributeDefAssignTo.member'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToMemberAssn }">
                                ${textContainer.text['attributeDefAssignTo.memberAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToStem }">
                                ${textContainer.text['attributeDefAssignTo.stem'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToStemAssn }">
                                ${textContainer.text['attributeDefAssignTo.stemAssign'] } <br />
                              </c:if>
                            </td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefMultiAssignable']}</strong></td>
                            <td>${textContainer.text[grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.multiAssignable ? 'attributeDefMultiAssignableYes' : 'attributeDefMultiAssignableNo']}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefMultiValued']}</strong></td>
                            <td>${textContainer.text[grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.multiValued ? 'attributeDefMultiValuedYes' : 'attributeDefMultiValuedNo']}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelIdPath']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.name)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelId']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.extension)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelCreated'] }</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.createdString }</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelCreator'] }</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.creatorGuiSubject.shortLinkWithIcon}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelLastEdited']}</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.lastEditedString}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelLastEditedBy']}</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.lastUpdatedByGuiSubject.shortLinkWithIcon}</td>
                          </tr>
                          <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin }">
                            <tr>
                              <td><strong>${textContainer.text['groupLabelPrivilegesAssignedToEveryone']}</strong></td>
                              <td>
                                ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.privilegeLabelsAllowedByGrouperAll }
                              
                              </td>
                            </tr>
                          </c:if>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelIdIndex']}</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.idIndex}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelUuid']}</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.uuid}</td>
                          </tr>

                        </tbody>
                      </table>
                    </div>
                    <p id="attributeDefDetailsMoreId"><a href="#" aria-label="${textContainer.text['ariaLabelGuiMoreAttributeDefDetails']}" id="moreButtonId" onclick="$('#attributeDefDetailsId').show('slow'); $('#attributeDefDetailsMoreId').hide(); $('#attributeDefDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="fa fa-angle-down"></i></a></p>
                    <p id="attributeDefDetailsLessId" style="display: none"><a href="#" onclick="$('#attributeDefDetailsId').hide('slow'); $('#attributeDefDetailsLessId').hide(); $('#attributeDefDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="fa fa-angle-up"></i></a></p>
                  </div>
                  <div class="span2" id="attributeDefMoreActionsButtonContentsDivId">
                    <%@ include file="attributeDefMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
