<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<form id="newGroupTemplateFormId" class="form-horizontal">
 
  <p class="lead" id="templateHeader">${textContainer.text['gshTemplateScreenDecription']}</p>
  <table class="table table-condensed table-striped">
    <tbody>

      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong><label
            for="templateTypeId">${textContainer.text['stemTemplateTypeLabel']}</label></strong></td>
        <td><select name="templateType"
          id="templateTypeId" style="width: 25em"
          onchange="ajax('../app/UiV2Template.newTemplate', {formIds: 'newGroupTemplateFormId'}); return false;">

            <option value="">
            </option>
            
            <c:forEach items="${grouperRequestContainer.groupStemTemplateContainer.templateOptions}"
                 var="templateOption">
              <option value="${templateOption.key}"
              ${grouperRequestContainer.groupStemTemplateContainer.templateType == templateOption.key ? 'selected="selected"' : '' }>${templateOption.value}</option>
            </c:forEach>
                                              
        </select> <br /> <span class="description">${textContainer.text['stemTemplateTypeDescription']}</span>
        </td>
      </tr>
      
      <c:if test="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig != null}">

        <tr class="stem-template-description">
          <td style="vertical-align: top; white-space: nowrap;" colspan="2"><br /><strong style="font-size: larger;">${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.gshTemplateConfig.templateNameForUi }</strong>
          <br />
          <span class="description">${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.gshTemplateConfig.templateDescriptionForUi }</span><br /><br /></td>
        </tr>
      
      	<c:forEach items="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.gshTemplateInputConfigAndValues}" var="guiGshTemplateInputConfigMap">
      			
      			<c:set var="guiGshTemplateInputConfigName" value="${guiGshTemplateInputConfigMap.key}"></c:set>		  				
      			<c:set var="guiGshTemplateInputConfig" value="${guiGshTemplateInputConfigMap.value}"></c:set>		  				
      								  				
      			<grouper:configFormElement 
      				formElementType="${guiGshTemplateInputConfig.gshTemplateInputConfig.configItemFormElement}"
      				configId="${guiGshTemplateInputConfig.gshTemplateInputConfig.name}" 
      				label="${guiGshTemplateInputConfig.gshTemplateInputConfig.labelForUi}"
      				readOnly="false"
      				helperText="${guiGshTemplateInputConfig.gshTemplateInputConfig.descriptionForUi}"
      				helperTextDefaultValue="${guiGshTemplateInputConfig.gshTemplateInputConfig.defaultValue}"
      				required="${guiGshTemplateInputConfig.gshTemplateInputConfig.required}"
      				shouldShow="true"
      				shouldShowElCheckbox="false"
      				value="${guiGshTemplateInputConfig.value}"
      				hasExpressionLanguage="false"
      				ajaxCallback="ajax('../app/UiV2Template.newTemplate?templateType=${grouperRequestContainer.groupStemTemplateContainer.templateType}', {formIds: 'newGroupTemplateFormId'}); return false;"
      				valuesAndLabels="${guiGshTemplateInputConfig.gshTemplateInputConfig.dropdownKeysAndLabels}"
              indent="${attribute.configItemMetadata.indent}"
      			/>
        				
        	</c:forEach>
      
      </c:if>

      <tr>
        <td></td>
        <td
          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
          
          <c:if test="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig != null}">
            <input type="submit" class="btn btn-primary"
	          aria-controls="groupFilterResultsId" id="filterSubmitId"
	          value="${textContainer.text['stemTemplateSubmitButton'] }"
	          onclick="$('#groupTemplateBody').empty(); guiScrollTop(); ajax('../app/UiV2Template.customTemplateExecute?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'newGroupTemplateFormId'}); return false;">
          </c:if>
          
          &nbsp;
          <a href="#" class="btn btn-cancel" role="button" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >${textContainer.text['stemTemplateCancelButton'] }</a>
        </td>
      </tr>
    </tbody>
  </table>  
</form>
<div id="groupTemplateBody"></div>