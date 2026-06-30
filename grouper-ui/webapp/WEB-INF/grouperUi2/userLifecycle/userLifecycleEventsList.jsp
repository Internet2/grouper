<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousUserLifecycleEventsListBreadcrumb')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>

    <li class="active">${textContainer.text['miscellaneousUserLifecycleEventsListBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">

    <div class="row-fluid">
      <div class="lead span8 pull-left">
        <h4>${textContainer.text['miscellaneousUserLifecycleEventsListMainDescription'] }</h4>
      </div>
    </div>
    <div class="row-fluid">
      <div class="span12">
        <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousUserLifecycleEventsListSubtitle']}</p>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">
  <form id="membershipsToKeepOrRemove">
    <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
      <thead>
        <tr>
          <td colspan="6" class="table-toolbar gradient-background">
            <a href="#" id="groupKeepSelectedMembersButton" onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['miscellaneousUserLifecycleEventsListKeepSelectedConfirmChanges']}')) {ajax('../app/UiV2UserLifecycleEvents.keepMembers', {formIds: 'membershipsToKeepOrRemove'});} return false;" class="btn" role="button">${textContainer.text['miscellaneousUserLifecycleEventsListKeepSelectedUsersButton'] }</a>
            <a href="#" id="groupRemoveSelectedMembersButton" onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['miscellaneousUserLifecycleEventsListRemoveSelectedConfirmChanges']}')) {ajax('../app/UiV2UserLifecycleEvents.removeMembers', {formIds: 'membershipsToKeepOrRemove'});} return false;" class="btn" role="button">${textContainer.text['miscellaneousUserLifecycleEventsListRemoveSelectedUsersButton'] }</a>
          </td>
        </tr>
        <tr>
          <th>
            <label class="checkbox checkbox-no-padding">
              <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.eventCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
              <span class="sr-only">${textContainer.text['miscellaneousUserLifecycleEventsListMembershipCheckboxAriaLabel']}</span>
            </label>
          </th>
          <th>${textContainer.text['miscellaneousUserLifecycleEventsListHeaderGroupName']}</th>
          <th>${textContainer.text['miscellaneousUserLifecycleEventsListHeaderEntityName']}</th>
          <th>${textContainer.text['miscellaneousUserLifecycleEventsListHeaderEventDate']}</th>
          <th>${textContainer.text['miscellaneousUserLifecycleEventsListHeaderEventDescription']}</th>
          <th>${textContainer.text['miscellaneousUserLifecycleEventsListHeaderMembershipRemovalDate']}</th>
        </tr>
      </thead>
      <tbody>
        <c:set var="i" value="0" />
        <c:forEach items="${grouperRequestContainer.userLifecycleEventsContainer.userLifecycleEventContainers}" var="userLifecycleEventContainer" >
          <tr>
            <td>
              <label class="checkbox checkbox-no-padding">
                <input type="checkbox" aria-label="${textContainer.text['miscellaneousUserLifecycleEventsListMembershipCheckboxAriaLabel']}" name="membershipRow_${i}" value="${userLifecycleEventContainer.membershipId}" class="eventCheckbox" />
              </label>
            </td>
            <td class="expand foo-clicker">${userLifecycleEventContainer.guiGroup.shortLinkWithIcon} <br /></td>
            <td class="expand foo-clicker">${userLifecycleEventContainer.guiSubject.shortLinkWithIcon} <br /></td>
            <td>${userLifecycleEventContainer.eventDateFormatted}</td>
            <td>${grouper:escapeHtml(userLifecycleEventContainer.eventDescription)}</td>
            <td>${userLifecycleEventContainer.membershipRemovalDateFormatted}</td>
          </tr>
          <c:set var="i" value="${i+1}" />
        </c:forEach>
      </tbody>
    </table>
  </form>
</div>
