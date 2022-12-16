<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.entityDataFieldsContainer.guiDataRowConfiguration}" var="guiDataRowConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="dataRowConfigId">${textContainer.text['dataRowConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiDataRowConfiguration.grouperDataRowConfiguration.configId)}"
         name="dataRowConfigId" id="dataRowConfigId" />
        <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right"
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      
      <br />
      <span class="description">${textContainer.text['dataRowConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="dataRowTypeId">${textContainer.text['dataRowTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="dataRowType" id="dataRowTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2EntityDataFields.addDataRowConfiguration', {formIds: 'dataRowConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.allDataRowTypes}" var="dataRow">
          <option value="${dataRow['class'].name}"
              ${guiDataRowConfiguration.grouperDataRowConfiguration['class'].name == dataRow['class'].name ? 'selected="selected"' : '' }
              >${dataRow.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['dataRowTypeHint']}</span>
    </td>
  </tr>
    
  <c:forEach items="${guiDataRowConfiguration.grouperDataRowConfiguration.subSections}" var="subSection">
      <tbody>
        <c:if test="${!grouper:isBlank(subSection.label) and subSection.show}">
          <tr>
            <th colspan="3">
              <h4>${subSection.title}</h4>
              <p style="font-weight: normal;">${subSection.description} </p>
            </th>
          </tr>
        
        </c:if>
        
        <c:forEach items="${subSection.attributesValues}" var="attribute">
        
          <%-- <c:set target="${grouperRequestContainer.sqlSyncConfigurationContainer}"
                  property="index"
                  value="${attribute.repeatGroupIndex}" /> --%>
                  
            <%-- <c:set target="${grouperRequestContainer.sqlSyncConfigurationContainer}"
                  property="currentConfigSuffix"
                  value="${attribute.configSuffix}" /> --%>
          
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
            ajaxCallback="ajax('../app/UiV2EntityDataFields.addDataRowConfiguration', {formIds: 'dataRowConfigDetails'}); return false;"
            valuesAndLabels="${attribute.dropdownValuesAndLabels }"
            checkboxAttributes="${attribute.checkboxAttributes}"
            indent="${attribute.configItemMetadata.indent}"
          />
          
        </c:forEach>
        
      </tbody>
  
  </c:forEach>
  
  
  
  
  