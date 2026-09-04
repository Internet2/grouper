<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousMcpRecipeAddBreadcrumb')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Mcp.viewMcpRecipes" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Mcp.viewMcpRecipes');">${textContainer.text['miscellaneousMcpRecipesBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>

    <li class="active">${textContainer.text['miscellaneousMcpRecipeAddBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">
    <div class="row-fluid">
      <%-- the heading names this screen rather than the feature, so the add, edit and edit
           wording screens are told apart by screen reader users navigating by heading --%>
      <div class="span9 pull-left"><h1>${textContainer.text['miscellaneousMcpRecipeAddBreadcrumb'] }</h1></div>
    </div>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <form class="form-inline form-small form-filter" id="mcpRecipeDetails">

      <table class="table table-condensed table-striped">
        <tbody>
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="mcpRecipeConfigId">${textContainer.text['mcpRecipeConfigIdLabel']}</label></strong></td>
            <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
            <td>
              <%-- posting back on change is what draws the rest of the form.  the fields below
                   come from the configuration module, which needs a config id before it can
                   build them, so without this the screen would offer nothing but this box --%>
              <%-- the * beside the field is reachable and announced (grouper.js gives
                   span[rel=tooltip] a tabindex and an aria-describedby), but that is a separate
                   tab stop after the field.  aria-required puts the requirement on the field
                   itself, so it is announced when focus lands there --%>
              <input type="text" id="mcpRecipeConfigId" name="mcpRecipeConfigId" style="width: 30em"
                aria-required="true"
                value="${grouper:escapeHtml(grouperRequestContainer.mcpContainer.guiMcpRecipeConfiguration.grouperMcpRecipeConfiguration.configId)}"
                onchange="ajax('../app/UiV2Mcp.addMcpRecipe', {formIds: 'mcpRecipeDetails'}); return false;" />
              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right"
                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
              <span class="help-block">${textContainer.text['mcpRecipeConfigIdDescription']}</span>
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
                ajaxCallback="ajax('../app/UiV2Mcp.addMcpRecipe', {formIds: 'mcpRecipeDetails'}); return false;"
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
          onclick="ajax('../app/UiV2Mcp.addMcpRecipeSubmit', {formIds: 'mcpRecipeDetails'}); return false;">
          &nbsp;
        <button type="button" class="btn btn-cancel"
          onclick="return guiV2link('operation=UiV2Mcp.viewMcpRecipes'); return false;"
          >${textContainer.text['mcpRecipeAddFormCancelButton'] }</button>
      </div>

    </form>
  </div>
</div>
