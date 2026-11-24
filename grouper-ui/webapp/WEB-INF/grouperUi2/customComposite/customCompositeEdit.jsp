<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<c:set var="customComposite" value="${grouperRequestContainer.customCompositeContainer.customComposite}" />
<%-- adding when there is no composite yet.  the uiKey is fixed once set, since it is also the
     text key of the label --%>
<c:set var="isAdd" value="${customComposite == null}" />
<c:set var="uiKeyFixed" value="${!isAdd && !grouper:isBlank(customComposite.uiKey)}" />
<c:set var="labelReadOnly" value="${uiKeyFixed && !customComposite.labelSource.editable}" />
<c:set var="editable" value="${isAdd || customComposite.editable}" />
<c:set var="titleKey" value="${isAdd ? 'customCompositeAddTitle' : 'customCompositeEditTitle'}" />
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

<div class="row-fluid">
  <form class="form-inline form-small form-filter" id="customCompositeForm">
    <c:if test="${!isAdd}">
      <input type="hidden" name="customCompositeIndex" value="${customComposite.index}" />
    </c:if>
    <table class="table table-condensed table-striped">
      <tbody>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="customCompositeUiKey">${textContainer.text['customCompositeUiKeyLabel'] }</label></strong></td>
          <td>
            <input type="text" id="customCompositeUiKey" name="customCompositeUiKey" style="width: 30em" aria-required="true"
              value="${grouper:escapeHtml(customComposite.uiKey)}" ${uiKeyFixed || !editable ? 'readonly="readonly"' : ''} />
            <br /><span class="description">${textContainer.text['customCompositeUiKeyDescription'] }</span>
          </td>
        </tr>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="customCompositeLabel">${textContainer.text['customCompositeLabelLabel'] }</label></strong></td>
          <td>
            <input type="text" id="customCompositeLabel" name="customCompositeLabel" style="width: 30em" aria-required="true"
              value="${grouper:escapeHtml(customComposite.label)}" ${labelReadOnly || !editable ? 'readonly="readonly"' : ''} />
            <br /><span class="description">${textContainer.text['customCompositeLabelDescription'] }</span>
          </td>
        </tr>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="customCompositeCompositeType">${textContainer.text['customCompositeCompositeTypeLabel'] }</label></strong></td>
          <td>
            <select id="customCompositeCompositeType" name="customCompositeCompositeType" ${editable ? '' : 'disabled="disabled"'}>
              <option value="complement" ${customComposite.compositeType == 'complement' ? 'selected="selected"' : ''}>${textContainer.text['customCompositeCompositeTypeComplement'] }</option>
              <option value="intersection" ${customComposite.compositeType == 'intersection' ? 'selected="selected"' : ''}>${textContainer.text['customCompositeCompositeTypeIntersection'] }</option>
            </select>
            <br /><span class="description">${textContainer.text['customCompositeCompositeTypeDescription'] }</span>
          </td>
        </tr>
        <%-- the same group picker as the membership requirement form: posts config_groupNameName, and
             shows the configured name even when that group no longer exists --%>
        <grouper:configFormElement
          formElementType="GROUPCOMBOBOX"
          configId="groupName"
          label="${textContainer.text['customCompositeGroupNameLabel']}"
          readOnly="${!editable}"
          helperText="${textContainer.text['customCompositeGroupNameDescription']}"
          required="false"
          shouldShow="true"
          value="${customComposite.groupName}"
          hasExpressionLanguage="false"
          shouldShowElCheckbox="false"
          ajaxCallback="return false;"
        />
      </tbody>
    </table>
    <p>${textContainer.text['customCompositeSaveNote'] }</p>
    <c:if test="${editable}">
      <input type="submit" class="btn btn-primary" value="${textContainer.text['customCompositeSaveButton'] }"
        onclick="ajax('../app/UiV2CustomComposite.saveCustomComposite', {formIds: 'customCompositeForm'}); return false;" />
      &nbsp;
    </c:if>
    <button type="button" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2CustomComposite.index');">${textContainer.text['customCompositeCancelButton'] }</button>
  </form>
</div>
