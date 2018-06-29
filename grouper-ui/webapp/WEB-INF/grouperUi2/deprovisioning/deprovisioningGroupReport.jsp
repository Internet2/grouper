<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <ul class="nav nav-tabs">
        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
        <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
          <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
        </c:if>
        <%@ include file="../group/groupMoreTab.jsp" %>
      </ul>
    </div>
    <div class="row-fluid">
      <div class="lead span9">${textContainer.text['deprovisioningUserGroupReportDescription'] }</div>
      <div class="span3" id="deprovisioningGroupMoreActionsButtonContentsDivId">
        <%@ include file="deprovisioningGroupMoreActionsButtonContents.jsp"%>
      </div>
    </div>

              <form id="deprovisionUserFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                  <thead>
                    <tr>
                      <td colspan="6" class="table-toolbar gradient-background">
                        <a href="#" onclick="ajax('../app/UiV2Deprovisioning.deprovisioningOnGroupReportSubmit', 
                        {formIds: 'deprovisionUserFormId'}); return false;" 
                        class="btn" role="button">${textContainer.text['deprovisionUserDeprovisionReportButtonSubmit'] }</a></td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" checked="checked"
                            onchange="$('.membershipCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                        </label>
                      </th>
                      <th>${textContainer.text['deprovisioningSubjectColumn'] }</th>
                      <th>${textContainer.text['deprovisioningFromColumn'] }</th>
                      <th>${textContainer.text['deprovisioningMemberColumn'] }</th>
                      <th data-hide="phone,medium">${textContainer.text['deprovisioningPrivilegeColumn'] }</th>
                    </tr>
                  </thead>
                  <tbody>
                      <c:set var="i" value="0" />
                      <c:forEach items="${grouperRequestContainer.deprovisioningContainer.guiDeprovisioningMembershipSubjectContainers}" 
                        var="guiDeprovisioningMembershipSubjectContainer" >
                        <c:set var="guiMembershipSubjectContainer" value="${guiDeprovisioningMembershipSubjectContainer.guiMembershipSubjectContainer}"/>
                        <c:set var="objectType" value="${guiMembershipSubjectContainer.guiGroup != null ? 'group' : 
                        ( guiMembershipSubjectContainer.guiStem != null ? 'stem' : 
                        ( guiMembershipSubjectContainer.guiAttributeDef != null ? 'attributeDef' : 'unknown' ) )}" />
                        <c:set var="guiMembershipContainer" value="${guiMembershipSubjectContainer.someGuiMembershipContainer}" />
                        <tr>
                          <td>
                            <c:if test="${guiDeprovisioningMembershipSubjectContainer.showCheckbox}">
                              <label class="checkbox checkbox-no-padding">
                                <input type="checkbox" name="membershipRow_${i}" 
                                  ${guiDeprovisioningMembershipSubjectContainer.checkCheckbox ? 'checked="checked"' : '' }
                                  aria-label="${textContainer.text['groupMembershipsInOtherGropusCheckboxAriaLabel'] }"
                                  value="${guiMembershipContainer.membershipContainer.immediateMembership.uuid}" class="membershipCheckbox" />
                              </label>
                            </c:if>
                          </td>
                          <td>${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon}</td>
                          <td>${guiDeprovisioningMembershipSubjectContainer.deprovisionedFromAffiliationsString}</td>
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
  </div>
</div>
                