<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.customUiContainer.guiCustomUiConfiguration}" var="guiCustomUiConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="customUiConfigId">${textContainer.text['gshTemplateConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
    	
   		<input type="text" style="width: 30em" value="${grouper:escapeHtml(guiCustomUiConfiguration.customUiConfiguration.configId)}"
         name="customUiConfigId" id="customUiConfigId" />
        <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
    	
      <br />
      <span class="description">${textContainer.text['customUiConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="customUiTypeId">${textContainer.text['customUiTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="customUiType" id="customUiTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2CustomUiConfig.addCustomUiConfig', {formIds: 'customUiConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.customUiContainer.allCustomUiTypes}" var="customUi">
          <option value="${customUi['class'].name}"
              ${guiCustomUiConfiguration.customUiConfiguration['class'].name == customUi['class'].name ? 'selected="selected"' : '' }
              >${customUi.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['customUiTypeHint']}</span>
    </td>
  </tr>
  	
  <c:forEach items="${guiCustomUiConfiguration.customUiConfiguration.subSections}" var="subSection">
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
  			
  				<c:set target="${grouperRequestContainer.customUiContainer}"
	               	property="index"
	               	value="${attribute.repeatGroupIndex}" />
               		
        		<c:set target="${grouperRequestContainer.customUiContainer}"
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
  					ajaxCallback="ajax('../app/UiV2CustomUiConfig.addCustomUiConfig', {formIds: 'customUiConfigDetails'}); return false;"
  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
  					checkboxAttributes="${attribute.checkboxAttributes}"
            indent="${attribute.configItemMetadata.indent}"
  				/>
  				
  			</c:forEach>
  			
  		</tbody>
  
  </c:forEach>
  
  
  
  
  