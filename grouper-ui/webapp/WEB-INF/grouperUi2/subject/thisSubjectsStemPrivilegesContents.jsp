<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <form class="form-inline form-small" name="groupPrivilegeFormName" id="groupPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="11" class="table-toolbar gradient-background">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="people-update">Update:</label>
                          </div>
                          <div class="span4">

                            <select id="people-update" class="span12" name="groupPrivilegeBatchUpdateOperation">
                              <%-- create group should be the default, so list it first --%>

                              <option value="assign_stemAdmins">${textContainer.text['groupPrivilegesAssignStemAdminPrivilege'] }</option>
                              <option value="assign_creators">${textContainer.text['groupPrivilegesAssignCreatePrivilege'] }</option>
                              <option value="assign_stemAttrReaders">${textContainer.text['groupPrivilegesAssignStemAttributeReadPrivilege'] }</option>
                              <option value="assign_stemAttrUpdaters">${textContainer.text['groupPrivilegesAssignStemAttributeUpdatePrivilege'] }</option>
                              <option value="revoke_stemAdmins">${textContainer.text['groupPrivilegesRevokeStemAdminPrivilege'] }</option>
                              <option value="revoke_creators">${textContainer.text['groupPrivilegesRevokeCreatePrivilege'] }</option>
                              <option value="revoke_stemAttrReaders">${textContainer.text['groupPrivilegesRevokeStemAttributeReadPrivilege'] }</option>
                              <option value="revoke_stemAttrUpdaters">${textContainer.text['groupPrivilegesRevokeStemAttributeUpdatePrivilege'] }</option>
                              <option value="revoke_all">${textContainer.text['groupPrivilegesRevokeAllStemPrivilege'] }</option>

                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignStemPrivilegeBatch?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId,groupPrivilegeFormId'}); return false;">${textContainer.text['thisSubjectPrivilegeUpdateSelectedButton'] }</button>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.privilegeCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                        </label>
                      </th>
                      <th>
                        ${textContainer.text['thisSubjectsPrivilegesStemColumn'] }
                      </th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colStemAdmin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colCreate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colStemAttributeRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colStemAttributeUpdate'] }</th>
                      <th style="width:100px;"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.subjectContainer.privilegeGuiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >
                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" value="${guiMembershipSubjectContainer.guiStem.stem.id}" class="privilegeCheckbox" />
                          </label>
                        </td>
                        <td class="expand foo-clicker" style="white-space: nowrap">${guiMembershipSubjectContainer.guiStem.shortLinkWithIcon}
                        </td>
                        <%-- loop through the fields for groups --%>
                        <c:forEach items="stemAdmins,creators,stemAttrReaders,stemAttrUpdaters" var="fieldName">
                          <td data-hide="phone,medium" class="direct-actions privilege" >
                            <c:set value="${guiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" var="guiMembershipContainer" />
                            <%-- if there is a container, then there is an assignment of some sort... --%>
                            <c:choose>
                              <c:when test="${guiMembershipContainer != null 
                                   && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <i class="fa fa-check fa-direct" tabindex="0" aria-label="${textContainer.textEscapeXml['thisSubjectsPrivilegesRemoveTitle'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignStemPrivilege?assign=false&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&fieldName=${fieldName}&parentStemId=${guiMembershipSubjectContainer.guiStem.stem.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;}"></i>
                                <a title="${textContainer.textEscapeXml['thisSubjectsPrivilegesRemoveTitle'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignStemPrivilege?assign=false&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&fieldName=${fieldName}&parentStemId=${guiMembershipSubjectContainer.guiStem.stem.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-times"></i></a>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><i class="fa fa-check fa-disabled" tabindex="0" aria-label="${textContainer.textEscapeXml['thisSubjectsPrivilegesAssignTitle'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignStemPrivilege?assign=true&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&fieldName=${fieldName}&parentStemId=${guiMembershipSubjectContainer.guiStem.stem.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;}"></i></c:if>
                                <a title="${textContainer.textEscapeXml['thisSubjectsPrivilegesAssignTitle'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignStemPrivilege?assign=true&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&fieldName=${fieldName}&parentStemId=${guiMembershipSubjectContainer.guiStem.stem.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-plus"></i></a>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group">
                          	<a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" href="#" class="btn btn-mini dropdown-toggle"
                          		aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#subject-stem-priv-more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#subject-stem-priv-more-options${i} li').first().focus();return true;});">
                          		${textContainer.text['thisSubjectsPrivilegesActionsButton']} 
                          			<span class="caret"></span>
                          	</a>
                            <ul class="dropdown-menu dropdown-menu-right" id="subject-stem-priv-more-options${i}">
                              
                              <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate}">
                                <li><a href="#"  onclick="return guiV2link('operation=UiV2Membership.traceStemPrivileges&stemId=${guiMembershipSubjectContainer.guiStem.stem.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&backTo=subject'); return false;" class="actions-revoke-membership">${textContainer.text['thisSubjectsPrivilegesActionsMenuTracePrivileges'] }</a></li>
                              </c:if>
                              
                              <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${guiMembershipSubjectContainer.guiStem.stem.id}');">${textContainer.text['thisSubjectsPrivilegesActionsMenuViewStem']}</a></li>
                            </ul>
                          </div>
                        </td>
                      </tr>
                      <c:set var="i" value="${i+1}" />
                    </c:forEach>
                  </tbody>
                </table>
              </form>
              <div class="data-table-bottom gradient-background">
                <grouper:paging2 guiPaging="${grouperRequestContainer.subjectContainer.privilegeGuiPaging}" formName="groupPagingPrivilegesForm" ajaxFormIds="groupFilterPrivilegesFormId"
                  refreshOperation="../app/UiV2Subject.filterThisSubjectsStemPrivileges?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" />
              </div>
              
