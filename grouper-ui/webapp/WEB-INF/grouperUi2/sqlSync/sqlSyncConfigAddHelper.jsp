<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set value="${grouperRequestContainer.sqlSyncConfigurationContainer.guiSqlSyncConfiguration}" var="guiSqlSyncConfiguration"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="sqlSyncConfigId">${textContainer.text['sqlSyncConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
    	
   		<input type="text" style="width: 30em" value="${grouper:escapeHtml(guiSqlSyncConfiguration.sqlSyncConfiguration.configId)}"
         name="sqlSyncConfigId" id="sqlSyncConfigId" />
        <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right"
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
    	
      <br />
      <span class="description">${textContainer.text['sqlSyncConfigIdHint']}</span>
    </td>
  </tr>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="sqlSyncTypeId">${textContainer.text['sqlSyncTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="sqlSyncType" id="sqlSyncTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2SqlSyncConfiguration.addSqlSyncConfiguration', {formIds: 'sqlSyncConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.sqlSyncConfigurationContainer.allSqlSyncTypes}" var="sqlSync">
          <option value="${sqlSync['class'].name}"
              ${guiSqlSyncConfiguration.sqlSyncConfiguration['class'].name == sqlSync['class'].name ? 'selected="selected"' : '' }
              >${sqlSync.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['sqlSyncTypeHint']}</span>
    </td>
  </tr>
  	
  <c:forEach items="${guiSqlSyncConfiguration.sqlSyncConfiguration.subSections}" var="subSection">
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
  					ajaxCallback="ajax('../app/UiV2SqlSyncConfiguration.addSqlSyncConfiguration', {formIds: 'sqlSyncConfigDetails'}); return false;"
  					valuesAndLabels="${attribute.dropdownValuesAndLabels }"
  					checkboxAttributes="${attribute.checkboxAttributes}"
  				/>
  				
  			</c:forEach>
  			
  		</tbody>
  
  </c:forEach>
  
  
  
  
  