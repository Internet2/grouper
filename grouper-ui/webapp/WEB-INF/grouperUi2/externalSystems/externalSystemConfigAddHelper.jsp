<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set  value="${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem}" var="guiGrouperExternalSystem"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="externalSystemConfigId">${textContainer.text['grouperExternalSystemConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiGrouperExternalSystem.grouperExternalSystem.configId)}"
         name="externalSystemConfigId" id="configId" />
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperExternalSystemConfigIdHint']}</span>
    </td>
  </tr>
  	
  	<tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="externalSystemTypeId">${textContainer.text['grouperExternalSystemTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="externalSystemType" id="externalSystemTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2ExternalSystem.addExternalSystem', {formIds: 'externalSystemConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.externalSystemContainer.allExternalSystemTypesAdd}" var="externalSystem">
          <option value="${externalSystem['class'].name}"
              ${guiGrouperExternalSystem.grouperExternalSystem['class'].name == externalSystem['class'].name ? 'selected="selected"' : '' }
              >${externalSystem.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperExternalSystemTypeHint']}</span>
    </td>
  </tr>
  
  <c:forEach items="${guiGrouperExternalSystem.grouperExternalSystem.subSections}" var="subSection">
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
  					configId="${attribute.configSuffix}" 
  					label="${attribute.label}"
  					readOnly="${attribute.readOnly}"
  					helperText="${attribute.description}"
  					helperTextDefaultValue="${attribute.defaultValue}"
  					required="${attribute.required}"
  					shouldShow="${attribute.show}"
  					value="${attribute.valueOrExpressionEvaluation}"
  					hasExpressionLanguage="${attribute.expressionLanguage}"
  					ajaxCallback="ajax('../app/UiV2ExternalSystem.addExternalSystem?externalSystemConfigId=${guiGrouperExternalSystem.grouperExternalSystem.configId}&externalSystemType=${guiGrouperExternalSystem.grouperExternalSystem['class'].name}', {formIds: 'externalSystemConfigDetails'}); return false;"
  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
  					checkboxAttributes="${attribute.checkboxAttributes}"
  				/>
  				
  			</c:forEach>
  			
  		</tbody>
  
  </c:forEach>
  
  
  
  
  