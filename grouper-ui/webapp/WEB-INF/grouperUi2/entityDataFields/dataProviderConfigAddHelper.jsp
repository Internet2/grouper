<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.entityDataFieldsContainer.guiDataProviderConfiguration}" var="guiDataProviderConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="dataProviderConfigId">${textContainer.text['dataProviderConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiDataProviderConfiguration.grouperDataProviderConfiguration.configId)}"
         name="dataProviderConfigId" id="dataProviderConfigId" />
        <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right"
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      
      <br />
      <span class="description">${textContainer.text['dataProviderConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="dataProviderTypeId">${textContainer.text['dataProviderTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="dataProviderType" id="dataProviderTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2EntityDataFields.addDataProviderConfig', {formIds: 'dataProviderConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.allDataProviderTypes}" var="dataProvider">
          <option value="${dataProvider['class'].name}"
              ${guiDataProviderConfiguration.grouperDataProviderConfiguration['class'].name == dataProvider['class'].name ? 'selected="selected"' : '' }
              >${dataProvider.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['dataProviderTypeHint']}</span>
    </td>
  </tr>
    
  <c:forEach items="${guiDataProviderConfiguration.grouperDataProviderConfiguration.subSections}" var="subSection">
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
            ajaxCallback="ajax('../app/UiV2EntityDataFields.addDataProviderConfig', {formIds: 'dataProviderConfigDetails'}); return false;"
            valuesAndLabels="${attribute.dropdownValuesAndLabels }"
            checkboxAttributes="${attribute.checkboxAttributes}"
            indent="${attribute.configItemMetadata.indent}"
          />
          
        </c:forEach>
        
      </tbody>
  
  </c:forEach>
  
  
  
  
  