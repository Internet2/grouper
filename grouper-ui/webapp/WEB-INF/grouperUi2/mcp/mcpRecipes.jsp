<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousMcpRecipesBreadcrumb')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>

    <li class="active">${textContainer.text['miscellaneousMcpRecipesBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">

    <div class="row-fluid">
      <%-- heading and blurb sized as on the MCP info screen: a plain h1 with the description
           tucked under it.  not the lead/grouper-heading-as-h4 pairing used elsewhere, which
           puts .lead's 19.5px on the paragraph and 16.25px on the h1, so the description ends
           up larger than the heading above it --%>
      <div class="span8 pull-left">
        <h1>${textContainer.text['miscellaneousMcpRecipesMainDescription'] }</h1>
        <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['mcpRecipesDescription'] }</p>
      </div>
      <c:if test="${grouperRequestContainer.mcpContainer.canOperateOnMcpRecipeConfigs}">
        <div class="span3 pull-right">
          <%@ include file="mcpRecipesMoreActionsButtonContents.jsp"%>
        </div>
      </c:if>
    </div>
  </div>
</div>

<%-- a recipe pointing at a tool which an upgrade removed, or naming a group which has since been
     deleted, looks fine in the table and does nothing at all.  nothing else reports it --%>
<c:if test="${not empty grouperRequestContainer.mcpContainer.mcpRecipeProblems}">
  <div class="row-fluid">
    <div class="alert alert-error">
      <strong>${textContainer.text['mcpRecipesProblemsHeader'] }</strong>
      <ul style="margin-top: 0.5em; margin-bottom: 0;">
        <c:forEach items="${grouperRequestContainer.mcpContainer.mcpRecipeProblems}" var="mcpRecipeProblem">
          <li>${grouper:escapeHtml(mcpRecipeProblem)}</li>
        </c:forEach>
      </ul>
    </div>
  </div>
</c:if>

<div class="row-fluid">

  <c:choose>
    <c:when test="${empty grouperRequestContainer.mcpContainer.guiMcpRecipeConfigurations}">
      <p>${textContainer.text['mcpRecipesNoneConfigured'] }</p>
    </c:when>
    <c:otherwise>

      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['mcpRecipesHeaderActions']}</th>
            <th>${textContainer.text['mcpRecipesHeaderConfigId']}</th>
            <th>${textContainer.text['mcpRecipesHeaderName']}</th>
            <%-- the summary is the point of a recipe, and this table has nine columns with three
                 of them nowrap, so without a stated share it is squeezed to a few characters and
                 wraps down the page.  claim a third of the table for it --%>
            <th style="width: 33%;">${textContainer.text['mcpRecipesHeaderSummary']}</th>
            <th>${textContainer.text['mcpRecipesHeaderToolNames']}</th>
            <th>${textContainer.text['mcpRecipesHeaderGroupCanUse']}</th>
            <th>${textContainer.text['mcpRecipesHeaderGroupCanEdit']}</th>
            <th>${textContainer.text['mcpRecipesHeaderEnabled']}</th>
            <th>${textContainer.text['mcpRecipesHeaderLastEdited']}</th>
          </tr>
        </thead>
        <tbody>
         <c:forEach items="${grouperRequestContainer.mcpContainer.guiMcpRecipeConfigurations}" var="guiMcpRecipeConfiguration" varStatus="loopStatus">

            <c:set var="mcpRecipeConfigId" value="${guiMcpRecipeConfiguration.grouperMcpRecipeConfiguration.configId}" />

            <tr>
              <td>
                <div class="btn-group">
                  <button type="button" data-toggle="dropdown"
                    aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}"
                    class="btn btn-mini dropdown-toggle"
                    aria-haspopup="true" aria-expanded="false"
                    onclick="$('#more-options${loopStatus.index}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${loopStatus.index} li').first().focus();return true;});">
                    ${textContainer.text['mcpRecipesRowActionsButton'] }
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu"
                    id="more-options${loopStatus.index}">

                    <%-- one edit screen for both audiences.  a content owner reaches the same
                         form and sees the administration fields read only, so there is no
                         second, lesser edit option to choose between --%>
                    <li><a href="?operation=UiV2Mcp.editMcpRecipe&mcpRecipeConfigId=${mcpRecipeConfigId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Mcp.editMcpRecipe&mcpRecipeConfigId=${mcpRecipeConfigId}');">${textContainer.text['mcpRecipesEditActionsOption'] }</a></li>

                    <%-- deleting a recipe is administration, never delegated --%>
                    <c:if test="${grouperRequestContainer.mcpContainer.canOperateOnMcpRecipeConfigs}">

                      <li class="divider"></li>
                      <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['mcpRecipesConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2Mcp.deleteMcpRecipe&mcpRecipeConfigId=${mcpRecipeConfigId}');}">${textContainer.text['mcpRecipesDeleteActionsOption'] }</a></li>
                    </c:if>

                  </ul>
                </div>
               </td>

              <td style="white-space: nowrap;">
                ${grouper:escapeHtml(mcpRecipeConfigId)}
              </td>

              <td style="white-space: nowrap;">
                ${grouper:escapeHtml(guiMcpRecipeConfiguration.name)}
              </td>

              <%-- the summary is what the client reads to decide whether the recipe applies, so
                   it is worth seeing at a glance, but a long one would push the rest of the row
                   off screen.  when it is cut short the full text goes in a tooltip on a span,
                   not a title on the td: grouper.js gives span[rel=tooltip] a tabindex, shows it
                   on focus and links it with aria-describedby, so the rest of the summary is
                   reachable by keyboard and announced, which a title on a non-focusable td is
                   not.  no tooltip when nothing was cut, so a table of short summaries does not
                   add a tab stop per row for text already fully on screen --%>
              <td>
                <c:choose>
                  <c:when test="${guiMcpRecipeConfiguration.summaryTruncated}">
                    <span rel="tooltip"
                      data-original-title="${grouper:escapeHtml(guiMcpRecipeConfiguration.summary)}">${grouper:escapeHtml(guiMcpRecipeConfiguration.summaryAbbreviated)}</span>
                  </c:when>
                  <c:otherwise>
                    ${grouper:escapeHtml(guiMcpRecipeConfiguration.summary)}
                  </c:otherwise>
                </c:choose>
              </td>

              <td>
                ${grouper:escapeHtml(guiMcpRecipeConfiguration.toolNames)}
              </td>

              <%-- short name and link, the way group names are shown elsewhere.  a recipe
                   resolves its groups as root, so it can name a group this reader cannot see;
                   in that case, and when the group has been deleted, there is no gui group and
                   the plain configured name is shown instead of a link which would error --%>
              <td>
                <c:choose>
                  <c:when test="${not empty guiMcpRecipeConfiguration.guiGroupCanUse}">
                    ${guiMcpRecipeConfiguration.guiGroupCanUse.shortLinkWithIcon}
                  </c:when>
                  <c:otherwise>
                    ${grouper:escapeHtml(guiMcpRecipeConfiguration.groupNameCanUse)}
                  </c:otherwise>
                </c:choose>
              </td>

              <td>
                <c:choose>
                  <c:when test="${not empty guiMcpRecipeConfiguration.guiGroupCanEdit}">
                    ${guiMcpRecipeConfiguration.guiGroupCanEdit.shortLinkWithIcon}
                  </c:when>
                  <c:otherwise>
                    ${grouper:escapeHtml(guiMcpRecipeConfiguration.groupNameCanEdit)}
                  </c:otherwise>
                </c:choose>
              </td>

              <td>
                <c:choose>
                  <c:when test="${guiMcpRecipeConfiguration.enabled}">
                    ${textContainer.text['mcpRecipesEnabledYes'] }
                  </c:when>
                  <c:otherwise>
                    ${textContainer.text['mcpRecipesEnabledNo'] }
                  </c:otherwise>
                </c:choose>
              </td>

              <%-- deliberately not nowrap.  this reads "Name (subjectId) on yyyy/MM/dd", around
                   forty characters, and a nowrap cell cannot shrink below its whole string.  In
                   auto table layout that is a hard minimum which beats the summary column's
                   stated width, so holding this on one line is what squeezed the summary --%>
              <td>
                ${grouper:escapeHtml(guiMcpRecipeConfiguration.lastEdited)}
              </td>
              </tr>

         </c:forEach>

        </tbody>
      </table>

    </c:otherwise>
  </c:choose>

</div>
