<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.globalAttributeResolverConfigContainer.guiGlobalAttributeResolverConfiguration}" var="guiGlobalAttributeResolverConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="globalAttributeResolverConfigId">${textContainer.text['globalAttributeResolverConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.configId)}"
         name="globalAttributeResolverConfigId" id="globalAttributeResolverConfigId" />
        <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      
      <br />
      <span class="description">${textContainer.text['globalAttributeResolverConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="globalAttributeResolverConfigTypeId">${textContainer.text['globalAttributeResolverConfigTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="globalAttributeResolverConfigType" id="globalAttributeResolverConfigTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2GlobalAttributeResolverConfig.addGlobalAttributeResolverConfig', {formIds: 'globalAttributeResolverConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.globalAttributeResolverConfigContainer.allGlobalAttributeResolverConfigTypes}" var="globalAttributeResolverConfiguration">
          <option value="${globalAttributeResolverConfiguration['class'].name}"
              ${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration['class'].name == globalAttributeResolverConfiguration['class'].name ? 'selected="selected"' : '' }
              >${globalAttributeResolverConfiguration.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['globalAttributeResolverConfigTypeHint']}</span>
    </td>
  </tr>
    
  <c:forEach items="${guiGlobalAttributeResolverConfiguration.globalAttributeResolverConfiguration.subSections}" var="subSection">
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
        
          <c:set target="${grouperRequestContainer.globalAttributeResolverConfigContainer}"
                  property="index"
                  value="${attribute.repeatGroupIndex}" />
                  
            <c:set target="${grouperRequestContainer.globalAttributeResolverConfigContainer}"
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
            ajaxCallback="ajax('../app/UiV2GlobalAttributeResolverConfig.addGlobalAttributeResolverConfig', {formIds: 'globalAttributeResolverConfigDetails'}); return false;"
            valuesAndLabels="${attribute.dropdownValuesAndLabels }"
            checkboxAttributes="${attribute.checkboxAttributes}"
          />
          
        </c:forEach>
        
      </tbody>
  
  </c:forEach>
  
  
  
  
  