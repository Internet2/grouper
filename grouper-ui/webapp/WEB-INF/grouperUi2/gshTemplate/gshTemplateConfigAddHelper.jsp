<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.gshTemplateContainer.guiGshTemplateConfiguration}" var="guiGshTemplateConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="gshTemplateConfigId">${textContainer.text['gshTemplateConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
    	
    	<c:choose>    	
	    	<c:when test="${not empty guiGshTemplateConfiguration.gshTemplateConfiguration['class'].name}">
	    		${grouper:escapeHtml(guiGshTemplateConfiguration.gshTemplateConfiguration.configId)}
	    		<input type="hidden" style="width: 30em" value="${grouper:escapeHtml(guiGshTemplateConfiguration.gshTemplateConfiguration.configId)}"
		         name="gshTemplateConfigId" id="configId" />
	    	</c:when>
	    	
	    	<c:otherwise>
	    		<input type="text" style="width: 30em" value="${grouper:escapeHtml(guiGshTemplateConfiguration.gshTemplateConfiguration.configId)}"
		         name="gshTemplateConfigId" id="configId" />
		      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
		        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
	    	</c:otherwise>
    	</c:choose>
    	
      <br />
      <span class="description">${textContainer.text['gshTemplateConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="gshTemplateTypeId">${textContainer.text['gshTemplateTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="gshTemplateType" id="gshTemplateTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2GshTemplateConfig.addGshTemplate', {formIds: 'gshTemplateConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.gshTemplateContainer.allGshTemplateTypes}" var="gshTemplate">
          <option value="${gshTemplate['class'].name}"
              ${guiGshTemplateConfiguration.gshTemplateConfiguration['class'].name == gshTemplate['class'].name ? 'selected="selected"' : '' }
              >${gshTemplate.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['gshTemplateTypeHint']}</span>
    </td>
  </tr>
  	
  <c:forEach items="${guiGshTemplateConfiguration.gshTemplateConfiguration.subSections}" var="subSection">
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
  			
  				<c:set target="${grouperRequestContainer.gshTemplateContainer}"
	               	property="index"
	               	value="${attribute.repeatGroupIndex}" />
               		
        		<c:set target="${grouperRequestContainer.gshTemplateContainer}"
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
  					ajaxCallback="ajax('../app/UiV2GshTemplateConfig.addGshTemplate', {formIds: 'gshTemplateConfigDetails'}); return false;"
  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
  					checkboxAttributes="${attribute.checkboxAttributes}"
  				/>
  				
  			</c:forEach>
  			
  		</tbody>
  
  </c:forEach>
  
  
  
  
  