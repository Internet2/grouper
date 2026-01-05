<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.userLifecycleContainer.guiUserLifecyclePolicyConfiguration}" var="guiUserLifecyclePolicyConfig"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="dataFieldConfigId">${textContainer.text['dataFieldConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiUserLifecyclePolicyConfig.userLifecyclePolicyConfiguration.configId)}"
         name="userLifecyclePolicyConfigId" id="userLifecyclePolicyConfigId" />
        <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right"
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      
      <br />
      <span class="description">${textContainer.text['userLifecyclePolicyConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="dataFieldTypeId">${textContainer.text['userLifecyclePolicyTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="userLifecyclePolicyType" id="userLifecyclePolicyTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2UserLifecycle.addUserLifecyclePolicyConfiguration', {formIds: 'userLifecyclePoliciesConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.userLifecycleContainer.allUserLifecyclePolicyTypes}" var="userLifecyclePolicy">
          <option value="${userLifecyclePolicy['class'].name}"
              ${guiUserLifecyclePolicyConfig.userLifecyclePolicyConfiguration['class'].name == userLifecyclePolicy['class'].name ? 'selected="selected"' : '' }
              >${userLifecyclePolicy.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['userLifecyclePolicyTypeHint']}</span>
    </td>
  </tr>
    
  <c:forEach items="${guiUserLifecyclePolicyConfig.userLifecyclePolicyConfiguration.subSections}" var="subSection">
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
            ajaxCallback="ajax('../app/UiV2UserLifecycle.addUserLifecyclePolicyConfiguration', {formIds: 'userLifecyclePoliciesConfigDetails'}); return false;"
            valuesAndLabels="${attribute.dropdownValuesAndLabels }"
            checkboxAttributes="${attribute.checkboxAttributes}"
            indent="${attribute.configItemMetadata.indent}"
          />
          
        </c:forEach>
        
      </tbody>
  
  </c:forEach>
  
  
  
  
  