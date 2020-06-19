<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set  value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap; width: 30%;"><strong><label for="provisionerConfigId">${textContainer.text['provisionerConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap; width: 5%;">&nbsp;</td>
    <td>
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiProvisionerConfiguration.provisionerConfiguration.configId)}"
         name="provisionerConfigId" id="provisionerConfigId" />
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['provisionerConfigIdHint']}</span>
    </td>
  </tr>
  	
  	<tr>
    <td style="vertical-align: top; white-space: nowrap; width: 30%;"><strong><label for="provisionerConfigTypeId">${textContainer.text['provisionerTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap; width: 5%;">&nbsp;</td>
    <td>
      <select name="provisionerConfigType" id="provisionerConfigTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration', {formIds: 'provisionerConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.allProvisionerConfigurationTypes}" var="provisionerConfiguration">
          <option value="${provisionerConfiguration['class'].name}"
              ${guiProvisionerConfiguration.provisionerConfiguration['class'].name == provisionerConfiguration['class'].name ? 'selected="selected"' : '' }
              >${provisionerConfiguration.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['provisionerTypeHint']}</span>
    </td>
  </tr>
  
  <c:forEach items="${guiProvisionerConfiguration.provisionerConfiguration.subSections}" var="subSection">
  		<tbody>
  			<c:if test="${!grouper:isBlank(subSection.label)}">
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
  					configId="${attribute.configSuffix}" label="${attribute.label}"
  					helperText="${attribute.description}"
  					helperTextDefaultValue="${attribute.defaultValue}"
  					required="${attribute.required}"
  					shouldShow="${attribute.show}"
  					value="${attribute.valueOrExpressionEvaluation}"
  					hasExpressionLanguage="${attribute.expressionLanguage}"
  					ajaxCallback="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration?provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}', {formIds: 'provisionerConfigDetails'}); return false;"
  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
  					checkboxAttributes="${attribute.checkboxAttributes}"
  				/>
  				
  			</c:forEach>
  			
  		</tbody>
  
  </c:forEach>
  