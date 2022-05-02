<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set  value="${grouperRequestContainer.subjectSourceContainer.guiSubjectSourceConfiguration}" var="guiSubjectSourceConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap; width: 30%;"><strong><label for="sourceConfigId">${textContainer.text['subjectSourceConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap; width: 5%;">&nbsp;</td>
    <td>
      <span style="white-space: nowrap">
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiSubjectSourceConfiguration.subjectSourceConfiguration.configId)}"
         name="subjectSourceConfigId" id="subjectSourceConfigId" />
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      </span>
      <br />
      <span class="description">${textContainer.text['subjectSourceConfigIdHint']}</span>
    </td>
  </tr>
  	
  	<tr>
    <td style="vertical-align: top; white-space: nowrap; width: 30%;"><strong><label for="subjectSourceConfigTypeId">${textContainer.text['subjectSourceTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap; width: 5%;">&nbsp;</td>
    <td>
      <span style="white-space: nowrap">
      <select name="subjectSourceConfigType" id="subjectSourceConfigTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2SubjectSource.addSubjectSource?focusOnElementName=sourceConfigType', {formIds: 'sourceConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.subjectSourceContainer.allSubjectSourceConfigurationTypes}" var="sourceConfiguration">
          <option value="${sourceConfiguration['class'].name}"
              ${guiSubjectSourceConfiguration.subjectSourceConfiguration['class'].name == sourceConfiguration['class'].name ? 'selected="selected"' : '' }
              >${sourceConfiguration.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      </span>
      <br />
      <span class="description">${textContainer.text['subjectSourceTypeHint']}</span>
    </td>
  </tr>
  
  <c:forEach items="${guiSubjectSourceConfiguration.subjectSourceConfiguration.subSections}" var="subSection">
  		<tbody>
  			<c:if test="${!grouper:isBlank(subSection.label) and subSection.show}">
	  			<tr>
	  				<th colspan="3">
	  				
	  				<c:set target="${grouperRequestContainer.subjectSourceContainer}"
		               	property="index"
		               	value="${subSection.attributesValues == null || subSection.attributesValues.size() == 0  ? -1 : subSection.attributesValues.iterator().next().repeatGroupIndex}" />
	  				
              <%-- the header needs to be on a field to subsitute the name in the label if there --%>
              <c:set target="${grouperRequestContainer.subjectSourceContainer}"
                      property="currentConfigSuffix"
                      value="${subSection.label}.header" />  
	  					<h4>${subSection.title}</h4>
	  					<p style="font-weight: normal;">${subSection.description} </p>
	  				</th>
	  			</tr>
  			
  			</c:if>
  			  			
  			<c:forEach items="${subSection.attributesValues}" var="attribute">	
  			
  				<c:set target="${grouperRequestContainer.subjectSourceContainer}"
               	property="index"
               	value="${attribute.repeatGroupIndex}" />
               		
        		<c:set target="${grouperRequestContainer.subjectSourceContainer}"
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
  					ajaxCallback="ajax('../app/UiV2SubjectSource.addSubjectSource?focusOnElementName=config_${attribute.configSuffix}&sourceConfigId=${guiSubjectSourceConfiguration.subjectSourceConfiguration.configId}&sourceConfigType=${guiSubjectSourceConfiguration.subjectSourceConfiguration['class'].name}', {formIds: 'sourceConfigDetails'}); return false;"
  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
  					checkboxAttributes="${attribute.checkboxAttributes}"
            indent="${attribute.configItemMetadata.indent}"
  				/>
  				
  			</c:forEach>
  			
  		</tbody>

  </c:forEach>
  