<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set  value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap; width: 30%;"><strong><label for="provisionerConfigId">${textContainer.text['provisionerConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap; width: 5%;">&nbsp;</td>
    <td>
      <span style="white-space: nowrap">
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiProvisionerConfiguration.provisionerConfiguration.configId)}"
         name="provisionerConfigId" id="provisionerConfigId" />
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      </span>
      <br />
      <span class="description">${textContainer.text['provisionerConfigIdHint']}</span>
    </td>
  </tr>
  	
  	<tr>
      <td style="vertical-align: top; white-space: nowrap; width: 30%;"><strong><label for="provisionerConfigTypeId">${textContainer.text['provisionerTypeLabel']}</label></strong></td>
      <td style="vertical-align: top; white-space: nowrap; width: 5%;">&nbsp;</td>
      <td>
        <span style="white-space: nowrap">
        <select name="provisionerConfigType" id="provisionerConfigTypeId" style="width: 30em"
        onchange="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration?focusOnElementName=provisionerConfigType', {formIds: 'provisionerConfigDetails'}); return false;"
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
        </span>
        <br />
        <span class="description">${textContainer.text['provisionerTypeHint']}</span>
      </td>
    </tr>
    
    <c:if test="${guiProvisionerConfiguration.provisionerConfiguration.startWithConfigClasses.size() > 0 &&
      grouperRequestContainer.provisionerConfigurationContainer.showStartWithSection}">
      
      <input type="hidden" name="startWithSessionId"
        value="${grouperRequestContainer.provisionerConfigurationContainer.startWithSessionId}" />
      
      <tr>
        <td style="vertical-align: top; white-space: nowrap; width: 30%;"><strong><label for="provisionerConfigStartWithId">${textContainer.text['provisionerStartWithLabel']}</label></strong></td>
        <td style="vertical-align: top; white-space: nowrap; width: 5%;">&nbsp;</td>
        <td>
          <span style="white-space: nowrap">
          <select name="provisionerStartWithClass" id="provisionerConfigStartWithId" style="width: 30em"
          onchange="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration?focusOnElementName=provisionerStartWithClass', {formIds: 'provisionerConfigDetails'}); return false;"
          >
           
            <option value="empty"></option>
            <option value="blank">${textContainer.text['startWithBlankConfiguration']}</option>
            <c:forEach items="${guiProvisionerConfiguration.provisionerConfiguration.startWithConfigClasses}" var="startWithConfigClass">
              <option value="${startWithConfigClass['class'].name}"
                  ${grouperRequestContainer.provisionerConfigurationContainer.provisionerStartWith['class'].name == startWithConfigClass['class'].name ? 'selected="selected"' : '' }
                  >${textContainer.text['provisionerStartWithOption_' += startWithConfigClass['class'].name]}</option>
            </c:forEach>
          </select>
          <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
          data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
          </span>
          <br />
          <span class="description">${textContainer.text['provisionerStartWithHint']}</span>
        </td>
      </tr>
    
    
    <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.provisionerStartWith.subSections}" var="subSection">
      <tbody>
        <c:if test="${!grouper:isBlank(subSection.label) and subSection.show}">
          <tr>
            <th colspan="3">
              <%-- the header needs to be on a field to subsitute the name in the label if there --%>
              <c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
                      property="currentConfigSuffix"
                      value="${subSection.label}.header" />  
              <h4>${subSection.title}</h4>
              <p style="font-weight: normal;">${subSection.description}</p>
            </th>
          </tr>
        
        </c:if>
                
        <c:forEach items="${subSection.attributesValues}" var="attribute">  
        
        <%-- <c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
                property="index"
                value="${attribute.repeatGroupIndex}" />  
          <c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
                property="currentConfigSuffix"
                value="${attribute.configSuffix}" />   --%>
        
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
            ajaxCallback="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration?focusOnElementName=config_${attribute.configSuffix}&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}', {formIds: 'provisionerConfigDetails'}); return false;"
            valuesAndLabels="${attribute.dropdownValuesAndLabels }"
            checkboxAttributes="${attribute.checkboxAttributes}"
            indent="${attribute.configItemMetadata.indent}"
          />
          
        </c:forEach>
        
      </tbody>

  </c:forEach>
  
  </c:if>
  
  <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.showStartWithSection == false}">
  
  <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.subSections}" var="subSection">
      <ul>
        <c:if test="${!grouper:isBlank(subSection.label) and subSection.show}">
        <li><a href="#subsection_${subSection.label}">${subSection.title}</a></li>
        </c:if>
      </ul>
   </c:forEach>
      
    <c:forEach items="${guiProvisionerConfiguration.provisionerConfiguration.subSections}" var="subSection">
    		<tbody>
    			<c:if test="${!grouper:isBlank(subSection.label) and subSection.show}">
  	  			<tr>
  	  				<th colspan="3">
                <%-- the header needs to be on a field to subsitute the name in the label if there --%>
                <c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
                        property="currentConfigSuffix"
                        value="${subSection.label}.header" />
                <a id="subsection_${subSection.label}" href="#">${textContainer.text['backToTop'] }</a> 
  	  					<h4>${subSection.title}</h4>
  	  					<p style="font-weight: normal;">${subSection.description} </p>
  	  					<p style="font-weight: normal;">${subSection.documentation} </p>
  	  				</th>
  	  			</tr>
    			
    			</c:if>
    			  			
    			<c:forEach items="${subSection.attributesValues}" var="attribute">	
    			
    			<c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
                 	property="index"
                 	value="${attribute.repeatGroupIndex}" />	
          	<c:set target="${grouperRequestContainer.provisionerConfigurationContainer}"
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
    					ajaxCallback="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration?focusOnElementName=config_${attribute.configSuffix}&provisionerConfigId=${guiProvisionerConfiguration.provisionerConfiguration.configId}&provisionerConfigType=${guiProvisionerConfiguration.provisionerConfiguration['class'].name}', {formIds: 'provisionerConfigDetails'}); return false;"
    					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
    					checkboxAttributes="${attribute.checkboxAttributes}"
              indent="${attribute.configItemMetadata.indent}"
    				/>
    				
    			</c:forEach>
    			
    		</tbody>
  
    </c:forEach>
  </c:if>
  