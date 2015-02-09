<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />


            <div class="bread-header-container">
              <ul class="breadcrumb">
                ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbBullets}
                <li class="active">${grouperRequestContainer.subjectContainer.guiSubject.screenLabelShort2noLink}</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-user"> </i> ${grouperRequestContainer.subjectContainer.guiSubject.screenLabelShort2noLink}
                <br /><small>${textContainer.text['membershipEditSubHeader']}</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-horizontal" id="editMembershipFormId">

                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                  <input type="hidden" name="memberId" value="${grouperRequestContainer.subjectContainer.guiSubject.memberId}" />
                  <input type="hidden" name="fieldId" value="${grouperRequestContainer.membershipGuiContainer.field.id}" />

                  <div class="control-group">
                    <label class="control-label">${textContainer.text['subjectViewLabelId'] }</label>
                    <div class="controls">
                      ${grouper:escapeHtml(grouperRequestContainer.subjectContainer.guiSubject.subject.id)}
                    </div>
                  </div>
                  <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.hasEmailAttributeInSource }">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['subjectViewLabelEmail']}</label>
                      <div class="controls">
                        ${grouperRequestContainer.subjectContainer.guiSubject.email}
                      </div>
                    </div>
                  </c:if>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['subjectViewLabelName'] }</label>
                    <div class="controls">
                      ${grouper:escapeHtml(grouperRequestContainer.subjectContainer.guiSubject.subject.name)}
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['subjectViewLabelDescription'] }</label>
                    <div class="controls">
                      ${grouper:escapeHtml(grouperRequestContainer.subjectContainer.guiSubject.subject.description)}
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label hide">${textContainer.text['membershipEditLabelMembership'] }</label>
                    <div class="controls">
                      <label class="checkbox">
                        <c:choose>
                          <c:when test="${grouperRequestContainer.membershipGuiContainer.directMembership}">
                            <input type="checkbox" name="hasMembership" checked="checked" value="true" /> 
                              ${textContainer.text['membershipEditHasDirectMembership']}
                          </c:when>
                          <c:otherwise>
                            <input type="checkbox" name="hasMembership" value="true" />
                              ${textContainer.text['membershipEditHasDirectMembership']}
                          </c:otherwise>
                        </c:choose>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label hide">${textContainer.text['membershipEditLabelIndirectMembership'] }</label>
                    <div class="controls">
                      <label class="checkbox">
                        <c:choose>
                          <c:when test="${grouperRequestContainer.membershipGuiContainer.indirectMembership}">
                            <input type="checkbox" name="hasIndirectMembership" checked="checked" value="true" disabled="disabled" /> 
                              ${textContainer.text['membershipEditHasIndirectMembership']}
                          </c:when>
                          <c:otherwise>
                            <input type="checkbox" name="hasMembership" value="true" disabled="disabled" />
                              ${textContainer.text['membershipEditNotHasIndirectMembership']}
                          </c:otherwise>
                        </c:choose>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="member-start-date"
                      class="control-label">${textContainer.text['membershipEditLabelStartDate'] }</label>
                    <div class="controls">
                      <input type="text" name="startDate"  placeholder="${textContainer.text['membershipEditDatePlaceholder'] }"  
                        value="${grouperRequestContainer.membershipGuiContainer.directGuiMembership.startDateLabel }" id="member-start-date"><span class="help-block">${textContainer.text['membershipEditLabelStartDateSubtext'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="member-end-date" class="control-label">${textContainer.text['membershipEditLabelEndDate'] }</label>
                    <div class="controls">
                      <input type="text" name="endDate" placeholder="${textContainer.text['membershipEditDatePlaceholder'] }"
                        value="${grouperRequestContainer.membershipGuiContainer.directGuiMembership.endDateLabel }" id="member-end-date"><span class="help-block">${textContainer.text['membershipEditLabelEndDateSubtext'] }</span>
                    </div>
                  </div>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['membershipEditLabelDirectPrivileges'] }</label>
                      <div class="controls">
                        <c:forEach items="admins,readers,updaters,optins,optouts,groupAttrReaders,groupAttrUpdaters,viewers" var="fieldName">
                          <c:set value="${grouperRequestContainer.membershipGuiContainer.privilegeGuiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" 
                            var="guiMembershipContainer" />
                          <label class="checkbox inline">
                            <input type="checkbox" id="inlineCheckbox1" 
                              ${guiMembershipContainer.membershipContainer.membershipAssignType.immediate ? 'checked="checked"' : ''}
                              name="privilege_${fieldName}" value="true">${textContainer.text[grouper:concat3('priv.', fieldName, 'Upper')]}
                          </label>
                        </c:forEach>
                      </div>
                    </div>
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['membershipEditLabelIndirectPrivileges'] }</label>
                      <div class="controls">
                        <c:forEach items="admins,readers,updaters,optins,optouts,groupAttrReaders,groupAttrUpdaters,viewers" var="fieldName">
                          <c:set value="${grouperRequestContainer.membershipGuiContainer.privilegeGuiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" 
                            var="guiMembershipContainer" />
                          <label class="checkbox inline">
                            <input type="checkbox" id="inlineCheckbox1" disabled="disabled"
                              ${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate ? 'checked="checked"' : ''}
                              name="privilegeReadonly_${fieldName}" value="true">${textContainer.text[grouper:concat3('priv.', fieldName, 'Upper')]}
                          </label>
                        </c:forEach>
                      </div>
                    </div>
                  </c:if>
                  <input type="hidden" name="backTo" value="${grouperRequestContainer.membershipGuiContainer.editMembershipFromSubject ? 'subject' : 'group' }" />
                  <div class="form-actions"><a href="#" onclick="ajax('../app/UiV2Membership.saveMembership', {formIds: 'editMembershipFormId'}); return false;" class="btn btn-primary">${textContainer.text['membershipEditSaveButton'] }</a> 
                    <c:choose>
                      <c:when test="${grouperRequestContainer.membershipGuiContainer.editMembershipFromSubject}">
                        <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}');"
                           class="btn">${textContainer.text['membershipEditCancelButton']}</a>
                      </c:when>
                      <c:otherwise>
                        <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');"
                           class="btn">${textContainer.text['membershipEditCancelButton']}</a>
                      </c:otherwise>
                    </c:choose>
                    <a href="#" onclick="return guiV2link('operation=UiV2Membership.traceMembership&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&memberId=${grouperRequestContainer.subjectContainer.guiSubject.memberId}&field=members&backTo=membership');"
                       class="btn">${textContainer.text['membershipEditTraceButton']}</a>
                  </div>                  
                </form>
              </div>
            </div>
