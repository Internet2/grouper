<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<form id="newStemTemplateFormId" class="form-horizontal">
  <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
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
            
            <c:forEach items="${grouperRequestContainer.stemTemplateContainer.templateOptions}"
                 var="templateOption">
              <option value="${templateOption.key}"
              ${grouperRequestContainer.stemTemplateContainer.templateType == templateOption.key ? 'selected="selected"' : '' }>${templateOption.value}</option>
            </c:forEach>
                                              
        </select> <br /> <span class="description">${textContainer.text['stemTemplateTypeDescription']}</span>
        </td>
      </tr>
      
      <c:if test="${grouperRequestContainer.stemTemplateContainer.templateLogic.promptForKeyAndLabelAndDescription}">
      
	      <tr>
	        <td style="vertical-align: top; white-space: nowrap;"><strong><label
	            for="serviceKeyId">${textContainer.text['stemServiceKey']}</label></strong></td>
	        <td>
	        
	          <span style="white-space: nowrap">
	            <input type="text" style="width: 35em" value="${grouper:escapeHtml(grouperRequestContainer.stemTemplateContainer.templateKey)}"
	               name="templateKey" id="serviceKeyId"
	               onkeyup="syncNameAndId('serviceKeyId', 'serviceFriendlyNameId', 'nameDifferentThanIdId', false, null); return true;"
	                />
	            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
	              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
	          </span>
	        
	          <br /> <span class="description">${textContainer.text['stemServiceKeyDescription']}</span>
	          
	        </td>
	      </tr>
	      
	      <tr>
	        <td style="vertical-align: top; white-space: nowrap;"><strong><label
	            for="serviceFriendlyNameId">${textContainer.text['stemServiceFriendlyName']}</label></strong></td>
	        <td>
	        
	          <span style="white-space: nowrap">
	            <input type="text" style="width: 35em" value="${grouper:escapeHtml(grouperRequestContainer.stemTemplateContainer.templateFriendlyName)}"
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
	      
	      <tr>
	        <td style="vertical-align: top; white-space: nowrap;"><strong><label
	            for="serviceDescriptionId">${textContainer.text['stemServiceDescription']}</label></strong></td>
	        <td>
	          <span style="white-space: nowrap">    
	            <textarea id="serviceDescriptionId" name=serviceDescription rows="3" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.stemTemplateContainer.templateDescription)}</textarea>
	          </span>
	        
	          <br /> <span class="description">${textContainer.text['stemServiceDescriptionDescription']}</span>
	          
	        </td>
	      </tr>
      
      </c:if>
      

      <c:if test="${! empty grouperRequestContainer.stemTemplateContainer.serviceActions}">
      <tr>
        <td colspan="2">
          ${textContainer.text['stemServiceActionsHelpText']}
        </td>
      </tr>
      <tr>
      <td colspan="2">

	      <c:forEach items="${grouperRequestContainer.stemTemplateContainer.serviceActions}" var="serviceAction">
	       
          <div style="margin: 10px;">
		        <c:set target="${grouperRequestContainer.stemTemplateContainer}" property="currentServiceAction" value="${serviceAction}" />
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
          
          <c:if test="${!grouper:isBlank(grouperRequestContainer.stemTemplateContainer.templateKey)}">
            <input type="submit" class="btn btn-primary"
	          aria-controls="groupFilterResultsId" id="filterSubmitId"
	          value="${textContainer.text['stemTemplateSubmitButton'] }"
	          onclick="ajax('../app/UiV2Template.newTemplateSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'newStemTemplateFormId'}); return false;">
          </c:if>
          <c:if test="${grouper:isBlank(grouperRequestContainer.stemTemplateContainer.templateKey)}">
            <input type="submit" class="btn btn-primary"
            aria-controls="groupFilterResultsId" id="filterSubmitId"
            value="${textContainer.text['stemTemplateNextButton'] }"
            onclick="ajax('../app/UiV2Template.loadBeansForServiceTemplateType?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'newStemTemplateFormId'}); return false;">
          </c:if>
          &nbsp; <a class="btn btn-cancel" role="button"
          onclick="return guiV2link('operation=UiV2Attestation.stemAttestation?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">${textContainer.text['grouperAttestationEditButtonCancel'] }</a>
        </td>
      </tr>
    </tbody>
  </table>  
</form>