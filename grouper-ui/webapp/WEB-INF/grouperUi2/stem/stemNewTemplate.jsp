<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<form id="newStemTemplateFormId" class="form-horizontal">
  <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
  <p class="lead" id="templateHeader">${textContainer.text['gshTemplateScreenDecription']}</p>
  <table class="table table-condensed table-striped">
    <tbody>

      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong><label
            for="templateTypeId">${textContainer.text['stemTemplateTypeLabel']}</label></strong></td>
        <td><select name="templateType"
          id="templateTypeId" style="width: 25em"
          onchange="ajax('../app/UiV2Template.newTemplate', {formIds: 'newStemTemplateFormId'}); return false;">

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
      
      <c:if test="${not empty grouperRequestContainer.groupStemTemplateContainer.templateLogic}">
      	
      	<c:if test="${grouperRequestContainer.groupStemTemplateContainer.showInThisFolderCheckbox}">
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label for="createSubfolder">${textContainer.text['stemCreateTemplateInThisFolder']}</label></strong>
          </td>
          <td>
          <input type="checkbox" id="createSubfolder" name="createSubfolder"
                 onchange="$('.stem-template-key').toggle('slow'); $('.stem-template-friendlyName').toggle('slow'); $('.stem-template-description').toggle('slow'); return false;"
                 ${grouperRequestContainer.groupStemTemplateContainer.createNoSubfolder == true ? 'checked="checked"' : '' } />
          </td>
        </tr>
      </c:if>      
      <c:if test="${!grouperRequestContainer.groupStemTemplateContainer.createNoSubfolder}">
	      <tr class="stem-template-key">
	        <td style="vertical-align: top; white-space: nowrap;"><strong><label
	            for="serviceKeyId">${textContainer.text['stemServiceKey']}</label></strong></td>
	        <td>
	        
	          <span style="white-space: nowrap">
	            <input type="text" style="width: 35em" value="${grouper:escapeHtml(grouperRequestContainer.groupStemTemplateContainer.templateKey)}"
	               name="templateKey" id="serviceKeyId"
	               onkeyup="syncNameAndId('serviceKeyId', 'serviceFriendlyNameId', 'nameDifferentThanIdId', false, null); return true;"
	                />
	            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
	              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
	          </span>
	        
	          <br /> <span class="description">${textContainer.text['stemServiceKeyDescription']}</span>
	          
	        </td>
	      </tr>
	      
	      <tr class="stem-template-friendlyName">
	        <td style="vertical-align: top; white-space: nowrap;"><strong><label
	            for="serviceFriendlyNameId">${textContainer.text['stemServiceFriendlyName']}</label></strong></td>
	        <td>
	        
	          <span style="white-space: nowrap">
	            <input type="text" style="width: 35em" value="${grouper:escapeHtml(grouperRequestContainer.groupStemTemplateContainer.templateFriendlyName)}"
	               name="serviceFriendlyName" id="serviceFriendlyNameId" disabled="disabled" />
	          </span>
	          <span style="white-space: nowrap;">
	            <input type="checkbox" name="nameDifferentThanId" id="nameDifferentThanIdId" value="true"
	              onchange="syncNameAndId('serviceKeyId', 'serviceFriendlyNameId', 'nameDifferentThanIdId', false, null); return true;"
	            /> ${textContainer.text['stemServiceEditFriendlyName'] }
	          </span>
	        
	          <br /> <span class="description">${textContainer.text['stemServiceFriendlyNameDescription']}</span>
	          
	        </td>
	      </tr>
	      
	      <tr class="stem-template-description">
	        <td style="vertical-align: top; white-space: nowrap;"><strong><label
	            for="serviceDescriptionId">${textContainer.text['stemServiceDescription']}</label></strong></td>
	        <td>
	          <span style="white-space: nowrap">    
	            <textarea id="serviceDescriptionId" name=serviceDescription rows="3" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.groupStemTemplateContainer.templateDescription)}</textarea>
	          </span>
	        
	          <br /> <span class="description">${textContainer.text['stemServiceDescriptionDescription']}</span>
	          
	        </td>
	      </tr>
      </c:if>
      
      </c:if>
      
      <c:if test="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig != null}">

        <tr class="stem-template-description">
          <td style="vertical-align: top; white-space: nowrap;" colspan="2"><br /><strong style="font-size: larger;">${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.gshTemplateConfig.templateNameForUi }</strong>
          <br />
          <span class="description">${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.gshTemplateConfig.templateDescriptionForUi }</span><br /><br /></td>
        </tr>
      
      	<c:forEach items="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.guiGshTemplateInputConfigs}" var="guiGshTemplateInputConfigMap">
			
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
				ajaxCallback="ajax('../app/UiV2Template.newTemplate?templateType=${grouperRequestContainer.groupStemTemplateContainer.templateType}', {formIds: 'newStemTemplateFormId'}); return false;"
				valuesAndLabels="${guiGshTemplateInputConfig.gshTemplateInputConfig.dropdownKeysAndLabels}"
        indent="${attribute.configItemMetadata.indent}"
        
			/>
  				
  			</c:forEach>
      
      </c:if>
      
      <c:if test="${not empty grouperRequestContainer.groupStemTemplateContainer.serviceActions}">
      <tr>
        <td colspan="2">
          ${textContainer.text['stemServiceActionsHelpText']}
        </td>
      </tr>
      <tr>
      <td colspan="2">

	      <c:forEach items="${grouperRequestContainer.groupStemTemplateContainer.serviceActions}" var="serviceAction">
	       
          <div style="margin: 10px;">
		        <c:set target="${grouperRequestContainer.groupStemTemplateContainer}" property="currentServiceAction" value="${serviceAction}" />
		        <span style="padding-left: ${serviceAction.indentLevel * 20}px;" class="${serviceAction.id}">
		          
			        <input type="checkbox" name="serviceActionId"
			         class="indent-${serviceAction.indentLevel}"
			         onchange="ajax('../app/UiV2Template.reloadServiceActions?serviceActionId=${serviceAction.id}&checked='+this.checked, {formIds: 'newStemTemplateFormId'}); return false;"
			         value="${serviceAction.id}" ${serviceAction.defaultChecked == true ? 'checked="checked"' : '' } />
		          ${textContainer.text[serviceAction.externalizedKey]}
	          </span> 
	        </div>
	      </c:forEach>
	       </td>
      </tr> 
      </c:if>
     

      <tr>
        <td></td>
        <td
          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
          
          <c:if test="${not empty grouperRequestContainer.groupStemTemplateContainer.serviceActions}">
            <input type="submit" class="btn btn-primary"
	          aria-controls="groupFilterResultsId" id="filterSubmitId"
	          value="${textContainer.text['stemTemplateSubmitButton'] }"
	          onclick="$('#stemTemplateBody').empty(); ajax('../app/UiV2Template.newTemplateSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'newStemTemplateFormId'}); return false;">
          </c:if>
          
          <c:if test="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig != null}">
            <input type="submit" class="btn btn-primary"
	          aria-controls="groupFilterResultsId" id="filterSubmitId"
	          value="${textContainer.text['stemTemplateSubmitButton'] }"
	          onclick="$('#stemTemplateBody').empty(); guiScrollTop(); ajax('../app/UiV2Template.customTemplateExecute?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'newStemTemplateFormId'}); return false;">
          </c:if>
          
          <c:if test="${empty grouperRequestContainer.groupStemTemplateContainer.serviceActions and grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig == null}">
            <input type="submit" class="btn btn-primary"
            aria-controls="groupFilterResultsId" id="filterSubmitId"
            value="${textContainer.text['stemTemplateNextButton'] }"
            onclick="$('#stemTemplateBody').empty(); ajax('../app/UiV2Template.loadBeansForServiceTemplateType?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'newStemTemplateFormId'}); return false;">
          </c:if>
          &nbsp;
          <a href="#" class="btn btn-cancel" role="button" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" >${textContainer.text['stemTemplateCancelButton'] }</a>
        </td>
      </tr>
    </tbody>
  </table>  
</form>
<div id="stemTemplateBody"></div>