<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('customCompositeIndexBreadcrumb')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li class="active">${textContainer.text['customCompositeIndexBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">
    <h1>${textContainer.text['customCompositeIndexTitle'] }</h1>
    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['customCompositeIndexDescription'] }</p>
  </div>
</div>

<%-- custom composites --%>
<div class="row-fluid">
  <h2 class="grouper-heading-as-h4">${textContainer.text['customCompositeSectionTitle'] }</h2>
  <p>${textContainer.text['customCompositeSectionDescription'] }</p>
  <p>
    <a class="btn btn-medium" href="?operation=UiV2CustomComposite.addCustomComposite"
      onclick="return handleGuiV2LinkClick(event, 'operation=UiV2CustomComposite.addCustomComposite');">${textContainer.text['customCompositeAddButton'] }</a>
  </p>

  <c:choose>
    <c:when test="${empty grouperRequestContainer.customCompositeContainer.customComposites}">
      <p>${textContainer.text['customCompositeNoneConfigured'] }</p>
    </c:when>
    <c:otherwise>
      <table class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['customCompositeHeaderActions'] }</th>
            <th>${textContainer.text['customCompositeHeaderLabel'] }</th>
            <th>${textContainer.text['customCompositeHeaderCompositeType'] }</th>
            <th>${textContainer.text['customCompositeHeaderGroupName'] }</th>
            <th>${textContainer.text['customCompositeHeaderUiKey'] }</th>
            <th>${textContainer.text['customCompositeHeaderIndex'] }</th>
            <th>${textContainer.text['customCompositeHeaderSource'] }</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${grouperRequestContainer.customCompositeContainer.customComposites}" var="customComposite" varStatus="loopStatus">
            <tr>
              <td>
                <%-- the row actions menu, as on the other configuration lists --%>
                <div class="btn-group">
                  <button type="button" data-toggle="dropdown"
                    aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}"
                    class="btn btn-mini dropdown-toggle"
                    aria-haspopup="true" aria-expanded="false"
                    onclick="$('#composite-options${loopStatus.index}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#composite-options${loopStatus.index} li').first().focus();return true;});">
                    ${textContainer.text['customCompositeRowActionsButton'] }
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu" id="composite-options${loopStatus.index}">
                    <li><a href="?operation=UiV2CustomComposite.editCustomComposite&customCompositeIndex=${customComposite.index}"
                      onclick="return handleGuiV2LinkClick(event, 'operation=UiV2CustomComposite.editCustomComposite&customCompositeIndex=${customComposite.index}');"
                      >${textContainer.text['customCompositeEditAction'] }</a></li>
                    <%-- one set in a config file cannot be deleted here --%>
                    <c:if test="${customComposite.editable}">
                      <li class="divider"></li>
                      <li><button type="button" class="grouper-menuitem"
                        onclick="if (confirm('${textContainer.textEscapeSingleDouble['customCompositeConfirmDelete']}')) { ajax('../app/UiV2CustomComposite.deleteCustomComposite?customCompositeIndex=${customComposite.index}'); } return false;"
                        >${textContainer.text['customCompositeDeleteAction'] }</button></li>
                    </c:if>
                  </ul>
                </div>
              </td>
              <td>
                ${grouper:escapeHtml(customComposite.label)}
                <%-- an incomplete one is skipped on the members screen, so say why it is not there --%>
                <c:if test="${!customComposite.complete}">
                  <span class="label label-important">${textContainer.text['customCompositeIncomplete'] }</span>
                </c:if>
              </td>
              <td>${grouper:escapeHtml(customComposite.compositeType)}</td>
              <td>${grouper:escapeHtml(customComposite.groupName)}</td>
              <td>${grouper:escapeHtml(customComposite.uiKey)}</td>
              <td>${customComposite.index}</td>
              <c:set var="sourceTextKey" value="customCompositeSource_${customComposite.configSource}" />
              <td>${textContainer.text[sourceTextKey] }</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>

<%-- membership requirements --%>
<div class="row-fluid">
  <h2 class="grouper-heading-as-h4">${textContainer.text['membershipRequirementSectionTitle'] }</h2>
  <p>${textContainer.text['membershipRequirementSectionDescription'] }</p>
  <p>
    <a class="btn btn-medium" href="?operation=UiV2CustomComposite.addMembershipRequirement"
      onclick="return handleGuiV2LinkClick(event, 'operation=UiV2CustomComposite.addMembershipRequirement');">${textContainer.text['membershipRequirementAddButton'] }</a>
  </p>

  <c:choose>
    <c:when test="${empty grouperRequestContainer.customCompositeContainer.membershipRequirements}">
      <p>${textContainer.text['membershipRequirementNoneConfigured'] }</p>
    </c:when>
    <c:otherwise>
      <table class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['customCompositeHeaderActions'] }</th>
            <th>${textContainer.text['membershipRequirementHeaderConfigId'] }</th>
            <th>${textContainer.text['membershipRequirementHeaderRequireGroupName'] }</th>
            <th>${textContainer.text['membershipRequirementHeaderAttributeName'] }</th>
            <th>${textContainer.text['membershipRequirementHeaderHookEnable'] }</th>
            <th>${textContainer.text['customCompositeHeaderSource'] }</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${grouperRequestContainer.customCompositeContainer.membershipRequirements}" var="membershipRequirement" varStatus="loopStatus">
            <c:set var="membershipRequirementConfigIdUrl" value="${grouper:escapeUrl(membershipRequirement.configId)}" />
            <tr>
              <td>
                <div class="btn-group">
                  <button type="button" data-toggle="dropdown"
                    aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}"
                    class="btn btn-mini dropdown-toggle"
                    aria-haspopup="true" aria-expanded="false"
                    onclick="$('#requirement-options${loopStatus.index}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#requirement-options${loopStatus.index} li').first().focus();return true;});">
                    ${textContainer.text['customCompositeRowActionsButton'] }
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu" id="requirement-options${loopStatus.index}">
                    <li><a href="?operation=UiV2CustomComposite.editMembershipRequirement&membershipRequirementConfigId=${membershipRequirementConfigIdUrl}"
                      onclick="return handleGuiV2LinkClick(event, 'operation=UiV2CustomComposite.editMembershipRequirement&membershipRequirementConfigId=${membershipRequirementConfigIdUrl}');"
                      >${textContainer.text['customCompositeEditAction'] }</a></li>
                    <%-- one set in a config file cannot be deleted here --%>
                    <c:if test="${membershipRequirement.editable}">
                      <li class="divider"></li>
                      <li><button type="button" class="grouper-menuitem"
                        onclick="if (confirm('${textContainer.textEscapeSingleDouble['membershipRequirementConfirmDelete']}')) { ajax('../app/UiV2CustomComposite.deleteMembershipRequirement?membershipRequirementConfigId=${membershipRequirementConfigIdUrl}'); } return false;"
                        >${textContainer.text['customCompositeDeleteAction'] }</button></li>
                    </c:if>
                  </ul>
                </div>
              </td>
              <td>
                ${grouper:escapeHtml(membershipRequirement.configId)}
                <c:if test="${not empty membershipRequirement.retrieveProblems()}">
                  <span class="label label-important">${textContainer.text['membershipRequirementHasProblems'] }</span>
                </c:if>
              </td>
              <td>${grouper:escapeHtml(membershipRequirement.requireGroupName)}</td>
              <td>${grouper:escapeHtml(membershipRequirement.attributeName)}</td>
              <td>
                ${membershipRequirement.hookEnable ? textContainer.text['customCompositeYes'] : textContainer.text['customCompositeNo']}
                <c:if test="${membershipRequirement.hookEnable && membershipRequirement.hookDisabledGlobally}">
                  ${textContainer.text['membershipRequirementHookDisabledGlobally'] }
                </c:if>
              </td>
              <c:set var="sourceTextKey" value="customCompositeSource_${membershipRequirement.configSource}" />
              <td>${textContainer.text[sourceTextKey] }</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>
