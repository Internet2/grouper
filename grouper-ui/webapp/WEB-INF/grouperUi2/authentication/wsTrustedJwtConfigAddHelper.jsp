<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.authenticationContainer.guiWsTrustedJwtConfiguration}" var="guiWsTrustedJwtConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="wsTrustedJwtConfigId">${textContainer.text['wsTrustedJwtConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.configId)}"
         name="wsTrustedJwtConfigId" id="configId" />
        <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      
      <br />
      <span class="description">${textContainer.text['wsTrustedJwtConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="wsTrustedJwtConfigTypeId">${textContainer.text['wsTrustedJwtConfigTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="wsTrustedJwtConfigType" id="wsTrustedJwtConfigTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2AuthenticationConfig.addWsTrustedJwt', {formIds: 'wsTrustedJwtConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.authenticationContainer.allWsTrustedJwtConfigTypes}" var="wsTrustedJwtConfig">
          <option value="${wsTrustedJwtConfig['class'].name}"
              ${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration['class'].name == wsTrustedJwtConfig['class'].name ? 'selected="selected"' : '' }
              >${wsTrustedJwtConfig.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['wsTrustedJwtConfigTypeHint']}</span>
    </td>
  </tr>
    
  <c:forEach items="${guiWsTrustedJwtConfiguration.wsTrustedJwtConfiguration.subSections}" var="subSection">
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
        
          <c:set target="${grouperRequestContainer.authenticationContainer}"
                  property="index"
                  value="${attribute.repeatGroupIndex}" />
                  
            <c:set target="${grouperRequestContainer.authenticationContainer}"
                  property="currentConfigSuffix"
                  value="${attribute.configSuffix}" />
          
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
            ajaxCallback="ajax('../app/UiV2AuthenticationConfig.addWsTrustedJwt', {formIds: 'wsTrustedJwtConfigDetails'}); return false;"
            valuesAndLabels="${attribute.dropdownValuesAndLabels }"
            checkboxAttributes="${attribute.checkboxAttributes}"
            indent="${attribute.configItemMetadata.indent}"
          />
          
        </c:forEach>
        
      </tbody>
  
  </c:forEach>
  
  
  
  
  