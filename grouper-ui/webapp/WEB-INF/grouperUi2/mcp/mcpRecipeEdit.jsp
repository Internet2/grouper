<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousMcpRecipeEditBreadcrumb')}

<c:set var="mcpRecipeConfigId" value="${grouperRequestContainer.mcpContainer.guiMcpRecipeConfiguration.grouperMcpRecipeConfiguration.configId}" />

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Mcp.viewMcpRecipes" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Mcp.viewMcpRecipes');">${textContainer.text['miscellaneousMcpRecipesBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>

    <li class="active">${textContainer.text['miscellaneousMcpRecipeEditBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">
    <div class="row-fluid">
      <%-- names this screen rather than the feature, so it is distinct from the add screen and
           from the edit wording screen when navigating by heading --%>
      <div class="span9 pull-left"><h1>${textContainer.text['miscellaneousMcpRecipeEditBreadcrumb'] }</h1></div>
    </div>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <form class="form-inline form-small form-filter" id="mcpRecipeDetails">
      <input type="hidden" name="previousMcpRecipeConfigId" value="${grouper:escapeHtml(mcpRecipeConfigId)}" />

      <table class="table table-condensed table-striped">
        <tbody>
          <tr>
            <%-- the config id is not editable here, so this is a caption for a value rather than
                 a form label.  a label element with nothing to point at is an orphaned label --%>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['mcpRecipeConfigIdLabel']}</strong></td>
            <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
            <td>
              ${grouper:escapeHtml(mcpRecipeConfigId)}
            </td>
          </tr>
        </tbody>

        <c:forEach items="${grouperRequestContainer.mcpContainer.guiMcpRecipeConfiguration.grouperMcpRecipeConfiguration.subSections}" var="subSection">
          <tbody>
            <c:if test="${!grouper:isBlank(subSection.label) and subSection.show }">
              <tr>
                <th colspan="3">
                  <h4>${subSection.title}</h4>
                  <p style="font-weight: normal;">${subSection.description} </p>
                </th>
              </tr>
            </c:if>

            <c:forEach items="${subSection.attributesValues}" var="attribute">

              <grouper:configFormElement
                formElementType="${attribute.formElement}"
                configId="${attribute.configSuffix}"
                label="${attribute.label}"
                readOnly="${attribute.readOnly}"
                helperText="${attribute.description}"
                helperTextDefaultValue="${attribute.defaultValue}"
                required="${attribute.required}"
                shouldShow="${attribute.show}"
                value="${attribute.valueOrExpressionEvaluation}"
                hasExpressionLanguage="${attribute.expressionLanguage}"
                ajaxCallback="ajax('../app/UiV2Mcp.editMcpRecipe?mcpRecipeConfigId=${mcpRecipeConfigId}', {formIds: 'mcpRecipeDetails'}); return false;"
                valuesAndLabels="${attribute.dropdownValuesAndLabels }"
                checkboxAttributes="${attribute.checkboxAttributes}"
                indent="${attribute.configItemMetadata.indent}"
              />

            </c:forEach>

          </tbody>
        </c:forEach>

      </table>

      <div class="span6">
        <input type="submit" class="btn btn-primary"
          aria-controls="mcpRecipeDetails" id="submitId"
          value="${textContainer.text['mcpRecipeAddFormSubmitButton'] }"
          onclick="ajax('../app/UiV2Mcp.editMcpRecipeSubmit?mcpRecipeConfigId=${mcpRecipeConfigId}', {formIds: 'mcpRecipeDetails'}); return false;">
          &nbsp;
        <button type="button" class="btn btn-cancel"
          onclick="return guiV2link('operation=UiV2Mcp.viewMcpRecipes'); return false;"
          >${textContainer.text['mcpRecipeAddFormCancelButton'] }</button>
      </div>

    </form>
  </div>
</div>
