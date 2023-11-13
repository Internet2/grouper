<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('stemDeprovisioningReportPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.name)}

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

<%@ include file="../stem/stemHeader.jsp" %>

<div class="row-fluid">
  <div class="span12 tab-interface">
    <ul class="nav nav-tabs">
      <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
      <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
      </c:if>
      <%@ include file="../stem/stemMoreTab.jsp" %>
    </ul>
    <div class="row-fluid">
      <div class="lead span9">${textContainer.text['deprovisioningUserGroupReportDescription'] }</div>
      <div class="span3" id="deprovisioningFolderMoreActionsButtonContentsDivId">
        <%@ include file="deprovisioningFolderMoreActionsButtonContents.jsp"%>
      </div>
    </div>
    <div class="row-fluid">
      <div class="span9"> <p>${textContainer.text['deprovisionReportDescription'] }</p></div>
    </div>
    
    <c:choose>
      <c:when test="${fn:length(grouperRequestContainer.deprovisioningContainer.guiDeprovisioningMembershipSubjectContainers) > 0}">
        <form id="deprovisionUserFormId">
          <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
          <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
            <thead>
              <tr>
                <td colspan="6" class="table-toolbar gradient-background">
                  <a href="#" onclick="ajax('../app/UiV2Deprovisioning.deprovisioningOnFolderReportSubmit', 
                    {formIds: 'deprovisionUserFormId'}); return false;" 
                    class="btn" role="button">${textContainer.text['deprovisionUserDeprovisionReportButtonSubmit'] }</a>
                  &nbsp;
                  <a href="#" onclick="ajax('../app/UiV2Deprovisioning.updateFolderLastCertifiedDate', 
                    {formIds: 'deprovisionUserFormId'}); return false;" 
                    class="btn" role="button">${textContainer.text['deprovisionReportUpdateCertifyDateButtonSubmit'] }</a>
                </td>
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
                          <input type="checkbox" name="memberIds_${i}" 
                            ${guiDeprovisioningMembershipSubjectContainer.checkCheckbox ? 'checked="checked"' : '' }
                            aria-label="${textContainer.text['groupMembershipsInOtherGropusCheckboxAriaLabel'] }"
                            value="${guiMembershipContainer.membershipContainer.immediateMembership.memberUuid}" class="membershipCheckbox" />
                        </label>
                      </c:if>
                    </td>
                    <td>${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon}</td>
                    <td>${guiDeprovisioningMembershipSubjectContainer.deprovisionedFromAffiliationsString}</td>
                    <td data-hide="phone,medium">
                      ${guiMembershipSubjectContainer.privilegesCommaSeparated}
                    </td>
                  </tr>
                  <c:set var="i" value="${i+1}" />
                </c:forEach>
            </tbody>
          </table>
        </form>
      </c:when>
      <c:otherwise>
        <div class="row-fluid">
          <div class="span9"> <p><b>${textContainer.text['deprovisioningReportNoEntitiesFoundOnFolder'] }</b></p></div>
        </div>
      </c:otherwise>
    </c:choose>

  </div>
</div>
                
