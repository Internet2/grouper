<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<c:set var="membershipRequirement" value="${grouperRequestContainer.customCompositeContainer.membershipRequirement}" />
<%-- the requirement's grouper.properties keys are a configuration module, drawn with the generic form
     from their metadata.  null on the add screen until a config id is entered --%>
<c:set var="membershipRequireConfiguration" value="${grouperRequestContainer.customCompositeContainer.membershipRequireConfiguration}" />
<%-- the fields which are not in the module: texts, how the attribute is chosen, the group screen --%>
<c:set var="form" value="${grouperRequestContainer.customCompositeContainer.membershipRequirementForm}" />
<%-- the config id, uiKey and attribute are fixed once set.  the uiKey is also the text key of the
     label and part of the veto message's text key --%>
<c:set var="isAdd" value="${membershipRequirement == null}" />
<c:set var="uiKeyFixed" value="${!isAdd && !grouper:isBlank(membershipRequirement.uiKey)}" />
<c:set var="vetoMessageReadOnly" value="${uiKeyFixed && !membershipRequirement.vetoMessageSource.editable}" />
<c:set var="attributeFixed" value="${!isAdd && !grouper:isBlank(membershipRequirement.attributeName)}" />
<c:set var="editable" value="${isAdd || membershipRequirement.editable}" />
<c:set var="titleKey" value="${isAdd ? 'membershipRequirementAddTitle' : 'membershipRequirementEditTitle'}" />
<%-- the add form posts back to itself as the config id is entered, which draws the module's fields.
     the edit form does not need to --%>
<c:choose>
  <c:when test="${isAdd}">
    <c:set var="ajaxCallback">ajax('../app/UiV2CustomComposite.addMembershipRequirement', {formIds: 'membershipRequirementForm'}); return false;</c:set>
  </c:when>
  <c:otherwise>
    <c:set var="ajaxCallback">return false;</c:set>
  </c:otherwise>
</c:choose>
${grouper:title(titleKey)}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2CustomComposite.index" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2CustomComposite.index');">${textContainer.text['customCompositeIndexBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li class="active">${textContainer.text[titleKey] }</li>
  </ul>
  <div class="page-header blue-gradient">
    <h1>${textContainer.text[titleKey] }</h1>
  </div>
</div>

<c:if test="${!editable}">
  <div class="alert alert-info">${textContainer.text['customCompositeReadOnlyNote'] }</div>
</c:if>

<c:if test="${!isAdd}">
  <%-- the engine skips a requirement with any of these, with only an error in the log --%>
  <c:set var="problems" value="${membershipRequirement.retrieveProblems()}" />
  <c:if test="${not empty problems}">
    <div class="alert alert-error">
      <strong>${textContainer.text['membershipRequirementProblemsHeader'] }</strong>
      <ul style="margin-top: 0.5em; margin-bottom: 0;">
        <c:forEach items="${problems}" var="problem">
          <li>${grouper:escapeHtml(problem)}</li>
        </c:forEach>
      </ul>
    </div>
  </c:if>
  <%-- missing texts do not stop the requirement, so these are warnings, not reasons it is skipped --%>
  <c:set var="warnings" value="${membershipRequirement.retrieveWarnings()}" />
  <c:if test="${not empty warnings}">
    <div class="alert">
      <strong>${textContainer.text['membershipRequirementWarningsHeader'] }</strong>
      <ul style="margin-top: 0.5em; margin-bottom: 0;">
        <c:forEach items="${warnings}" var="warning">
          <li>${grouper:escapeHtml(warning)}</li>
        </c:forEach>
      </ul>
    </div>
  </c:if>
</c:if>

<div class="row-fluid">
  <form class="form-inline form-small form-filter" id="membershipRequirementForm">
    <input type="hidden" name="membershipRequirementIsNew" value="${isAdd ? 'true' : 'false'}" />
    <table class="table table-condensed table-striped">
      <tbody>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="membershipRequirementConfigId">${textContainer.text['membershipRequirementConfigIdLabel'] }</label></strong></td>
          <td>
            <c:choose>
              <c:when test="${isAdd}">
                <%-- what was typed, so a config id which was refused stays in the field to fix --%>
                <input type="text" id="membershipRequirementConfigId" name="membershipRequirementConfigId" style="width: 30em" aria-required="true"
                  value="${grouper:escapeHtml(form['configId'])}"
                  onchange="ajax('../app/UiV2CustomComposite.addMembershipRequirement', {formIds: 'membershipRequirementForm'}); return false;" />
              </c:when>
              <c:otherwise>
                <input type="text" id="membershipRequirementConfigId" name="membershipRequirementConfigId" style="width: 30em"
                  value="${grouper:escapeHtml(membershipRequirement.configId)}" readonly="readonly" />
              </c:otherwise>
            </c:choose>
            <br /><span class="description">${textContainer.text['membershipRequirementConfigIdDescription'] }</span>
          </td>
        </tr>
      </tbody>

      <c:if test="${membershipRequireConfiguration != null}">
        <tbody>
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="membershipRequirementVetoMessage">${textContainer.text['membershipRequirementVetoMessageLabel'] }</label></strong></td>
            <td>
              <textarea id="membershipRequirementVetoMessage" name="membershipRequirementVetoMessage" rows="3" style="width: 30em" aria-required="true"
                ${vetoMessageReadOnly || !editable ? 'readonly="readonly"' : ''}>${grouper:escapeHtml(form['vetoMessage'])}</textarea>
              <br /><span class="description">${textContainer.text['membershipRequirementVetoMessageDescription'] }</span>
            </td>
          </tr>
        </tbody>

        <%-- uiKey, requireGroupName, hookEnable, sourceIdsToIgnore.  attributeName is drawn below, with
             the choice of how the attribute is chosen and the definition name --%>
        <c:forEach items="${membershipRequireConfiguration.subSections}" var="subSection">
          <tbody>
            <c:forEach items="${subSection.attributesValues}" var="attribute">
              <c:if test="${attribute.configSuffix != 'attributeName'}">
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
                shouldShowElCheckbox="false"
                ajaxCallback="${ajaxCallback}"
                valuesAndLabels="${attribute.dropdownValuesAndLabels }"
                checkboxAttributes="${attribute.checkboxAttributes}"
                indent="${attribute.configItemMetadata.indent}"
              />
              </c:if>
            </c:forEach>
          </tbody>
        </c:forEach>

        <tbody>
          <c:if test="${!isAdd}">
            <%-- blank exempt sources means the setting for all requirements, not nobody --%>
            <tr>
              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['membershipRequirementSourceIdsToIgnoreEffectiveLabel'] }</strong></td>
              <td>${grouper:isBlank(membershipRequirement.sourceIdsToIgnoreEffective) ? textContainer.text['membershipRequirementSourceIdsToIgnoreNone'] : grouper:escapeHtml(membershipRequirement.sourceIdsToIgnoreEffective)}</td>
            </tr>
          </c:if>
          <%-- the attribute: how it is chosen, its name, and the definition, together.  the name is the
               module's field, drawn here rather than in the loop above --%>
          <c:set var="attributeNameAttribute" value="${membershipRequireConfiguration.retrieveAttributes()['attributeName']}" />
          <c:choose>
            <c:when test="${attributeFixed}">
              <grouper:configFormElement
                formElementType="${attributeNameAttribute.formElement}"
                configId="${attributeNameAttribute.configSuffix}"
                label="${attributeNameAttribute.label}"
                readOnly="${attributeNameAttribute.readOnly}"
                helperText="${attributeNameAttribute.description}"
                helperTextDefaultValue="${attributeNameAttribute.defaultValue}"
                required="${attributeNameAttribute.required}"
                shouldShow="${attributeNameAttribute.show}"
                value="${attributeNameAttribute.valueOrExpressionEvaluation}"
                hasExpressionLanguage="${attributeNameAttribute.expressionLanguage}"
                shouldShowElCheckbox="false"
                ajaxCallback="${ajaxCallback}"
                valuesAndLabels="${attributeNameAttribute.dropdownValuesAndLabels }"
                checkboxAttributes="${attributeNameAttribute.checkboxAttributes}"
                indent="${attributeNameAttribute.configItemMetadata.indent}"
              />
              <%-- the attribute is fixed once a requirement has one, like the config id.  where it is
                   assigned, and who can assign it, are on the attribute's own screens --%>
              <tr>
                <td></td>
                <td>
                  <c:set var="attributeDefName" value="${membershipRequirement.retrieveAttributeDefName()}" />
                  <c:if test="${attributeDefName != null}">
                    <a href="?operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=${attributeDefName.id}"
                      onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=${attributeDefName.id}');"
                      >${textContainer.text['membershipRequirementAttributeLink'] }</a>
                  </c:if>
                </td>
              </tr>
            </c:when>
            <c:otherwise>
              <tr>
                <td style="vertical-align: top; white-space: nowrap;"><strong><span id="membershipRequirementAttributeOption">${textContainer.text['membershipRequirementAttributeOptionLabel'] }</span></strong></td>
                <td>
                  <%-- the definition is only used when the attribute is created, so it is hidden for an existing one --%>
                  <label class="radio"><input type="radio" name="membershipRequirementAttributeOption" value="existing"
                    onchange="$('#membershipRequirementAttributeDefNameRow').toggle(!this.checked);"
                    ${form['attributeOption'] == 'existing' ? 'checked="checked"' : ''} ${editable ? '' : 'disabled="disabled"'} /> ${textContainer.text['membershipRequirementAttributeOptionExisting'] }</label><br />
                  <label class="radio"><input type="radio" name="membershipRequirementAttributeOption" value="newNameInExistingDef"
                    onchange="$('#membershipRequirementAttributeDefNameRow').toggle(this.checked);"
                    ${form['attributeOption'] == 'newNameInExistingDef' ? 'checked="checked"' : ''} ${editable ? '' : 'disabled="disabled"'} /> ${textContainer.text['membershipRequirementAttributeOptionNewNameInExistingDef'] }</label><br />
                  <label class="radio"><input type="radio" name="membershipRequirementAttributeOption" value="newNameAndDef"
                    onchange="$('#membershipRequirementAttributeDefNameRow').toggle(this.checked);"
                    ${form['attributeOption'] == 'newNameAndDef' ? 'checked="checked"' : ''} ${editable ? '' : 'disabled="disabled"'} /> ${textContainer.text['membershipRequirementAttributeOptionNewNameAndDef'] }</label>
                  <br /><span class="description">${textContainer.text['membershipRequirementAttributeOptionDescription'] }</span>
                </td>
              </tr>
              <%-- the definition first, since the attribute is created in it --%>
              <tr id="membershipRequirementAttributeDefNameRow" ${form['attributeOption'] == 'existing' ? 'style="display: none;"' : ''}>
                <td style="vertical-align: top; white-space: nowrap;"><strong><label for="membershipRequirementAttributeDefName">${textContainer.text['membershipRequirementAttributeDefNameLabel'] }</label></strong></td>
                <td>
                  <input type="text" id="membershipRequirementAttributeDefName" name="membershipRequirementAttributeDefName" style="width: 40em"
                    value="${grouper:escapeHtml(form['attributeDefName'])}" ${editable ? '' : 'readonly="readonly"'} />
                  <br /><span class="description">${textContainer.text['membershipRequirementAttributeDefNameDescription'] }</span>
                </td>
              </tr>
              <grouper:configFormElement
                formElementType="${attributeNameAttribute.formElement}"
                configId="${attributeNameAttribute.configSuffix}"
                label="${attributeNameAttribute.label}"
                readOnly="${attributeNameAttribute.readOnly}"
                helperText="${attributeNameAttribute.description}"
                helperTextDefaultValue="${attributeNameAttribute.defaultValue}"
                required="${attributeNameAttribute.required}"
                shouldShow="${attributeNameAttribute.show}"
                value="${attributeNameAttribute.valueOrExpressionEvaluation}"
                hasExpressionLanguage="${attributeNameAttribute.expressionLanguage}"
                shouldShowElCheckbox="false"
                ajaxCallback="${ajaxCallback}"
                valuesAndLabels="${attributeNameAttribute.dropdownValuesAndLabels }"
                checkboxAttributes="${attributeNameAttribute.checkboxAttributes}"
                indent="${attributeNameAttribute.configItemMetadata.indent}"
              />
            </c:otherwise>
          </c:choose>

          <%-- the group screen entry (groupScreen.attribute.*) is read only if it is set in a config file --%>
          <c:set var="groupScreenReadOnly" value="${!editable || (!isAdd && !membershipRequirement.groupScreenSource.editable)}" />
          <%-- the label, description and order only apply when the box is ticked, so they are shown only then --%>
          <c:set var="groupScreenFieldsStyle" value="${form['showOnGroupScreen'] == 'true' ? '' : 'display: none;'}" />
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="membershipRequirementShowOnGroupScreen">${textContainer.text['membershipRequirementShowOnGroupScreenLabel'] }</label></strong></td>
            <td>
              <input type="checkbox" id="membershipRequirementShowOnGroupScreen" name="membershipRequirementShowOnGroupScreen" value="true"
                onchange="$('.membershipRequirementGroupScreenField').toggle(this.checked);"
                ${form['showOnGroupScreen'] == 'true' ? 'checked="checked"' : ''} ${groupScreenReadOnly ? 'disabled="disabled"' : ''} />
              <br /><span class="description">${textContainer.text['membershipRequirementShowOnGroupScreenDescription'] }</span>
              <c:if test="${!isAdd && membershipRequirement.groupScreenDisabledGlobally}">
                <br /><span class="description">${textContainer.text['membershipRequirementGroupScreenDisabledGlobally'] }</span>
              </c:if>
            </td>
          </tr>
          <tr class="membershipRequirementGroupScreenField" style="${groupScreenFieldsStyle}">
            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="membershipRequirementGroupScreenLabel">${textContainer.text['membershipRequirementGroupScreenLabelLabel'] }</label></strong></td>
            <td>
              <input type="text" id="membershipRequirementGroupScreenLabel" name="membershipRequirementGroupScreenLabel" style="width: 30em"
                value="${grouper:escapeHtml(form['groupScreenLabel'])}" ${groupScreenReadOnly ? 'readonly="readonly"' : ''} />
              <br /><span class="description">${textContainer.text['membershipRequirementGroupScreenLabelDescription'] }</span>
            </td>
          </tr>
          <tr class="membershipRequirementGroupScreenField" style="${groupScreenFieldsStyle}">
            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="membershipRequirementGroupScreenDescription">${textContainer.text['membershipRequirementGroupScreenDescriptionLabel'] }</label></strong></td>
            <td>
              <input type="text" id="membershipRequirementGroupScreenDescription" name="membershipRequirementGroupScreenDescription" style="width: 40em"
                value="${grouper:escapeHtml(form['groupScreenDescription'])}" ${groupScreenReadOnly ? 'readonly="readonly"' : ''} />
              <br /><span class="description">${textContainer.text['membershipRequirementGroupScreenDescriptionDescription'] }</span>
            </td>
          </tr>
          <tr class="membershipRequirementGroupScreenField" style="${groupScreenFieldsStyle}">
            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="membershipRequirementGroupScreenIndex">${textContainer.text['membershipRequirementGroupScreenIndexLabel'] }</label></strong></td>
            <td>
              <input type="text" id="membershipRequirementGroupScreenIndex" name="membershipRequirementGroupScreenIndex" style="width: 6em"
                value="${grouper:escapeHtml(form['groupScreenIndex'])}" ${groupScreenReadOnly ? 'readonly="readonly"' : ''} />
              <br /><span class="description">${textContainer.text['membershipRequirementGroupScreenIndexDescription'] }</span>
            </td>
          </tr>
        </tbody>
      </c:if>
    </table>
    <c:if test="${membershipRequireConfiguration != null}">
      <p>${textContainer.text['membershipRequirementSaveNote'] }</p>
      <c:if test="${editable}">
        <input type="submit" class="btn btn-primary" value="${textContainer.text['customCompositeSaveButton'] }"
          onclick="ajax('../app/UiV2CustomComposite.saveMembershipRequirement', {formIds: 'membershipRequirementForm'}); return false;" />
        &nbsp;
      </c:if>
    </c:if>
    <button type="button" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2CustomComposite.index');">${textContainer.text['customCompositeCancelButton'] }</button>
  </form>
</div>
