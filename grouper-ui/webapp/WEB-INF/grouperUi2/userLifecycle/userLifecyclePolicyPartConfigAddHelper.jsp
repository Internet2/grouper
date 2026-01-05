<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.userLifecycleContainer.guiUserLifecyclePolicyPartConfiguration}" var="guiUserLifecyclePolicyPartConfig"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="dataFieldConfigId">${textContainer.text['dataFieldConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiUserLifecyclePolicyPartConfig.userLifecyclePolicyPartConfiguration.configId)}"
         name="userLifecyclePolicyPartConfigId" id="userLifecyclePolicyPartConfigId" />
        <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right"
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      
      <br />
      <span class="description">${textContainer.text['userLifecyclePolicyPartConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="dataFieldTypeId">${textContainer.text['userLifecyclePolicyPartTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="userLifecyclePolicyPartType" id="userLifecyclePolicyPartTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2UserLifecycle.addUserLifecyclePolicyPartConfiguration', {formIds: 'userLifecyclePolicyPartsConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.userLifecycleContainer.allUserLifecyclePolicyPartTypes}" var="userLifecyclePolicyPart">
          <option value="${userLifecyclePolicyPart['class'].name}"
              ${guiUserLifecyclePolicyPartConfig.userLifecyclePolicyPartConfiguration['class'].name == userLifecyclePolicyPart['class'].name ? 'selected="selected"' : '' }
              >${userLifecyclePolicyPart.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['userLifecyclePolicyPartTypeHint']}</span>
    </td>
  </tr>
    
  <c:forEach items="${guiUserLifecyclePolicyPartConfig.userLifecyclePolicyPartConfiguration.subSections}" var="subSection">
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
            ajaxCallback="ajax('../app/UiV2UserLifecycle.addUserLifecyclePolicyPartConfiguration', {formIds: 'userLifecyclePolicyPartsConfigDetails'}); return false;"
            valuesAndLabels="${attribute.dropdownValuesAndLabels }"
            checkboxAttributes="${attribute.checkboxAttributes}"
            indent="${attribute.configItemMetadata.indent}"
          />
          
        </c:forEach>
        
      </tbody>
  
  </c:forEach>
  
  
  
  
  