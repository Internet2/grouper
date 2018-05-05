<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<br /><br />
${textContainer.text['deprovisioningUserResultsDescription'] }
<br /><br />
                <form id="groupsToDeleteFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                  <thead>
                    <tr>
                      <td colspan="2" class="table-toolbar gradient-background">
                        <b>${textContainer.text['deprovisionUserNotesLabel'] }</b></td>
                      <td colspan="4" class="table-toolbar gradient-background">
                        <textarea rows="2" cols="30"></textarea>
                      </td>
                    </tr>
                    <tr>
                      <td colspan="6" class="table-toolbar gradient-background">
                        <a href="#" onclick="ajax('../app/UiV2Dep.removeMembersForThisGroupsMemberships?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', 
                        {formIds: 'deprovisionSearchFormId'}); return false;" 
                        class="btn" role="button">${textContainer.text['deprovisionUserDeprovisionButtonSubmit'] }</a></td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" checked="checked"
                            onchange="$('.membershipCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                        </label>
                      </th>
                      <th data-hide="phone,medium">${textContainer.text['thisGroupsMembershipsFolderColumn'] }</th>
                      <th>${textContainer.text['deprovisioningObjectColumn'] }</th>
                      <th>${textContainer.text['deprovisioningObjectTypeColumn'] }</th>
                      <th>${textContainer.text['deprovisioningMemberColumn'] }</th>
                      <th data-hide="phone,medium">${textContainer.text['deprovisioningPrivilegeColumn'] }</th>
                    </tr>
                  </thead>
                  <tbody>
                      <c:set var="i" value="0" />
                      <c:forEach items="${grouperRequestContainer.deprovisioningContainer.guiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >
                        <c:set var="objectType" value="${guiMembershipSubjectContainer.guiGroup != null ? 'group' : 
                        ( guiMembershipSubjectContainer.guiStem != null ? 'stem' : 
                        ( guiMembershipSubjectContainer.guiAttributeDef != null ? 'attributeDef' : 'unknown' ) )}" />
                        <c:set var="guiMembershipContainer" value="${guiMembershipSubjectContainer.someGuiMembershipContainer}" />
                        <tr>
                          <td>
                            <label class="checkbox checkbox-no-padding">
                              <input type="checkbox" name="membershipRow_${i}" 
                              checked="checked"
                              aria-label="${textContainer.text['groupMembershipsInOtherGropusCheckboxAriaLabel'] }"
                              value="${guiMembershipContainer.membershipContainer.immediateMembership.uuid}" class="membershipCheckbox" />
                            </label>
                          </td>
                          <td data-hide="phone,medium" style="white-space: nowrap;">
                            ${guiMembershipSubjectContainer.guiObjectBase.pathColonSpaceSeparated}
                          </td>
                          <td>${guiMembershipSubjectContainer.guiObjectBase.shortLinkWithIcon}</td>
                          <td>${textContainer.text[grouper:concat2('deprovisioningObjectType_',objectType)] }
                          
                          </td>
                          <td>${guiMembershipSubjectContainer.guiGroup == null ? textContainer.text['deprovisioningMemberColumnCantBeMember'] : ( guiMembershipContainer == null ? textContainer.text['deprovisioningMemberColumnIsNotMember']  : textContainer.text['deprovisioningMemberColumnIsMember'] )}</td>
                          <td data-hide="phone,medium">
                            ${guiMembershipSubjectContainer.privilegesCommaSeparated}
                          </td>
                        </tr>
                        <c:set var="i" value="${i+1}" />
                      </c:forEach>
                  </tbody>
                </table>
                </form>
